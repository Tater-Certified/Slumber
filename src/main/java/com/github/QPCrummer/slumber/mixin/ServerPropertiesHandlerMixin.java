package com.github.QPCrummer.slumber.mixin;

import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Properties;

@Mixin(ServerPropertiesHandler.class)
public abstract class ServerPropertiesHandlerMixin extends AbstractPropertiesHandler<ServerPropertiesHandler> {
    public ServerPropertiesHandlerMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/ServerPropertiesHandler;intAccessor(Ljava/lang/String;I)Lnet/minecraft/server/dedicated/AbstractPropertiesHandler$PropertyAccessor;", ordinal = 6))
    private PropertyAccessor redirectMojangImpl(ServerPropertiesHandler instance, String s, int i) {
        return this.accessor(s, ServerPropertiesHandlerMixin::dummyImpl, 0);
    }

    private static Integer dummyImpl(String s) {
        return 0;
    }
}
