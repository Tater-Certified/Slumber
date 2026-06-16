/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber;

import static com.github.tatercertified.slumber.Slumber.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameModeCommand;

public class SlumberCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("slumber")
                .requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK))
                .executes(SlumberCommand::status)

                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(SlumberCommand::set)));
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
        PROPERTIES.setProperty(TOGGLE_KEY, Boolean.toString(enabledArg));

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
