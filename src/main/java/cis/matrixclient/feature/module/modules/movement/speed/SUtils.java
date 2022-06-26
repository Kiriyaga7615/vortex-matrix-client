package cis.matrixclient.feature.module.modules.movement.speed;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec2f;

import static cis.matrixclient.MatrixClient.mc;

public class SUtils {
    static boolean isPlayerMoving() {
        return mc.player.input.movementSideways != 0 || mc.player.input.movementForward != 0;
    }

    static Vec2f OmniSpeeds(double speed) {
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();
        if (forward == 0.0f && side == 0.0f) {
            return new Vec2f(0, 0);
        }
        if (forward != 0.0f) {
            if (side >= 1.0f) {
                yaw += (float) (forward > 0.0f ? -45 : 45);
                side = 0.0f;
            } else if (side <= -1.0f) {
                yaw += (float) (forward > 0.0f ? 45 : -45);
                side = 0.0f;
            }
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double mx = Math.cos(Math.toRadians(yaw + 90.0f));
        double mz = Math.sin(Math.toRadians(yaw + 90.0f));
        double velX = (double) forward * speed * mx + (double) side * speed * mz;
        double velZ = (double) forward * speed * mz - (double) side * speed * mx;
        return new Vec2f((float) velX, (float) velZ);
    }

    static double getDefaultSpeed() {
        int amplifier;
        double defaultSpeed = 0.2873;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            defaultSpeed *= 1.0 + 0.2 * (double) (amplifier + 1);
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            defaultSpeed /= 1.0 + 0.2 * (double) (amplifier + 1);
        }
        return defaultSpeed;
    }

    static double GetPlayerHeight(double height) {
        StatusEffectInstance jumpBoost;
        jumpBoost = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ? mc.player.getStatusEffect(StatusEffects.JUMP_BOOST) : null;
        if (jumpBoost != null) {
            height += ((float) jumpBoost.getAmplifier() + 0.1f);
        }
        return height;
    }
}