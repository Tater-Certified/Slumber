package com.github.QPCrummer.slumber;

import carpet.fakes.MinecraftServerInterface;
import carpet.helpers.ServerTickRateManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

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

public class Slumber implements ModInitializer {
    final static String CONFIG_VERSION_KEY = "config-version";
    final static String COMPLETE_FREEZE_KEY = "complete-freeze";
    final static String TOGGLE_KEY = "toggle";
    final static String FREEZE_DELAY_SECONDS_KEY = "freeze-delay-seconds";
    final static String SAFE_STARTING_KEY = "safe-starting";

    private static final Path config = FabricLoader.getInstance().getConfigDir().resolve("slumber.properties");

    public static Properties properties = new Properties();
    public static String cfgver = "1.1";
    public static int delay;
    public static boolean deepsleep;

    public static boolean enabled;
    public static boolean safe_starting;

    private static final ScheduledExecutorService wait = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    private static volatile ScheduledFuture<?> task;

    public static ServerTickRateManager tickManager;

    @Override
    public void onInitialize() {
        //Create Config
        if (Files.notExists(config)) {
            try {
                storecfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!(Objects.equals(properties.getProperty(CONFIG_VERSION_KEY), cfgver))) {
                properties.setProperty(CONFIG_VERSION_KEY, cfgver);
                try {
                    storecfg();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                parse();
            }
        }

        //Register Command
        SlumberCommand.register();

        // Freezes ticking during startup if safe-starting is enabled.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            createTickManager(server);
            if (safe_starting) {
                tickManager.setFrozenState(true, false);
            }
        });

        // Complete safe-starting and disable.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            freeze(enabled);
        });

        // Join handler; unfreezes the server when a player joins.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var future = task;
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            freeze(false);
        });

        // Disconnect handler; freezes the server when no players are online.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            //This less-than or equals one is because of FAPI weirdness
            if (server.getCurrentPlayerCount() <= 1) {
                task = wait.schedule(() -> {
                    if (server.getCurrentPlayerCount() == 0) {
                        server.execute(() -> freeze(true));
                    }
                }, delay, TimeUnit.SECONDS);
            }
        });
    }

    private static void createTickManager(MinecraftServer server) {
        tickManager = ((MinecraftServerInterface)server).getTickRateManager();
    }

    /**
     * Save the config
     */
    public static void storecfg() throws IOException {
        try (OutputStream output = Files.newOutputStream(config, StandardOpenOption.CREATE)) {
            fillDefaults();
            properties.store(output, null);
        }
        parse();
    }

    /**
     * If the config value doesn't exist, set it to default
     */
    private static void fillDefaults() {
        if (!properties.containsKey(CONFIG_VERSION_KEY)) {
            properties.setProperty(CONFIG_VERSION_KEY, "1.1");
        }
        if (!properties.containsKey(SAFE_STARTING_KEY)) {
            properties.setProperty(SAFE_STARTING_KEY, "true");
        }
        if (!properties.containsKey(FREEZE_DELAY_SECONDS_KEY)) {
            properties.setProperty(FREEZE_DELAY_SECONDS_KEY, "20");
        }
        if (!properties.containsKey(COMPLETE_FREEZE_KEY)) {
            properties.setProperty(COMPLETE_FREEZE_KEY, "false");
        }
        if (!properties.containsKey(TOGGLE_KEY)) {
            properties.setProperty(TOGGLE_KEY, "false");
        }
    }

    /**
     * Loads the config
     */
    public static void loadcfg() throws IOException {
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        }
    }

    /**
     * Parses the config to convert into Objects
     */
    public static void parse() {
        fillDefaults();
        delay = Integer.parseInt(properties.getProperty(FREEZE_DELAY_SECONDS_KEY));
        deepsleep = Boolean.parseBoolean(properties.getProperty(COMPLETE_FREEZE_KEY));
        enabled = Boolean.parseBoolean(properties.getProperty(TOGGLE_KEY));
        safe_starting = Boolean.parseBoolean(properties.getProperty(SAFE_STARTING_KEY));
    }

    /**
     * Toggles the freezing of the server
     */
    public static void freeze(boolean frozen) {
        System.out.println(enabled + ", " + tickManager.gameIsPaused() + ", " + frozen);
        if (!enabled || tickManager.gameIsPaused() == frozen) return;
        tickManager.setFrozenState(frozen, deepsleep);
    }
}
