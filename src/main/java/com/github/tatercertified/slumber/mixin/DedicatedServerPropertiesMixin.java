/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber.mixin;

import java.util.Properties;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
