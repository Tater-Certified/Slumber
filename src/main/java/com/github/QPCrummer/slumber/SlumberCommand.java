package com.github.QPCrummer.slumber;

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
            dispatcher.register(literal("slumber")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(SlumberCommand::status)

                    .then(argument("enabled", BoolArgumentType.bool())
                            .executes(SlumberCommand::set)));
        });
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        boolean frozen = context.getSource().getServer().getTickManager().isFrozen();

        context.getSource().sendFeedback(() -> Text.of("Enabled: " + enabled +
                ", Frozen: " + frozen), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean enabledArg = BoolArgumentType.getBool(context, "enabled");

        enabled = enabledArg;
        properties.setProperty(TOGGLE_KEY, Boolean.toString(enabledArg));

        freeze(enabledArg && source.getServer().getCurrentPlayerCount() == 0, source.getServer());

        source.sendFeedback(() -> Text.of("Server Freezing is now set to " + enabledArg), true);

        try {
            Slumber.storecfg();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }
}
