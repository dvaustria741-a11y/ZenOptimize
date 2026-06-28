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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Shadow private Map<ParticleTextureSheet, Queue<Particle>> particles;

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
        for (Queue<Particle> q : particles.values()) {
            total += q.size();
            if (total >= ZenConfig.maxParticles) {
                cir.setReturnValue(null);
                return;
            }
        }
    }
}