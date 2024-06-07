/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

package net.momirealms.customcrops.api.event;

import net.momirealms.customcrops.api.mechanic.misc.Reason;
import net.momirealms.customcrops.api.mechanic.world.level.WorldGlass;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An event that triggered when breaking greenhouse glass
 */
public class GreenhouseGlassBreakEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Location location;
    private final Entity entity;
    private final Reason reason;
    private final WorldGlass glass;

    public GreenhouseGlassBreakEvent(
            @Nullable Entity entity,
            @NotNull Location location,
            @NotNull WorldGlass glass,
            @NotNull Reason reason
    ) {
        this.entity = entity;
        this.location = location;
        this.reason = reason;
        this.glass = glass;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Get the glass location
     *
     * @return location
     */
    @NotNull
    public Location getLocation() {
        return location;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @Nullable
    public Player getPlayer() {
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    @NotNull
    public Reason getReason() {
        return reason;
    }

    /**
     * Get the glass data
     *
     * @return glass data
     */
    @NotNull
    public WorldGlass getGlass() {
        return glass;
    }
}