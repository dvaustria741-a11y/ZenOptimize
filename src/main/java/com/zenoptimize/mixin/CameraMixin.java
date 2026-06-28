package com.zenoptimize.mixin;

import com.zenoptimize.config.ZenConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
 *
 * Camera#update() calls setRotation() TWICE in one frame when in
 * third-person front view: once with the entity's real yaw/pitch, then
 * again flipped 180°/negated for the front-facing inversion. We only want
 * to ease the first (real) call — the second call is a deliberate flip,
 * not a rotation to smooth into — so we track call order per frame and
 * derive the second call's result directly from the first call's smoothed
 * result using the same flip, instead of smoothing the ~180° jump itself
 * (which is what broke third-person front view previously).
 */
@Mixin(Camera.class)
public class CameraMixin {

    private float zenoptimize$smoothedYaw;
    private float zenoptimize$smoothedPitch;
    private boolean zenoptimize$initialized = false;
    private long zenoptimize$lastNanos = 0L;
    private float zenoptimize$alpha = 1f;

    private int zenoptimize$callsThisFrame = 0;
    private float zenoptimize$primaryRawYaw;
    private float zenoptimize$primarySmoothedYaw;
    private float zenoptimize$primarySmoothedPitch;

    @Inject(method = "update", at = @At("HEAD"))
    private void zenoptimize$onUpdateStart(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                            boolean inverseView, float tickDelta, CallbackInfo ci) {
        zenoptimize$callsThisFrame = 0;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float zenoptimize$smoothYaw(float yaw) {
        zenoptimize$callsThisFrame++;

        if (!ZenConfig.smoothCamera) {
            zenoptimize$smoothedYaw = yaw;
            zenoptimize$initialized = false;
            return yaw;
        }

        if (zenoptimize$callsThisFrame == 1) {
            zenoptimize$advanceTime();
            zenoptimize$primaryRawYaw = yaw;
            if (!zenoptimize$initialized) {
                zenoptimize$smoothedYaw = yaw;
                zenoptimize$initialized = true;
            } else {
                float delta = MathHelper.wrapDegrees(yaw - zenoptimize$smoothedYaw);
                zenoptimize$smoothedYaw = Math.abs(delta) < 0.02f
                        ? yaw
                        : zenoptimize$smoothedYaw + delta * zenoptimize$alpha;
            }
            zenoptimize$primarySmoothedYaw = zenoptimize$smoothedYaw;
            return zenoptimize$smoothedYaw;
        }

        if (zenoptimize$callsThisFrame == 2) {
            // Third-person front view's flip — apply the same flip to our
            // already-smoothed primary result instead of smoothing this jump.
            float rawDelta = MathHelper.wrapDegrees(yaw - zenoptimize$primaryRawYaw);
            return zenoptimize$primarySmoothedYaw + rawDelta;
        }

        // Unexpected extra call — don't guess, pass the real value through.
        return yaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float zenoptimize$smoothPitch(float pitch) {
        if (!ZenConfig.smoothCamera || !zenoptimize$initialized) {
            zenoptimize$smoothedPitch = pitch;
            return pitch;
        }

        if (zenoptimize$callsThisFrame == 1) {
            float delta = pitch - zenoptimize$smoothedPitch;
            zenoptimize$smoothedPitch = Math.abs(delta) < 0.02f
                    ? pitch
                    : zenoptimize$smoothedPitch + delta * zenoptimize$alpha;
            zenoptimize$primarySmoothedPitch = zenoptimize$smoothedPitch;
            return zenoptimize$smoothedPitch;
        }

        if (zenoptimize$callsThisFrame == 2) {
            // Front view negates pitch exactly — mirror that on our smoothed value.
            return -zenoptimize$primarySmoothedPitch;
        }

        return pitch;
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
