package com.zenoptimize.mixin;

import com.zenoptimize.util.FpsTracker;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // GameRenderer.render signature changed in 1.21.2:
    //   OLD: render(float tickDelta, long startTime, boolean tick)
    //   NEW: render(RenderTickCounter tickCounter, boolean tick)
    //
    // NOTE: this mixin used to also run its own Thread.sleep()-based frame
    // limiter here (zenoptimize$capFramerate), stacked on top of the
    // mobileFrameCap value MinecraftClientMixin already applies via the
    // vanilla maxFps option. That meant two limiters were fighting every
    // frame, and Thread.sleep() has poor precision in this environment
    // (Android JVM via Zalith's caciocavallo translation layer), which is
    // what caused the stuttery "feels laggy" sensation even while nominally
    // hitting the target FPS. Letting the engine's own (much smoother)
    // maxFps limiter do this job alone fixes it — we only track frames here
    // now, for the dynamic render distance feature.
    @Inject(method = "render", at = @At("HEAD"))
    private void zenoptimize$trackFps(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        FpsTracker.recordFrame();
    }
}
