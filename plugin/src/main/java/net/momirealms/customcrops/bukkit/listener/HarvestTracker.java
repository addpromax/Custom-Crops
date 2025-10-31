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

package net.momirealms.customcrops.bukkit.listener;

import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.data.HarvestDataManager;
import net.momirealms.customcrops.api.event.QualityCropActionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to crop break events and tracks harvest statistics.
 */
public class HarvestTracker implements Listener {

    private final BukkitCustomCropsPlugin plugin;
    private final HarvestDataManager dataManager;

    /**
     * Creates a new HarvestTracker.
     *
     * @param plugin The plugin instance
     * @param dataManager The harvest data manager
     */
    public HarvestTracker(BukkitCustomCropsPlugin plugin, HarvestDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Handles quality crop action events to track harvests and quality item drops.
     * This event is only triggered when a crop produces drops (i.e., when it's mature).
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQualityCropDrop(QualityCropActionEvent event) {
        // Only count if the context holder is a player
        final Player player;
        
        if (event.context().holder() instanceof Player) {
            player = (Player) event.context().holder();
        } else {
            return; // Skip if holder is not a player
        }
        
        // Debug: Log raw quality crops array
        String[] qualityCrops = event.qualityCrops();
        plugin.getPluginLogger().info("[HarvestTracker] Player: " + player.getName() + 
                                     ", Quality Crops Array: " + java.util.Arrays.toString(qualityCrops) +
                                     ", Items Count: " + event.items().size());
        
        // Get crop ID from quality loots (first entry is typically the base crop)
        String cropId = null;
        if (qualityCrops != null && qualityCrops.length > 0 && qualityCrops[0] != null && !qualityCrops[0].isEmpty()) {
            cropId = qualityCrops[0];
            
            plugin.getPluginLogger().info("[HarvestTracker] Original Crop ID: " + cropId);
            
            // Remove namespace prefix (e.g., "customcrops:orange_bell_pepper" -> "orange_bell_pepper")
            if (cropId.contains(":")) {
                cropId = cropId.substring(cropId.indexOf(":") + 1);
                plugin.getPluginLogger().info("[HarvestTracker] Removed Namespace, New ID: " + cropId);
            }
            
            // Extract base crop name if the ID contains quality suffix (at the END)
            // e.g., "strawberry_gold" -> "strawberry"
            // But keep names like "orange_bell_pepper" intact
            if (cropId.endsWith("_gold") || cropId.endsWith("_silver") || cropId.endsWith("_normal")) {
                cropId = cropId.replaceAll("_(gold|silver|normal)$", "");
                plugin.getPluginLogger().info("[HarvestTracker] Stripped Quality Suffix, New ID: " + cropId);
            }
        }
        
        // Count harvest if we have a valid crop ID and items were dropped
        if (cropId != null && !cropId.isEmpty() && !event.items().isEmpty()) {
            dataManager.addHarvest(player.getUniqueId(), cropId, 1);
            
            final String finalCropId = cropId;
            plugin.getPluginLogger().info(String.format(
                "[HarvestTracker] Player %s harvested crop %s (Total: %d, Crop: %d)",
                player.getName(),
                finalCropId,
                dataManager.getTotalHarvests(player.getUniqueId()),
                dataManager.getHarvestCount(player.getUniqueId(), finalCropId)
            ));
        } else {
            plugin.getPluginLogger().warn("[HarvestTracker] NOT COUNTED - CropID: " + cropId + 
                                         ", Items Empty: " + event.items().isEmpty());
        }
        
        // Track each quality item dropped
        for (ItemStack item : event.items()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            
            // Get item ID from ItemStack or quality crops array
            String itemId = getItemIdFromStack(item, qualityCrops);
            
            if (itemId != null && !itemId.isEmpty()) {
                dataManager.addQualityItem(player.getUniqueId(), itemId, item.getAmount());
                
                // Debug logging if enabled
                plugin.debug(() -> String.format(
                    "Player %s obtained quality item %s x%d",
                    player.getName(),
                    itemId,
                    item.getAmount()
                ));
            }
        }
    }

    /**
     * Attempts to extract item ID from ItemStack.
     * This is a helper method to match dropped items with quality crop IDs.
     */
    private String getItemIdFromStack(ItemStack item, String[] qualityCrops) {
        // Try to get ID from ItemManager
        String itemId = plugin.getItemManager().id(item);
        if (itemId != null && !itemId.isEmpty()) {
            // Remove namespace prefix if present
            if (itemId.contains(":")) {
                itemId = itemId.substring(itemId.indexOf(":") + 1);
            }
            return itemId;
        }
        
        // Fallback: check if any quality crop ID matches
        // This is not perfect but works for most cases
        for (String cropId : qualityCrops) {
            if (cropId != null && !cropId.isEmpty()) {
                ItemStack builtItem = plugin.getItemManager().build(null, cropId);
                if (builtItem != null && builtItem.isSimilar(item)) {
                    // Remove namespace prefix before returning
                    if (cropId.contains(":")) {
                        return cropId.substring(cropId.indexOf(":") + 1);
                    }
                    return cropId;
                }
            }
        }
        
        return null;
    }
}

