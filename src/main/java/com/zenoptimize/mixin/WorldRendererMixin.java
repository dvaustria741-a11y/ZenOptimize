package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import com.zenoptimize.util.FpsTracker;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // Reduce max entity render distance when FPS is low
    @Inject(
        method = "isRenderingReady",
        at = @At("HEAD"),
        cancellable = true
    )
    private void zenoptimize$fastReadyCheck(net.minecraft.util.math.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // No-op — hook point reserved for future culling
    }
}