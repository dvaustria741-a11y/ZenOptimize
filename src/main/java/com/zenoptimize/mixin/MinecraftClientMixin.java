package com.zenoptimize.mixin;

import com.zenoptimize.ZenOptimizeMod;
import com.zenoptimize.config.ZenConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    // Apply mobile-friendly defaults on first world join
    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void zenoptimize$onJoinWorld(net.minecraft.client.world.ClientWorld world, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient)(Object)this;

        // Ensure max framerate option respects our mobile cap if user hasnt set it already
        if (ZenConfig.mobileFrameCap > 0) {
            int currentMax = client.options.getMaxFps().getValue();
            // Only cap downward if user has uncapped (256 = unlimited in MC)
            if (currentMax >= 255) {
                client.options.getMaxFps().setValue(ZenConfig.mobileFrameCap);
                ZenOptimizeMod.LOGGER.info("[ZenOptimize] Applied mobile frame cap: {}fps", ZenConfig.mobileFrameCap);
            }
        }
    }
}