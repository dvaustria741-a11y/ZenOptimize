package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Smooth Camera: eases the rendered camera rotation toward the real,
 * instantly-responsive yaw/pitch each frame instead of snapping straight to
 * it, which removes the small visual jitter you get from uneven frame
 * timing (especially noticeable on mobile).
 *
 * This intentionally does NOT touch the player's actual aim/yaw/pitch —
 * only what the camera renders — so input/aim itself has zero added delay.
 * The smoothing uses frame-time-independent exponential easing (not a flat
 * per-frame lerp), so it behaves the same whether you're at 30fps or
 * 120fps, and it snaps back onto the real rotation once it's within a
 * fraction of a degree so there's never a lasting offset.
 *
 * Tuned via ZenConfig.smoothCameraSpeed — higher = snappier/less smoothing
 * (less perceived delay), lower = smoother/more lag. Disabled entirely via
 * ZenConfig.smoothCamera, in which case the real rotation passes straight
 * through untouched.
 */
@Mixin(Camera.class)
public class CameraMixin {

    private float zenoptimize$smoothedYaw;
    private float zenoptimize$smoothedPitch;
    private boolean zenoptimize$initialized = false;
    private long zenoptimize$lastNanos = 0L;
    private float zenoptimize$alpha = 1f;

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float zenoptimize$smoothYaw(float yaw) {
        if (!ZenConfig.smoothCamera) {
            zenoptimize$smoothedYaw = yaw;
            zenoptimize$initialized = false;
            return yaw;
        }
        zenoptimize$advanceTime();
        if (!zenoptimize$initialized) {
            zenoptimize$smoothedYaw = yaw;
            zenoptimize$initialized = true;
            return yaw;
        }
        float delta = MathHelper.wrapDegrees(yaw - zenoptimize$smoothedYaw);
        if (Math.abs(delta) < 0.02f) {
            zenoptimize$smoothedYaw = yaw;
        } else {
            zenoptimize$smoothedYaw += delta * zenoptimize$alpha;
        }
        return zenoptimize$smoothedYaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float zenoptimize$smoothPitch(float pitch) {
        if (!ZenConfig.smoothCamera || !zenoptimize$initialized) {
            zenoptimize$smoothedPitch = pitch;
            return pitch;
        }
        float delta = pitch - zenoptimize$smoothedPitch;
        if (Math.abs(delta) < 0.02f) {
            zenoptimize$smoothedPitch = pitch;
        } else {
            zenoptimize$smoothedPitch += delta * zenoptimize$alpha;
        }
        return zenoptimize$smoothedPitch;
    }

    private void zenoptimize$advanceTime() {
        long now = System.nanoTime();
        if (zenoptimize$lastNanos == 0L) {
            zenoptimize$lastNanos = now;
            zenoptimize$alpha = 1f;
            return;
        }
        double deltaSeconds = (now - zenoptimize$lastNanos) / 1_000_000_000.0;
        zenoptimize$lastNanos = now;
        if (deltaSeconds > 0.1) deltaSeconds = 0.1; // clamp so a stutter/pause can't cause a big camera jump
        zenoptimize$alpha = (float) (1.0 - Math.exp(-deltaSeconds * ZenConfig.smoothCameraSpeed));
    }
}
