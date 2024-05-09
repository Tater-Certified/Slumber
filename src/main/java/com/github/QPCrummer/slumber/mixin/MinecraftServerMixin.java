package com.github.QPCrummer.slumber.mixin;

import com.github.QPCrummer.slumber.MinecraftServerInterface;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerInterface {
    private boolean autosaving = true;
    @Override
    public void setAutoSave(boolean enable) {
        autosaving = enable;
    }

    @WrapWithCondition(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;ticksUntilAutosave:I", ordinal = 0))
    private boolean checkAutoSaving() {
        return autosaving;
    }
}
