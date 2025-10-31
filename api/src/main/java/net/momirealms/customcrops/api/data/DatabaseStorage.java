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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Interface for database storage operations.
 */
public interface DatabaseStorage {

    /**
     * Initializes the database connection and creates tables.
     */
    void initialize();

    /**
     * Loads player harvest data from database.
     *
     * @param playerId The player's UUID
     * @return The player's harvest data, or null if not found
     */
    @Nullable
    PlayerHarvestData loadPlayerData(@NotNull UUID playerId);

    /**
     * Saves player harvest data to database.
     *
     * @param data The player's harvest data
     */
    void savePlayerData(@NotNull PlayerHarvestData data);

    /**
     * Closes the database connection.
     */
    void close();

    /**
     * Gets the storage type name.
     *
     * @return The storage type
     */
    String getType();
}

