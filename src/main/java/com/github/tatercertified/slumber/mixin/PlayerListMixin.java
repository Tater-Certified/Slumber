/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber.mixin;

import com.github.tatercertified.slumber.Slumber;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void slumber$onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        if (Slumber.enabled) {
            var future = Slumber.task;
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            Slumber.freeze(false, this.server);
        }
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void slumber$onPlayerLeave(ServerPlayer player, CallbackInfo ci) {
        if (Slumber.enabled && this.server.getPlayerCount() == 0) {
            Slumber.task = Slumber.WAIT.schedule(() -> {
                if (this.server.getPlayerCount() == 0) {
                    this.server.execute(() -> Slumber.freeze(true, this.server));
                }
            }, Slumber.delay, TimeUnit.SECONDS);
        }
    }
}
