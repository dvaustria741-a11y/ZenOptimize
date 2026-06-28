package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import com.zenoptimize.util.FpsTracker;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private long zenoptimize$lastFrameTime = 0L;

    @Inject(method = "render", at = @At("HEAD"))
    private void zenoptimize$trackFps(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        FpsTracker.recordFrame();
        zenoptimize$capFramerate();
    }

    private void zenoptimize$capFramerate() {
        int cap = ZenConfig.mobileFrameCap;
        if (cap <= 0) return;
        if (zenoptimize$lastFrameTime == 0L) {
            zenoptimize$lastFrameTime = System.nanoTime();
            return;
        }
        long targetNs = 1_000_000_000L / cap;
        long now = System.nanoTime();
        long elapsed = now - zenoptimize$lastFrameTime;
        if (elapsed < targetNs) {
            long sleepNs = targetNs - elapsed;
            try {
                Thread.sleep(sleepNs / 1_000_000L, (int)(sleepNs % 1_000_000L));
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        zenoptimize$lastFrameTime = System.nanoTime();
    }
}