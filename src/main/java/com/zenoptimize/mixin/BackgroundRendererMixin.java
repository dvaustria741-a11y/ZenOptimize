package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import com.zenoptimize.util.FpsTracker;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    // When FPS is critically low and reduceFogDensity is on,
    // push the fog start further to cut fog geometry overhead.
    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void zenoptimize$lightFog(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            net.minecraft.client.render.FogShape fogShape,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfoReturnable<Fog> cir) {
        if (!ZenConfig.reduceFogDensity) return;
        if (!FpsTracker.isBelowTarget(ZenConfig.targetFps)) return;
        Fog original = cir.getReturnValue();
        if (original == null) return;
        // Extend fog start by 20% to reduce overdraw cost
        float newStart = original.start() * 1.20f;
        float newEnd   = Math.max(original.end(), newStart + 8f);
        cir.setReturnValue(new Fog(newStart, newEnd, original.shape(), original.red(), original.green(), original.blue(), original.alpha()));
    }
}