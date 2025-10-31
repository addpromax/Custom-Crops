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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

/**
 * MySQL implementation of database storage using HikariCP.
 */
public class MySQLStorage implements DatabaseStorage {

    private final BukkitCustomCropsPlugin plugin;
    private final Gson gson;
    private HikariDataSource dataSource;
    
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;
    private final int maxPoolSize;
    private final int minIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;

    public MySQLStorage(BukkitCustomCropsPlugin plugin, String host, int port, String database, 
                       String username, String password, String tablePrefix,
                       int maxPoolSize, int minIdle, long connectionTimeout, 
                       long idleTimeout, long maxLifetime) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
        this.maxPoolSize = maxPoolSize;
        this.minIdle = minIdle;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxLifetime = maxLifetime;
    }

    @Override
    public void initialize() {
        try {
            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + 
                            "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(minIdle);
            config.setConnectionTimeout(connectionTimeout);
            config.setIdleTimeout(idleTimeout);
            config.setMaxLifetime(maxLifetime);
            
            dataSource = new HikariDataSource(config);
            
            // Create table
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + "harvest_data (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "harvests TEXT," +
                    "quality_items TEXT," +
                    "total_harvests INT," +
                    "last_updated BIGINT" +
                    ")"
                );
            }
            
            plugin.getPluginLogger().info("MySQL storage initialized successfully");
        } catch (Exception e) {
            plugin.getPluginLogger().severe("Failed to initialize MySQL storage", e);
        }
    }

    @Override
    @Nullable
    public PlayerHarvestData loadPlayerData(@NotNull UUID playerId) {
        String sql = "SELECT * FROM " + tablePrefix + "harvest_data WHERE uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerId.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PlayerHarvestData data = new PlayerHarvestData(playerId);
                    
                    // Load harvests
                    String harvestsJson = rs.getString("harvests");
                    if (harvestsJson != null && !harvestsJson.isEmpty()) {
                        Map<String, Integer> harvests = gson.fromJson(
                            harvestsJson,
                            new TypeToken<Map<String, Integer>>(){}.getType()
                        );
                        harvests.forEach(data::setHarvestCount);
                    }
                    
                    // Load quality items
                    String qualityItemsJson = rs.getString("quality_items");
                    if (qualityItemsJson != null && !qualityItemsJson.isEmpty()) {
                        Map<String, Integer> qualityItems = gson.fromJson(
                            qualityItemsJson,
                            new TypeToken<Map<String, Integer>>(){}.getType()
                        );
                        qualityItems.forEach(data::setQualityItemCount);
                    }
                    
                    data.setTotalHarvests(rs.getInt("total_harvests"));
                    data.recalculateTotalHarvests();
                    
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getPluginLogger().severe("Failed to load player data for " + playerId, e);
        }
        
        return null;
    }

    @Override
    public void savePlayerData(@NotNull PlayerHarvestData data) {
        String sql = "INSERT INTO " + tablePrefix + "harvest_data (uuid, harvests, quality_items, total_harvests, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE harvests = VALUES(harvests), quality_items = VALUES(quality_items), " +
                    "total_harvests = VALUES(total_harvests), last_updated = VALUES(last_updated)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data.getPlayerUUID().toString());
            pstmt.setString(2, gson.toJson(data.getHarvestCounts()));
            pstmt.setString(3, gson.toJson(data.getQualityItemCounts()));
            pstmt.setInt(4, data.getTotalHarvests());
            pstmt.setLong(5, data.getLastUpdated());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getPluginLogger().severe("Failed to save player data for " + data.getPlayerUUID(), e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getPluginLogger().info("MySQL connection pool closed");
        }
    }

    @Override
    public String getType() {
        return "MySQL";
    }
}

