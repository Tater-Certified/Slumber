/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber.mixin;

import com.github.tatercertified.slumber.Slumber;
import com.github.tatercertified.slumber.TickManagerInterface;
import com.mojang.datafixers.DataFixer;
import java.net.Proxy;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServer {
    public DedicatedServerMixin(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, NotificationManager notificationManager) {
        super(serverThread, storageSource, packRepository, worldStem, gameRules, proxy, fixerUpper, services, levelLoadListener, propagatesCrashes, notificationManager);
    }

    @Inject(method = "initServer", at = @At("TAIL"))
    private void slumber$onServerStarted(CallbackInfoReturnable<Boolean> cir) {
        if (Slumber.enabled) {
            if (Slumber.sleepTickSpeed > 0) {
                ((TickManagerInterface)this.tickRateManager()).setFrozenNoPacket(false);
                Slumber.changeTickRateAction(true, this.tickRateManager());
            } else {
                ((TickManagerInterface)this.tickRateManager()).setFrozenNoPacket(true);
            }
        } else {
            ((TickManagerInterface)this.tickRateManager()).setFrozenNoPacket(false);
        }
        Slumber.calculateTimeElapsed(Slumber.enabled);
        if (Slumber.safeStarting) {
            Slumber.sendToDebugLogger("Safe Starting Finished; Continued Freeze: " + Slumber.enabled);
        }
    }
}
