/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber.mixin;

import com.github.tatercertified.slumber.MinecraftServerInterface;
import com.github.tatercertified.slumber.Slumber;
import com.github.tatercertified.slumber.TickManagerInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerInterface {
    @Shadow private int ticksUntilAutosave;
    private boolean autosaving = true;
    @Override
    public void setAutoSave(boolean enable) {
        autosaving = enable;
    }

    @WrapOperation(method = "tickServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;ticksUntilAutosave:I", ordinal = 0, opcode = Opcodes.GETFIELD))
    private int slumber$checkAutoSaving(MinecraftServer instance, Operation<Integer> original) {
        if (autosaving) {
            return this.ticksUntilAutosave;
        }
        return original.call(instance);
    }

    @Redirect(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;pauseWhenEmptySeconds()I", ordinal = 0))
    private int slumber$removeMojangImplementation(MinecraftServer instance) {
        return 0;
    }

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;OVERLOADED_THRESHOLD_NANOS:J", opcode = Opcodes.PUTSTATIC))
    private static void slumber$initMod(CallbackInfo ci) {
        Slumber.onInitialize();
    }

    @Inject(method = "spin", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicReference;set(Ljava/lang/Object;)V"))
    private static <S> void slumber$serverStarting(Function<Thread, S> factory, CallbackInfoReturnable<S> cir, @Local(name = "server") MinecraftServer server) {
        if (Slumber.safeStarting) {
            ((TickManagerInterface)server.tickRateManager()).setFrozenNoPacket(true);
            Slumber.calculateTimeElapsed(true);
            Slumber.sendToDebugLogger("Safe Starting Active");
        }
    }
}
