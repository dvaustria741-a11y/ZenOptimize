package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow private Map<ParticleTextureSheet, ?> particles;

    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void zenoptimize$limitParticles(
            ParticleEffect parameters,
            double x, double y, double z,
            double vx, double vy, double vz,
            CallbackInfoReturnable<Particle> cir) {
        if (!ZenConfig.limitParticles) return;
        int total = 0;
        for (Object bucket : particles.values()) {
            total += zenoptimize$sizeOf(bucket);
            if (total >= ZenConfig.maxParticles) {
                cir.setReturnValue(null);
                return;
            }
        }
    }

    /**
     * Counts the particles in a single texture-sheet bucket without assuming its
     * concrete type. Mojang changed this internal storage type away from
     * {@code Queue<Particle>} in 1.21.11, which previously caused a
     * ClassCastException here. Casting to a specific collection interface is
     * avoided entirely so this keeps working even if the internal type changes
     * again in a future version.
     */
    private static int zenoptimize$sizeOf(Object bucket) {
        if (bucket == null) return 0;
        if (bucket instanceof Collection<?> collection) {
            return collection.size();
        }
        try {
            Method sizeMethod = bucket.getClass().getMethod("size");
            Object result = sizeMethod.invoke(bucket);
            if (result instanceof Integer count) {
                return count;
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through to manual iteration below.
        }
        if (bucket instanceof Iterable<?> iterable) {
            int count = 0;
            for (Object ignored : iterable) {
                count++;
            }
            return count;
        }
        return 0;
    }
}
