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
import net.momirealms.customcrops.api.core.ConfigManager;
import net.momirealms.customcrops.api.core.world.CustomCropsWorld;
import net.momirealms.customcrops.api.integration.SeasonProvider;
import net.momirealms.customcrops.bukkit.command.BukkitCommandFeature;
import net.momirealms.customcrops.common.command.CustomCropsCommandManager;
import net.momirealms.customcrops.common.locale.MessageConstants;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.Optional;

public class SetDateCommand extends BukkitCommandFeature<CommandSender> {

    public SetDateCommand(CustomCropsCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("world", WorldParser.worldParser())
                .required("date", IntegerParser.integerParser())
                .handler(context -> {
                    World world = context.get("world");
                    SeasonProvider provider = BukkitCustomCropsPlugin.getInstance().getWorldManager().seasonProvider();
                    if (provider.identifier().equals("CustomCrops")) {
                        Optional<CustomCropsWorld<?>> optionalWorld = BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(world);
                        if (optionalWorld.isPresent()) {
                            CustomCropsWorld<?> customCropsWorld = optionalWorld.get();
                            int date = context.get("date");
                            if (date < 1 || date > customCropsWorld.setting().seasonDuration()) {
                                handleFeedback(context, MessageConstants.COMMAND_SET_DATE_FAILURE_INVALID, Component.text(world.getName()), Component.text(date));
                                return;
                            }
                            if (customCropsWorld.setting().enableSeason()) {
                                if (ConfigManager.syncSeasons()) {
                                    if (ConfigManager.referenceWorld().equals(world.getName())) {
                                        customCropsWorld.extraData().setDate(date);
                                        handleFeedback(context, MessageConstants.COMMAND_SET_DATE_SUCCESS, Component.text(world.getName()), Component.text(date));
                                    } else {
                                        handleFeedback(context, MessageConstants.COMMAND_SET_DATE_FAILURE_REFERENCE, Component.text(world.getName()));
                                    }
                                } else {
                                    customCropsWorld.extraData().setDate(date);
                                    handleFeedback(context, MessageConstants.COMMAND_SET_DATE_SUCCESS, Component.text(world.getName()), Component.text(date));
                                }
                            } else {
                                handleFeedback(context, MessageConstants.COMMAND_SET_DATE_FAILURE_DISABLE, Component.text(world.getName()));
                            }
                        } else {
                            handleFeedback(context, MessageConstants.COMMAND_SET_DATE_FAILURE_DISABLE, Component.text(world.getName()));
                        }
                    } else {
                        handleFeedback(context, MessageConstants.COMMAND_SET_DATE_FAILURE_OTHER, Component.text(world.getName()), Component.text(provider.identifier()));
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "set_date";
    }
}
