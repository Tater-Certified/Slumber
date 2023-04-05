package com.github.QPCrummer.slumber;

import carpet.helpers.TickSpeed;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

import static com.github.QPCrummer.slumber.Slumber.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SlumberCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            var slumber = dispatcher.register(literal("slumber")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(SlumberCommand::status)

                    .then(argument("enabled", BoolArgumentType.bool())
                            .executes(SlumberCommand::set)));

            dispatcher.register(literal("serv-freeze")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(SlumberCommand::status)
                    .redirect(slumber)
            );
        });
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        boolean frozen = TickSpeed.isPaused();
        boolean deeply = TickSpeed.deeplyFrozen();
        context.getSource().sendFeedback(Text.of("Enabled: " + enabled +
                ", Frozen: " + frozen + ", Deeply: " + deeply), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<ServerCommandSource> context) {
        boolean enabledArg = BoolArgumentType.getBool(context, "enabled");
        ServerCommandSource source = context.getSource();

        enabled = enabledArg;
        properties.setProperty(TOGGLE_KEY, Boolean.toString(enabledArg));
        if (enabledArg) {
            if (source.getServer().getCurrentPlayerCount() == 0) {
                freeze();
            }
        } else {
            unfreeze();
        }

        source.sendFeedback(Text.of("Server Freezing is now set to " + enabledArg), true);

        try {
            Slumber.storecfg();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }
}
