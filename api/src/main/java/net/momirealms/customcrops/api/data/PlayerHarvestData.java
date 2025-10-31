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

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents harvest data for a player, tracking the number of times
 * each crop has been harvested and quality items obtained.
 */
public class PlayerHarvestData {

    @SerializedName("uuid")
    private final UUID playerUUID;

    @SerializedName("harvests")
    private final Map<String, Integer> harvestCounts;

    @SerializedName("quality_items")
    private final Map<String, Integer> qualityItemCounts;

    @SerializedName("total_harvests")
    private int totalHarvests;

    @SerializedName("last_updated")
    private long lastUpdated;

    /**
     * Creates a new PlayerHarvestData instance.
     *
     * @param playerUUID The UUID of the player
     */
    public PlayerHarvestData(@NotNull UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.harvestCounts = new ConcurrentHashMap<>();
        this.qualityItemCounts = new ConcurrentHashMap<>();
        this.totalHarvests = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Gets the player's UUID.
     *
     * @return The player's UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Adds a harvest count for a specific crop.
     *
     * @param cropId The crop ID
     * @param amount The amount to add
     */
    public void addHarvest(@NotNull String cropId, int amount) {
        if (amount <= 0) return;
        harvestCounts.merge(cropId, amount, Integer::sum);
        totalHarvests += amount;
        lastUpdated = System.currentTimeMillis();
    }

    /**
     * Gets the harvest count for a specific crop.
     *
     * @param cropId The crop ID
     * @return The harvest count, or 0 if never harvested
     */
    public int getHarvestCount(@NotNull String cropId) {
        return harvestCounts.getOrDefault(cropId, 0);
    }

    /**
     * Gets the total number of harvests across all crops.
     *
     * @return The total harvest count
     */
    public int getTotalHarvests() {
        return totalHarvests;
    }

    /**
     * Checks if the player has harvested a specific crop.
     *
     * @param cropId The crop ID
     * @return True if the crop has been harvested at least once
     */
    public boolean hasHarvested(@NotNull String cropId) {
        return harvestCounts.containsKey(cropId);
    }

    /**
     * Gets all harvest counts.
     *
     * @return A map of crop IDs to harvest counts
     */
    @NotNull
    public Map<String, Integer> getHarvestCounts() {
        return new ConcurrentHashMap<>(harvestCounts);
    }

    /**
     * Gets the last update timestamp.
     *
     * @return The timestamp in milliseconds
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the harvest count for a specific crop (used for data loading).
     *
     * @param cropId The crop ID
     * @param count The count
     */
    public void setHarvestCount(@NotNull String cropId, int count) {
        if (count > 0) {
            harvestCounts.put(cropId, count);
        }
    }

    /**
     * Sets the total harvests (used for data loading).
     *
     * @param total The total harvest count
     */
    public void setTotalHarvests(int total) {
        this.totalHarvests = total;
    }

    /**
     * Recalculates the total harvests from individual crop counts.
     */
    public void recalculateTotalHarvests() {
        this.totalHarvests = harvestCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Clears all harvest data.
     */
    public void clear() {
        harvestCounts.clear();
        qualityItemCounts.clear();
        totalHarvests = 0;
        lastUpdated = System.currentTimeMillis();
    }

    /**
     * Adds a quality item count.
     *
     * @param itemId The item ID
     * @param amount The amount to add
     */
    public void addQualityItem(@NotNull String itemId, int amount) {
        if (amount <= 0) return;
        qualityItemCounts.merge(itemId, amount, Integer::sum);
        lastUpdated = System.currentTimeMillis();
    }

    /**
     * Gets the quality item count.
     *
     * @param itemId The item ID
     * @return The item count, or 0 if never obtained
     */
    public int getQualityItemCount(@NotNull String itemId) {
        return qualityItemCounts.getOrDefault(itemId, 0);
    }

    /**
     * Checks if the player has obtained a specific quality item.
     *
     * @param itemId The item ID
     * @return True if the item has been obtained at least once
     */
    public boolean hasObtainedQualityItem(@NotNull String itemId) {
        return qualityItemCounts.containsKey(itemId);
    }

    /**
     * Gets all quality item counts.
     *
     * @return A map of item IDs to counts
     */
    @NotNull
    public Map<String, Integer> getQualityItemCounts() {
        return new ConcurrentHashMap<>(qualityItemCounts);
    }

    /**
     * Sets the quality item count (used for data loading).
     *
     * @param itemId The item ID
     * @param count The count
     */
    public void setQualityItemCount(@NotNull String itemId, int count) {
        if (count > 0) {
            qualityItemCounts.put(itemId, count);
        }
    }
}

