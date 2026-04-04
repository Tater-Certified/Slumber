package com.github.QPCrummer.slumber.mixin;

import com.github.QPCrummer.slumber.MinecraftServerInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
