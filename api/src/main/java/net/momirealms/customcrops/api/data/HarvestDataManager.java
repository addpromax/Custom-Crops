/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customcrops.api.data;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.common.plugin.feature.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages harvest data for all players with 3-second cache and database storage.
 */
public class HarvestDataManager implements Reloadable, Listener {

    private final BukkitCustomCropsPlugin plugin;
    private final Map<UUID, PlayerHarvestData> dataCache;
    private final Map<UUID, Long> dirtyTimestamps;
    private ScheduledExecutorService cacheExecutor;
    private DatabaseStorage storage;

    /**
     * Creates a new HarvestDataManager.
     *
     * @param plugin The plugin instance
     */
    public HarvestDataManager(@NotNull BukkitCustomCropsPlugin plugin) {
        this.plugin = plugin;
        this.dataCache = new ConcurrentHashMap<>();
        this.dirtyTimestamps = new ConcurrentHashMap<>();
    }

    @Override
    public void load() {
        // Initialize database storage
        initializeStorage();
        
        // Create or recreate executor service
        if (cacheExecutor == null || cacheExecutor.isShutdown()) {
            cacheExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "CustomCrops-HarvestData-Cache");
                thread.setDaemon(true);
                return thread;
            });
        }
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin.getBootstrap());

        // Load data for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }

        // Start cache flush task (every 1 second, flush data older than configured seconds)
        cacheExecutor.scheduleAtFixedRate(this::flushDirtyData, 1, 1, TimeUnit.SECONDS);

        plugin.getPluginLogger().info("HarvestDataManager loaded with " + storage.getType() + " storage");
    }

    @Override
    public void unload() {
        // Flush all dirty data
        flushAllData();
        
        // Shutdown cache executor
        if (cacheExecutor != null && !cacheExecutor.isShutdown()) {
            cacheExecutor.shutdown();
            try {
                if (!cacheExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cacheExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cacheExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Close database
        if (storage != null) {
            storage.close();
        }
        
        // Clear cache
        dataCache.clear();
        dirtyTimestamps.clear();
    }

    @Override
    public void reload() {
        unload();
        load();
    }

    @Override
    public void disable() {
        unload();
    }

    /**
     * Initializes the database storage based on configuration.
     */
    private void initializeStorage() {
        YamlDocument config = getConfig();
        
        // Check if harvest data tracking is enabled
        if (!config.getBoolean("other-settings.harvest-data.enable", true)) {
            plugin.getPluginLogger().warn("Harvest data tracking is disabled in config.yml");
            storage = new SQLiteStorage(plugin); // Use dummy storage
            return;
        }
        
        // Read storage type from config
        String storageType = config.getString("other-settings.harvest-data.storage-type", "sqlite");
        
        if (storageType.equalsIgnoreCase("mysql")) {
            // Read MySQL configuration
            String host = config.getString("other-settings.harvest-data.mysql.host", "localhost");
            int port = config.getInt("other-settings.harvest-data.mysql.port", 3306);
            String database = config.getString("other-settings.harvest-data.mysql.database", "customcrops");
            String username = config.getString("other-settings.harvest-data.mysql.username", "root");
            String password = config.getString("other-settings.harvest-data.mysql.password", "");
            String tablePrefix = config.getString("other-settings.harvest-data.mysql.table-prefix", "cc_");
            
            // Read pool settings
            int maxPoolSize = config.getInt("other-settings.harvest-data.mysql.pool.maximum-pool-size", 10);
            int minIdle = config.getInt("other-settings.harvest-data.mysql.pool.minimum-idle", 2);
            long connectionTimeout = config.getLong("other-settings.harvest-data.mysql.pool.connection-timeout", 30000L);
            long idleTimeout = config.getLong("other-settings.harvest-data.mysql.pool.idle-timeout", 600000L);
            long maxLifetime = config.getLong("other-settings.harvest-data.mysql.pool.max-lifetime", 1800000L);
            
            storage = new MySQLStorage(plugin, host, port, database, username, password, tablePrefix,
                                      maxPoolSize, minIdle, connectionTimeout, idleTimeout, maxLifetime);
        } else {
            // Default to SQLite
            storage = new SQLiteStorage(plugin);
        }
        
        storage.initialize();
    }
    
    /**
     * Gets the main configuration document.
     */
    private YamlDocument getConfig() {
        // This will be implemented in the concrete implementation
        // For now, return a method that accesses BukkitConfigManager's static method
        try {
            Class<?> configManagerClass = Class.forName("net.momirealms.customcrops.bukkit.config.BukkitConfigManager");
            java.lang.reflect.Method method = configManagerClass.getMethod("getMainConfig");
            return (YamlDocument) method.invoke(null);
        } catch (Exception e) {
            plugin.getPluginLogger().severe("Failed to access configuration", e);
            throw new RuntimeException("Failed to access configuration", e);
        }
    }

    /**
     * Gets or creates player harvest data.
     *
     * @param playerId The player's UUID
     * @return The player's harvest data
     */
    @NotNull
    public PlayerHarvestData getOrCreateData(@NotNull UUID playerId) {
        return dataCache.computeIfAbsent(playerId, uuid -> {
            PlayerHarvestData data = storage.loadPlayerData(uuid);
            if (data == null) {
                data = new PlayerHarvestData(uuid);
            }
            return data;
        });
    }

    /**
     * Gets player harvest data if it exists.
     *
     * @param playerId The player's UUID
     * @return The player's harvest data, or null if not found
     */
    @Nullable
    public PlayerHarvestData getData(@NotNull UUID playerId) {
        return dataCache.get(playerId);
    }

    /**
     * Adds a harvest record for a player.
     *
     * @param playerId The player's UUID
     * @param cropId The crop ID
     * @param amount The amount harvested
     */
    public void addHarvest(@NotNull UUID playerId, @NotNull String cropId, int amount) {
        PlayerHarvestData data = getOrCreateData(playerId);
        data.addHarvest(cropId, amount);
        markDirty(playerId);
    }

    /**
     * Gets the harvest count for a specific crop.
     *
     * @param playerId The player's UUID
     * @param cropId The crop ID
     * @return The harvest count
     */
    public int getHarvestCount(@NotNull UUID playerId, @NotNull String cropId) {
        PlayerHarvestData data = getData(playerId);
        return data != null ? data.getHarvestCount(cropId) : 0;
    }

    /**
     * Gets the total harvest count for a player.
     *
     * @param playerId The player's UUID
     * @return The total harvest count
     */
    public int getTotalHarvests(@NotNull UUID playerId) {
        PlayerHarvestData data = getData(playerId);
        return data != null ? data.getTotalHarvests() : 0;
    }

    /**
     * Checks if a player has harvested a specific crop.
     *
     * @param playerId The player's UUID
     * @param cropId The crop ID
     * @return True if the crop has been harvested
     */
    public boolean hasHarvested(@NotNull UUID playerId, @NotNull String cropId) {
        PlayerHarvestData data = getData(playerId);
        return data != null && data.hasHarvested(cropId);
    }

    /**
     * Adds a quality item record for a player.
     *
     * @param playerId The player's UUID
     * @param itemId The item ID
     * @param amount The amount obtained
     */
    public void addQualityItem(@NotNull UUID playerId, @NotNull String itemId, int amount) {
        PlayerHarvestData data = getOrCreateData(playerId);
        data.addQualityItem(itemId, amount);
        markDirty(playerId);
    }

    /**
     * Gets the quality item count.
     *
     * @param playerId The player's UUID
     * @param itemId The item ID
     * @return The item count
     */
    public int getQualityItemCount(@NotNull UUID playerId, @NotNull String itemId) {
        PlayerHarvestData data = getData(playerId);
        return data != null ? data.getQualityItemCount(itemId) : 0;
    }

    /**
     * Checks if a player has obtained a specific quality item.
     *
     * @param playerId The player's UUID
     * @param itemId The item ID
     * @return True if the item has been obtained
     */
    public boolean hasObtainedQualityItem(@NotNull UUID playerId, @NotNull String itemId) {
        PlayerHarvestData data = getData(playerId);
        return data != null && data.hasObtainedQualityItem(itemId);
    }

    /**
     * Loads player data into cache.
     *
     * @param playerId The player's UUID
     */
    public void loadPlayerData(@NotNull UUID playerId) {
        if (!dataCache.containsKey(playerId)) {
            getOrCreateData(playerId);
        }
    }

    /**
     * Marks player data as dirty (modified).
     *
     * @param playerId The player's UUID
     */
    private void markDirty(@NotNull UUID playerId) {
        dirtyTimestamps.put(playerId, System.currentTimeMillis());
    }

    /**
     * Flushes dirty data that is older than configured cache time.
     */
    private void flushDirtyData() {
        long now = System.currentTimeMillis();
        int flushDelay = getConfig().getInt("other-settings.harvest-data.cache.flush-delay", 3);
        long cacheTime = flushDelay * 1000L; // Convert to milliseconds
        
        dirtyTimestamps.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            long timestamp = entry.getValue();
            
            if (now - timestamp >= cacheTime) {
                PlayerHarvestData data = dataCache.get(playerId);
                if (data != null) {
                    plugin.getScheduler().async().execute(() -> storage.savePlayerData(data));
                }
                return true; // Remove from dirty timestamps
            }
            return false;
        });
    }

    /**
     * Flushes all cached data immediately.
     */
    private void flushAllData() {
        if (storage == null || dataCache.isEmpty()) {
            return;
        }
        
        plugin.getPluginLogger().info("Flushing all harvest data...");
        for (PlayerHarvestData data : dataCache.values()) {
            try {
                storage.savePlayerData(data);
            } catch (Exception e) {
                plugin.getPluginLogger().severe("Failed to save harvest data for player " + data.getPlayerUUID(), e);
            }
        }
        dirtyTimestamps.clear();
        plugin.getPluginLogger().info("All harvest data flushed");
    }

    /**
     * Event handler for player join.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getScheduler().async().execute(() -> {
            loadPlayerData(event.getPlayer().getUniqueId());
        });
    }

    /**
     * Event handler for player quit.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Force save on quit
        PlayerHarvestData data = dataCache.get(playerId);
        if (data != null) {
            plugin.getScheduler().async().execute(() -> {
                storage.savePlayerData(data);
                dirtyTimestamps.remove(playerId);
            });
        }
        
        // Remove from cache after configured delay
        int keepAfterQuit = getConfig().getInt("other-settings.harvest-data.cache.keep-after-quit", 5);
        if (keepAfterQuit > 0) {
            long delayTicks = keepAfterQuit * 20L; // Convert seconds to ticks
            Bukkit.getScheduler().runTaskLater(plugin.getBootstrap(), () -> {
                dataCache.remove(playerId);
            }, delayTicks);
        } else {
            // Remove immediately if keepAfterQuit is 0
            dataCache.remove(playerId);
        }
    }
}
