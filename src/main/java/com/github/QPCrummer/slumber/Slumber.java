package com.github.QPCrummer.slumber;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class Slumber implements ModInitializer {
    //region Config variables
    final static String
            CONFIG_VERSION_KEY = "config-version",
            TOGGLE_KEY = "toggle",
            FREEZE_DELAY_SECONDS_KEY = "freeze-delay-seconds";

    private static final Path config = FabricLoader.getInstance().getConfigDir().resolve("slumber.properties");
    public static Properties properties = new Properties();
    public static String cfgver;
    public static int delay;
    //endregion

    protected static final Logger LOGGER = LoggerFactory.getLogger("Slumber");
    public static boolean enabled;
    private static final ScheduledExecutorService wait = Executors.newSingleThreadScheduledExecutor();
    private static volatile ScheduledFuture<?> task;

    @Override
    public void onInitialize() {
        //region Create Config
        if (Files.notExists(config)) {
            try {
                storecfg();
                LOGGER.info("Creating Slumber config");
            } catch (IOException e) {
                LOGGER.error("Config creation failed", e);
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                LOGGER.error("Config loading failed", e);
            }
            cfgver = properties.getProperty("config-version");
            if (!(Objects.equals(cfgver, "1.1"))) {
                try {
                    LOGGER.info("Updating Slumber config");
                    storecfg();
                } catch (IOException e) {
                    LOGGER.error("Config update failed", e);
                }
            } else {
                parse();
            }
        }
        //endregion

        //Register Command
        ToggleCommand.register();

        //region Events
        // Freezes the server on startup if toggled on.
        ServerLifecycleEvents.SERVER_STARTED.register(Slumber::freeze);

        // Join handler; unfreezes the server when a player joins.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var future = task;
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            unfreeze(server);
        });

        // Disconnect handler; freezes the server when no players are online.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (server.getCurrentPlayerCount() == 1) {
                task = wait.schedule(() -> freeze(server), delay, TimeUnit.SECONDS);
            }
        });
        //endregion
    }

    public static void storecfg() throws IOException {
        try (OutputStream output = Files.newOutputStream(config, StandardOpenOption.CREATE)) {
            if (!properties.containsKey(CONFIG_VERSION_KEY)) {
                properties.setProperty(CONFIG_VERSION_KEY, "1.0");
            }
            if (!properties.containsKey(FREEZE_DELAY_SECONDS_KEY)) {
                properties.setProperty(FREEZE_DELAY_SECONDS_KEY, "20");
            }
            if (!properties.containsKey(TOGGLE_KEY)) {
                properties.setProperty(TOGGLE_KEY, "false");
            }
            properties.store(output, null);
        }
        parse();
    }

    public static void loadcfg() throws IOException {
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        }
    }

    public static void parse() {
        cfgver = properties.getProperty(CONFIG_VERSION_KEY);
        delay = Integer.parseInt(properties.getProperty(FREEZE_DELAY_SECONDS_KEY));
        enabled = Boolean.parseBoolean(properties.getProperty(TOGGLE_KEY));
    }

    /**
     * Freezes the server if it isn't already frozen.
     */
    public static void freeze(@NotNull MinecraftServer server) {
        final ServerTickManager tickManager = server.getTickManager();
        if (enabled && !tickManager.isFrozen()) {
            server.getTickManager().setFrozen(true);
        }
    }

    /**
     * Unfreezes the server if it's frozen.
     */
    public static void unfreeze(@NotNull MinecraftServer server) {
        final ServerTickManager tickManager = server.getTickManager();
        if (tickManager.isFrozen()) {
            tickManager.setFrozen(false);
        }
    }
}
