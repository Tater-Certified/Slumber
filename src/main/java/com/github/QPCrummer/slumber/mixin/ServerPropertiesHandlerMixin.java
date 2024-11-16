package com.github.QPCrummer.slumber.mixin;

import com.github.QPCrummer.slumber.Slumber;
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

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/ServerPropertiesHandler;getInt(Ljava/lang/String;I)I", ordinal = 13))
    private int redirectMojangImplemenation(ServerPropertiesHandler instance, String s, int i) {
        // TODO Redirect this to Slumber delay
        //Slumber.delay = this.getInt(s, -1);
        return 0;
    }
}
