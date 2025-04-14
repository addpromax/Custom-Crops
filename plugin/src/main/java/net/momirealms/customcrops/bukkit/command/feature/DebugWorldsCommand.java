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

package net.momirealms.customcrops.bukkit.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.core.world.CustomCropsWorld;
import net.momirealms.customcrops.bukkit.command.BukkitCommandFeature;
import net.momirealms.customcrops.common.command.CustomCropsCommandManager;
import net.momirealms.customcrops.common.locale.MessageConstants;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.Optional;

public class DebugWorldsCommand extends BukkitCommandFeature<CommandSender> {

    public DebugWorldsCommand(CustomCropsCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .handler(context -> {
                    int worldCount = 0;
                    for (World world : Bukkit.getWorlds()) {
                        Optional<CustomCropsWorld<?>> optional = BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(world);
                        if (optional.isPresent()) {
                            worldCount++;
                            CustomCropsWorld<?> w = optional.get();
                            handleFeedback(context, MessageConstants.COMMAND_DEBUG_WORLDS_SUCCESS,
                                    Component.text(world.getName()), Component.text(w.loadedRegions().length), Component.text(w.loadedChunks().length), Component.text(w.lazyChunks().length)
                            );
                        }
                    }
                    if (worldCount == 0) {
                        handleFeedback(context, MessageConstants.COMMAND_DEBUG_WORLDS_FAILURE);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_worlds";
    }
}
