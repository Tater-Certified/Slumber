package com.github.QPCrummer.slumber;

import carpet.helpers.TickSpeed;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

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

    private static final Path config = FabricLoader.getInstance().getConfigDir().resolve("slumber.properties");
    public static Properties properties = new Properties();
    public static String cfgver;
    public static int delay;
    public static boolean deepsleep;

    public static boolean enabled;

    private static final ScheduledExecutorService wait = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    private static volatile ScheduledFuture<?> task;

    @Override
    public void onInitialize() {
        //Create Config
        if (Files.notExists(config)) {
            try {
                storecfg();
                System.out.println("Creating Slumber config");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cfgver = properties.getProperty("config-version");
            if (!(Objects.equals(cfgver, "1.0"))) {
                try {
                    storecfg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Updating Slumber config");
            } else {
                parse();
            }
        }

        //Register Command
        ToggleCommand.register();

        // Freezes ticking during startup.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> TickSpeed.setFrozenState(true, false));

        // If it isn't intended to be enabled, unfreeze the server.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!enabled) {
                unfreeze();
            } else if (deepsleep) {
                TickSpeed.setFrozenState(true, true);
            }
        });

        // Join handler; unfreezes the server when a player joins.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var future = task;
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            unfreeze();
        });

        // Disconnect handler; freezes the server when no players are online.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (server.getCurrentPlayerCount() <= 1) {
                task = wait.schedule(() -> {
                    if (server.getCurrentPlayerCount() == 0) {
                        server.execute(Slumber::freeze);
                    }
                }, delay, TimeUnit.SECONDS);
            }
        });
    }

    public static void storecfg() throws IOException {
        try (OutputStream output = Files.newOutputStream(config, StandardOpenOption.CREATE)) {
            fillDefaults();
            properties.store(output, null);
        }
        parse();
    }

    private static void fillDefaults() {
        if (!properties.containsKey(CONFIG_VERSION_KEY)) {
            properties.setProperty(CONFIG_VERSION_KEY, "1.0");
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

    public static void loadcfg() throws IOException {
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        }
    }

    public static void parse() {
        fillDefaults();
        cfgver = properties.getProperty(CONFIG_VERSION_KEY);
        delay = Integer.parseInt(properties.getProperty(FREEZE_DELAY_SECONDS_KEY));
        deepsleep = Boolean.parseBoolean(properties.getProperty(COMPLETE_FREEZE_KEY));
        enabled = Boolean.parseBoolean(properties.getProperty(TOGGLE_KEY));
    }

    /**
     * Freezes the server if it isn't already frozen.
     */
    public static void freeze() {
        if (enabled && !TickSpeed.isPaused()) {
            TickSpeed.setFrozenState(true, deepsleep);
        }
    }

    /**
     * Unfreezes the server if it's frozen.
     */
    public static void unfreeze() {
        if (TickSpeed.isPaused()) {
            TickSpeed.setFrozenState(false, false);
        }
    }
}
