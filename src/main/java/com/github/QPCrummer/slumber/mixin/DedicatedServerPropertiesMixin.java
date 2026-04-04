package com.github.QPCrummer.slumber.mixin;

import net.minecraft.server.dedicated.Settings;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings.MutableValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin extends Settings<DedicatedServerProperties> {
    public DedicatedServerPropertiesMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;intAccessor(Ljava/lang/String;I)Lnet/minecraft/server/dedicated/Settings$MutableValue;", ordinal = 6))
    private MutableValue redirectMojangImpl(DedicatedServerProperties instance, String s, int i) {
        return this.getMutable(s, DedicatedServerPropertiesMixin::dummyImpl, 0);
    }

    private static Integer dummyImpl(String s) {
        return 0;
    }
}
