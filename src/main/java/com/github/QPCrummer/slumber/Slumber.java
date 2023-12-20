package com.github.QPCrummer.slumber;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    final static String
            CONFIG_VERSION_KEY = "config-version",
            TOGGLE_KEY = "toggle",
            FREEZE_DELAY_SECONDS_KEY = "freeze-delay-seconds",
            SAFE_STARTING_KEY = "safe-starting",
            DEBUG_KEY = "debug-messages";

    private static final Path config = FabricLoader.getInstance().getConfigDir().resolve("slumber.properties");

    public static final Properties properties = new Properties();
    public static final String cfgver = "1.3";
    public static int delay;
    public static boolean
            enabled,
            safe_starting,
            debug;

    private static long
            beginningTime,
            endingTime;

    private static final ScheduledExecutorService wait = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    private static volatile ScheduledFuture<?> task;

    private static final Logger logger = LogManager.getLogger("Slumber");


    @Override
    public void onInitialize() {
        //Create Config
        if (Files.notExists(config)) {
            try {
                storecfg();
            } catch (IOException e) {
                logger.error("Config storing failed", e);
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                logger.error("Config creation failed", e);
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
            if (safe_starting) {
                ((TickManagerInterface)server.getTickManager()).setFrozenNoPacket(true);
                calculateTimeElapsed(true);
                sendToDebugLogger("Safe Starting Active");
            }
        });

        // Complete safe-starting and disable.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ((TickManagerInterface)server.getTickManager()).setFrozenNoPacket(enabled);
            calculateTimeElapsed(enabled);
            sendToDebugLogger("Safe Starting Finished; Continued Freeze: " + enabled);
        });

        // Join handler; unfreezes the server when a player joins.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (enabled) {
                var future = task;
                if (future != null && !future.isDone()) {
                    future.cancel(false);
                }
                freeze(false, server);
            }
        });

        // Disconnect handler; freezes the server when no players are online.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            //This less-than or equals one is because of FAPI weirdness
            if (enabled && server.getCurrentPlayerCount() <= 1) {
                task = wait.schedule(() -> {
                    if (server.getCurrentPlayerCount() == 0) {
                        server.execute(() -> freeze(true, server));
                    }
                }, delay, TimeUnit.SECONDS);
            }
        });
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
            properties.setProperty(CONFIG_VERSION_KEY, cfgver);
        }
        if (!properties.containsKey(SAFE_STARTING_KEY)) {
            properties.setProperty(SAFE_STARTING_KEY, "true");
        }
        if (!properties.containsKey(FREEZE_DELAY_SECONDS_KEY)) {
            properties.setProperty(FREEZE_DELAY_SECONDS_KEY, "20");
        }
        if (!properties.containsKey(TOGGLE_KEY)) {
            properties.setProperty(TOGGLE_KEY, "true");
        }
        if (!properties.containsKey(DEBUG_KEY)) {
            properties.setProperty(DEBUG_KEY, "false");
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
        enabled = Boolean.parseBoolean(properties.getProperty(TOGGLE_KEY));
        safe_starting = Boolean.parseBoolean(properties.getProperty(SAFE_STARTING_KEY));
        debug = Boolean.parseBoolean(properties.getProperty(DEBUG_KEY));
    }

    /**
     * Toggles the freezing of the server
     */
    public static void freeze(boolean frozen, MinecraftServer server) {
        ServerTickManager tickManager = server.getTickManager();
        sendToDebugLogger("Enabled:" + enabled + ", Game is Frozen: " + tickManager.isFrozen() + ", Trying to Freeze: " + frozen);
        if (enabled || tickManager.isFrozen() != frozen) {
            ((TickManagerInterface)tickManager).setFrozenNoPacket(frozen);
            calculateTimeElapsed(frozen);
            sendToDebugLogger("Frozen: " + frozen);
        }
    }

    public static void sendToDebugLogger(String message) {
        if (debug) {
            logger.info(message);
        }
    }

    private static void calculateTimeElapsed(boolean frozen) {
        if (debug) {
            if (frozen && beginningTime == 0) {
                beginningTime = System.currentTimeMillis();
            } else if (!frozen && beginningTime != 0 && endingTime == 0) {
                endingTime = System.currentTimeMillis();
            }

            if (endingTime != 0) {
                int timeElapsed = (int) ((endingTime - beginningTime) / 1000);
                sendToDebugLogger("Unfroze after " + timeElapsed + " seconds");
                beginningTime = 0;
                endingTime = 0;
            }
        }
    }
}
