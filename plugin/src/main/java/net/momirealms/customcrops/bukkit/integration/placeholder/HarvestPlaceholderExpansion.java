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

package net.momirealms.customcrops.bukkit.integration.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.data.HarvestDataManager;
import net.momirealms.customcrops.api.data.PlayerHarvestData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for harvest tracking system and world data.
 * 
 * Supported placeholders:
 * - %customcrops_total_harvests% - Total harvest count
 * - %customcrops_crop_<cropId>% - Harvest count for specific crop
 * - %customcrops_quality_<itemId>% - Quality item count
 * - %customcrops_has_harvested_<cropId>% - Returns true/false
 * - %customcrops_has_quality_<itemId>% - Returns true/false
 * - %customcrops_unique_crops% - Number of unique crops harvested
 * - %customcrops_unique_qualities% - Number of unique quality items obtained
 * - %customcrops_season% - Current season of player's world
 * - %customcrops_season_<worldName>% - Season of specific world
 * - %customcrops_date% - Current date of player's world
 * - %customcrops_date_<worldName>% - Date of specific world
 */
public class HarvestPlaceholderExpansion extends PlaceholderExpansion {

    private final BukkitCustomCropsPlugin plugin;
    private final HarvestDataManager dataManager;

    public HarvestPlaceholderExpansion(BukkitCustomCropsPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getHarvestDataManager();
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "customcrops";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getPluginVersion();
    }

    @Override
    public boolean persist() {
        return true; // Keep expansion loaded after reload
    }

    @Override
    @Nullable
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String[] split = params.split("_", 2);
        
        switch (split[0]) {
            case "season" -> {
                if (split.length == 1) {
                    Player player = offlinePlayer.getPlayer();
                    if (player == null)
                        return null;
                    return plugin.getWorldManager().getSeason(player.getWorld()).translation();
                } else {
                    try {
                        return plugin.getWorldManager().getSeason(Bukkit.getWorld(split[1])).translation();
                    } catch (NullPointerException e) {
                        plugin.getPluginLogger().severe("World " + split[1] + " does not exist");
                    }
                }
            }
            case "date" -> {
                if (split.length == 1) {
                    Player player = offlinePlayer.getPlayer();
                    if (player == null)
                        return null;
                    return String.valueOf(plugin.getWorldManager().getDate(player.getWorld()));
                } else {
                    try {
                        return String.valueOf(plugin.getWorldManager().getDate(Bukkit.getWorld(split[1])));
                    } catch (NullPointerException e) {
                        plugin.getPluginLogger().severe("World " + split[1] + " does not exist");
                    }
                }
            }
        }

        // Handle harvest data placeholders - need online player for these
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        PlayerHarvestData data = dataManager.getData(player.getUniqueId());

        // %customcrops_total_harvests%
        if (params.equals("total_harvests")) {
            return String.valueOf(dataManager.getTotalHarvests(player.getUniqueId()));
        }

        // %customcrops_unique_crops%
        if (params.equals("unique_crops")) {
            return data != null ? String.valueOf(data.getHarvestCounts().size()) : "0";
        }

        // %customcrops_unique_qualities%
        if (params.equals("unique_qualities")) {
            return data != null ? String.valueOf(data.getQualityItemCounts().size()) : "0";
        }

        // %customcrops_crop_<cropId>%
        if (params.startsWith("crop_")) {
            String cropId = params.substring(5);
            return String.valueOf(dataManager.getHarvestCount(player.getUniqueId(), cropId));
        }

        // %customcrops_quality_<itemId>%
        if (params.startsWith("quality_")) {
            String itemId = params.substring(8);
            return String.valueOf(dataManager.getQualityItemCount(player.getUniqueId(), itemId));
        }

        // %customcrops_has_harvested_<cropId>%
        if (params.startsWith("has_harvested_")) {
            String cropId = params.substring(14);
            boolean hasHarvested = dataManager.hasHarvested(player.getUniqueId(), cropId);
            return hasHarvested ? "true" : "false";
        }

        // %customcrops_has_quality_<itemId>%
        if (params.startsWith("has_quality_")) {
            String itemId = params.substring(12);
            boolean hasQuality = dataManager.hasObtainedQualityItem(player.getUniqueId(), itemId);
            return hasQuality ? "true" : "false";
        }

        return null;
    }
}

