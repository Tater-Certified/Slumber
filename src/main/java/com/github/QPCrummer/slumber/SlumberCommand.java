package com.github.QPCrummer.slumber;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.io.IOException;

import static com.github.QPCrummer.slumber.Slumber.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SlumberCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            dispatcher.register(literal("slumber")
                    .requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK))
                    .executes(SlumberCommand::status)

                    .then(argument("enabled", BoolArgumentType.bool())
                            .executes(SlumberCommand::set)));
        });
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        boolean frozen = context.getSource().getServer().tickRateManager().isFrozen();

        context.getSource().sendSuccess(() -> Component.nullToEmpty("Enabled: " + enabled +
                ", Frozen: " + frozen), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean enabledArg = BoolArgumentType.getBool(context, "enabled");

        enabled = enabledArg;
        properties.setProperty(TOGGLE_KEY, Boolean.toString(enabledArg));

        freeze(enabledArg && source.getServer().getPlayerCount() == 0, source.getServer());

        source.sendSuccess(() -> Component.nullToEmpty("Server Freezing is now set to " + enabledArg), true);

        try {
            Slumber.storecfg();
        } catch (IOException e) {
            sendToDebugLogger("Failed to save config: " + e);
        }

        return Command.SINGLE_SUCCESS;
    }
}
