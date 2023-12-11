package com.github.QPCrummer.slumber;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

import static com.github.QPCrummer.slumber.Slumber.*;
import static net.minecraft.server.command.CommandManager.argument;

public class ToggleCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> dispatcher.register(CommandManager.literal("serv-freeze")

                .executes(context -> {
                    final ServerTickManager tickManager = context.getSource().getServer().getTickManager();

                    boolean frozen = tickManager.isFrozen();
                    context.getSource().sendFeedback(
                            () -> Text.of("Enabled: " + enabled + ", Frozen: " + frozen),
                            false);
                    return 1;
                })

                .then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
                    boolean freezeenabled = BoolArgumentType.getBool(context, "enabled");
                    ServerCommandSource source = context.getSource();

                    enabled = freezeenabled;
                    properties.setProperty(TOGGLE_KEY, Boolean.toString(freezeenabled));
                    if (freezeenabled) {
                        if (source.getServer().getCurrentPlayerCount() == 0) {
                            freeze(source.getServer());
                        }
                    } else {
                        unfreeze(source.getServer());
                    }
                    source.sendFeedback(() -> Text.of("Server Freezing is now set to " + freezeenabled), true);

                    try {
                        Slumber.storecfg();
                    } catch (IOException e) {
                        LOGGER.error("Config update failed", e);
                    }
                    return 1;
                }))));
    }
}
