/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Slumber {
    static final String
            CONFIG_VERSION_KEY = "config-version",
            TOGGLE_KEY = "toggle",
            FREEZE_DELAY_SECONDS_KEY = "freeze-delay-seconds",
            SAFE_STARTING_KEY = "safe-starting",
            SLEEP_TICK_SPEED = "sleep_tick_speed",
            SLEEP_AUTOSAVING = "sleep_autosaving",
            DEBUG_KEY = "debug-messages";

    private static final Path config = Path.of("").resolve("config/slumber.properties");

    public static final Properties PROPERTIES = new Properties();
    public static final String CFG_VER = "1.4";
    public static int delay = -1;
    public static float sleepTickSpeed;
    public static boolean
            enabled,
            safeStarting,
            autosaving,
            debug;

    private static long
            beginningTime,
            endingTime;

    public static final ScheduledExecutorService WAIT = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    public static volatile ScheduledFuture<?> task;

    private static final Logger LOGGER = LogManager.getLogger("Slumber");

    public static void onInitialize() {
        //Create Config
        if (Files.notExists(config)) {
            try {
                storecfg();
            } catch (IOException e) {
                LOGGER.error("Config storing failed", e);
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                LOGGER.error("Config creation failed", e);
            }
            if (!(Objects.equals(PROPERTIES.getProperty(CONFIG_VERSION_KEY), CFG_VER))) {
                PROPERTIES.setProperty(CONFIG_VERSION_KEY, CFG_VER);
                try {
                    storecfg();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                parse();
            }
        }
    }

    /**
     * Save the config
     */
    public static void storecfg() throws IOException {
        if (config.getParent() != null) {
            Files.createDirectories(config.getParent());
        }

        try (OutputStream output = Files.newOutputStream(config, StandardOpenOption.CREATE)) {
            fillDefaults();
            PROPERTIES.store(output, null);
        }
        parse();
    }

    /**
     * If the config value doesn't exist, set it to default
     */
    private static void fillDefaults() {
        checkProperty(CONFIG_VERSION_KEY, CFG_VER);
        checkProperty(SAFE_STARTING_KEY, "true");
        checkProperty(FREEZE_DELAY_SECONDS_KEY, "20");
        checkProperty(TOGGLE_KEY, "true");
        checkProperty(DEBUG_KEY, "false");
        checkProperty(SLEEP_TICK_SPEED, "0.0");
        checkProperty(SLEEP_AUTOSAVING, "false");
    }

    private static void checkProperty(String key, String defaultValue) {
        if (!PROPERTIES.containsKey(key)) {
            PROPERTIES.setProperty(key, defaultValue);
        }
    }

    /**
     * Loads the config
     */
    public static void loadcfg() throws IOException {
        try (InputStream input = Files.newInputStream(config)) {
            PROPERTIES.load(input);
        }
    }

    /**
     * Parses the config to convert into Objects
     */
    public static void parse() {
        fillDefaults();
        delay = Integer.parseInt(PROPERTIES.getProperty(FREEZE_DELAY_SECONDS_KEY));
        enabled = Boolean.parseBoolean(PROPERTIES.getProperty(TOGGLE_KEY));
        safeStarting = Boolean.parseBoolean(PROPERTIES.getProperty(SAFE_STARTING_KEY));
        debug = Boolean.parseBoolean(PROPERTIES.getProperty(DEBUG_KEY));
        sleepTickSpeed = Float.parseFloat(PROPERTIES.getProperty(SLEEP_TICK_SPEED));
        autosaving = Boolean.parseBoolean(PROPERTIES.getProperty(SLEEP_AUTOSAVING));
    }

    /**
     * Toggles the freezing of the server
     */
    public static void freeze(boolean frozen, MinecraftServer server) {
        ServerTickRateManager tickManager = server.tickRateManager();

        if (enabled) {
            if (sleepTickSpeed > 0) {
                changeTickRateAction(frozen, tickManager);
            } else {
                freezeServerAction(frozen, tickManager);
            }
            if (!autosaving) {
                ((MinecraftServerInterface)server).setAutoSave(!frozen);
            }
        }

        float realTPS = sleepTickSpeed > 0 ? tickManager.tickrate() : 0;
        sendToDebugLogger("Enabled:" + enabled + ", Frozen: " + tickManager.isFrozen() + ", Trying to Freeze: " + frozen + ", TPS: " + realTPS);
    }

    private static void freezeServerAction(boolean frozen, ServerTickRateManager tickManager) {
        if (tickManager.isFrozen() != frozen) {
            ((TickManagerInterface) tickManager).setFrozenNoPacket(frozen);
            calculateTimeElapsed(frozen);
            sendToDebugLogger("Frozen: " + frozen);
        }
    }

    public static void changeTickRateAction(boolean frozen, ServerTickRateManager tickManager) {
        if (frozen) {
            ((TickManagerInterface)tickManager).setTickRateNoPacket(sleepTickSpeed);
        } else {
            ((TickManagerInterface)tickManager).setTickRateNoPacket(20.0f);
        }
        sendToDebugLogger("TPS: " + tickManager.tickrate());
        calculateTimeElapsed(frozen);
    }

    public static void sendToDebugLogger(String message) {
        if (debug) {
            LOGGER.info(message);
        }
    }

    public static void calculateTimeElapsed(boolean frozen) {
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
