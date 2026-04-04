package com.github.QPCrummer.slumber.mixin;

import com.github.QPCrummer.slumber.TickManagerInterface;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerTickRateManager.class)
public abstract class ServerTickRateManagerMixin extends TickRateManager implements TickManagerInterface {

    @Override
    public void setFrozenNoPacket(boolean frozen) {
        super.setFrozen(frozen);
    }

    @Override
    public void setTickRateNoPacket(float tickRate) {
        super.setTickRate(tickRate);
    }
}
