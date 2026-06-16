/**
 * Copyright (c) 2026 QPCrummer
 * This project is Licensed under <a href="https://github.com/Tater-Certified/Slumber/blob/main/LICENSE">MIT</a>
 */
package com.github.tatercertified.slumber.mixin;

import com.github.tatercertified.slumber.TickManagerInterface;
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
