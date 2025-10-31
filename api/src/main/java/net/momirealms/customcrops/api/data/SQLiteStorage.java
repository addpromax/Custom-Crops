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
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;

/**
 * SQLite implementation of database storage.
 */
public class SQLiteStorage implements DatabaseStorage {

    private final BukkitCustomCropsPlugin plugin;
    private final File databaseFile;
    private final Gson gson;
    private Connection connection;

    public SQLiteStorage(BukkitCustomCropsPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = plugin.getDataDirectory().resolve("harvest-data.db").toFile();
        this.gson = new Gson();
    }

    @Override
    public void initialize() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create database file if not exists
            if (!databaseFile.getParentFile().exists()) {
                databaseFile.getParentFile().mkdirs();
            }
            
            // Establish connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            
            // Create table
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS harvest_data (" +
                    "uuid TEXT PRIMARY KEY," +
                    "harvests TEXT," +
                    "quality_items TEXT," +
                    "total_harvests INTEGER," +
                    "last_updated INTEGER" +
                    ")"
                );
            }
            
            plugin.getPluginLogger().info("SQLite storage initialized successfully");
        } catch (Exception e) {
            plugin.getPluginLogger().severe("Failed to initialize SQLite storage", e);
        }
    }

    @Override
    @Nullable
    public PlayerHarvestData loadPlayerData(@NotNull UUID playerId) {
        String sql = "SELECT * FROM harvest_data WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        String sql = "INSERT OR REPLACE INTO harvest_data (uuid, harvests, quality_items, total_harvests, last_updated) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        if (connection != null) {
            try {
                connection.close();
                plugin.getPluginLogger().info("SQLite connection closed");
            } catch (SQLException e) {
                plugin.getPluginLogger().severe("Failed to close SQLite connection", e);
            }
        }
    }

    @Override
    public String getType() {
        return "SQLite";
    }
}

