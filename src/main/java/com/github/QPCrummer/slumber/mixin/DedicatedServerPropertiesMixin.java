package com.github.QPCrummer.slumber.mixin;

import net.minecraft.server.dedicated.Settings;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin extends Settings<DedicatedServerProperties> {
    public DedicatedServerPropertiesMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;getMutable(Ljava/lang/String;I)Lnet/minecraft/server/dedicated/Settings$MutableValue;", ordinal = 6))
    private MutableValue<Integer> slumber$redirectMojangImpl(DedicatedServerProperties instance, String s, int i) {
        return this.getMutable(s, 0);
    }
}
