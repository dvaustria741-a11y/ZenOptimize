package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import com.zenoptimize.util.FpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Sodium-safe: EntityRenderDispatcher is not touched by Sodium/Lithium/Iris
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    // When FPS is critically low, cut entity render distance by up to 50%
    // to reduce entity rendering overhead — biggest lag source on low-end devices
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void zenoptimize$cullDistantEntities(
            E entity, net.minecraft.client.render.Frustum frustum,
            double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {
        if (!ZenConfig.skipOffscreenEntities) return;
        if (!FpsTracker.isBelowTarget(ZenConfig.targetFps)) return;

        // Skip entities beyond half the configured distance when FPS is suffering
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double maxDist = client.options.getEntityDistanceScaling().getValue()
                * 64.0 * 0.5; // 50% of normal entity range
        double dx = x - entity.getX();
        double dy = y - entity.getY();
        double dz = z - entity.getZ();
        if ((dx * dx + dy * dy + dz * dz) > maxDist * maxDist) {
            cir.setReturnValue(false);
        }
    }
}