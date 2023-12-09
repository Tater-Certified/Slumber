package com.github.QPCrummer.slumber.mixin;

import com.github.QPCrummer.slumber.TickManagerInterface;
import net.minecraft.server.ServerTickManager;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerTickManager.class)
public abstract class ServerTickManagerMixin extends TickManager implements TickManagerInterface {

    @Override
    public void setFrozenNoPacket(boolean frozen) {
        super.setFrozen(frozen);
    }
}
