package cc.samsara.module.impl.combat.killaura;

import cc.samsara.component.impl.player.RotationComponent;
import cc.samsara.interfaces.IAccess;
import cc.samsara.util.math.RandomUtil;
import cc.samsara.util.math.TimeUtil;
import cc.samsara.util.player.RotationUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

/**
 * Rotation calculation component for KillAura.
 * Supports Hypixel mode (full noise/GCD/speed randomization) and
 * simple rotation (used by HeyPixel mode via direct call).
 *
 * Usage (Hypixel mode):
 *   rotations.calculateAndApply(props, target);
 *
 * Usage (HeyPixel mode — just get face rotation):
 *   float[] rots = rotations.getSimpleRotations(target);
 */
public class KillauraRotations implements IAccess {

    // State for rotation noise/speed randomization (Hypixel mode)
    private final TimeUtil rotationRandomizationTimer = new TimeUtil();
    private final TimeUtil rotationNoiseTimer = new TimeUtil();
    private double currentYawSpeed = 0f, currentPitchSpeed = 0f;
    private float randomYawOffset = 0f, randomPitchOffset = 0f;
    private float smoothYawNoise = 0f, smoothPitchNoise = 0f;
    private float yawNoiseTarget = 0f, pitchNoiseTarget = 0f;
    private float[] rotations;

    /**
     * Interface for Hypixel mode rotation configuration.
     */
    public interface HypixelRotProps {
        boolean getRotation();
        float getRotationRange();
        float getMinYawSpeed();
        float getMaxYawSpeed();
        float getMinPitchSpeed();
        float getMaxPitchSpeed();
        String getLegitRandomization();  // "Off", "Snap", "MouseSim"
        float getNoiseYawMultiplier();
        float getNoisePitchMultiplier();
        float getMaxYawNoise();
        float getMaxPitchNoise();
        boolean getGcdFix();
    }

    /**
     * Calculate rotation and apply to RotationComponent (Hypixel full mode).
     * @return the calculated rotations array (yaw, pitch), or null if no rotation needed.
     */
    public float[] calculateAndApply(HypixelRotProps props, LivingEntity target, float distance) {
        if (distance > props.getRotationRange() || !props.getRotation()) {
            return null;
        }

        double minYaw = props.getMinYawSpeed() / 2.0;
        double maxYaw = props.getMaxYawSpeed() / 2.0;
        double minPitch = props.getMinPitchSpeed() / 2.0;
        double maxPitch = props.getMaxPitchSpeed() / 2.0;
        if (minYaw > maxYaw) minYaw = maxYaw;
        if (minPitch > maxPitch) minPitch = maxPitch;

        if (rotationRandomizationTimer.finished(150)) {
            currentYawSpeed = minYaw + new Random().nextDouble() * (maxYaw - minYaw);
            currentPitchSpeed = minPitch + new Random().nextDouble() * (maxPitch - minPitch);
        }
        currentYawSpeed = Mth.clamp(currentYawSpeed, minYaw, maxYaw);
        currentPitchSpeed = Mth.clamp(currentPitchSpeed, minPitch, maxPitch);

        float[] rotsMode;
        switch (props.getLegitRandomization()) {
            case "Snap" -> {
                if (rotationNoiseTimer.finished((long) RandomUtil.getAdvancedRandom(100, 200))) {
                    randomYawOffset = generateYawNoise(props);
                    randomPitchOffset = generatePitchNoise(props);
                    rotationNoiseTimer.reset();
                }
                Vec3 noisy = RotationUtil.getHitVec3(target).add(
                        randomYawOffset * props.getNoiseYawMultiplier(),
                        randomPitchOffset * props.getNoisePitchMultiplier(),
                        randomYawOffset * props.getNoiseYawMultiplier());
                float[] nr = RotationUtil.getRotationsToVector(noisy);
                nr[1] = Mth.clamp(nr[1], -89.9f, 89.9f);
                rotsMode = nr;
            }
            case "MouseSim" -> {
                if (rotationNoiseTimer.finished(350)) {
                    yawNoiseTarget = generateYawNoise(props);
                    pitchNoiseTarget = generatePitchNoise(props);
                    rotationNoiseTimer.reset();
                }
                smoothYawNoise += (yawNoiseTarget - smoothYawNoise) * 0.1f;
                smoothPitchNoise += (pitchNoiseTarget - smoothPitchNoise) * 0.1f;
                Vec3 noisy = RotationUtil.getHitVec3(target).add(
                        smoothYawNoise * props.getNoiseYawMultiplier(),
                        smoothPitchNoise * props.getNoisePitchMultiplier(),
                        smoothYawNoise * props.getNoiseYawMultiplier());
                float[] nr = RotationUtil.getRotationsToVector(noisy);
                nr[1] = Mth.clamp(nr[1], -89.9f, 89.9f);
                rotsMode = nr;
            }
            default -> rotsMode = RotationUtil.getRotations(target);
        }

        rotations = rotsMode;

        if (props.getGcdFix()) {
            rotations[0] += (float) (Math.random() - 0.5f);
            rotations[1] += (float) (Math.random() - 0.5f) * 2;
        }

        RotationComponent.setRotations(rotations, (float) currentYawSpeed, (float) currentPitchSpeed);
        return rotations;
    }

    /**
     * Get simple face rotations (for HeyPixel mode — no noise, no speed customization).
     */
    public float[] getSimpleRotations(LivingEntity target) {
        return RotationUtil.getRotations(target);
    }

    /**
     * Simple rotation: just set RotationComponent with default speed (180, 180).
     */
    public void applySimpleRotation(float[] rots) {
        if (rots != null) {
            RotationComponent.setRotations(rots, 180, 180);
        }
    }

    public float generateYawNoise(HypixelRotProps props) {
        return (float) ((Math.random() - 0.5f) * 2 * props.getMaxYawNoise());
    }

    public float generatePitchNoise(HypixelRotProps props) {
        return (float) ((Math.random() - 0.5f) * 2 * props.getMaxPitchNoise());
    }

    /**
     * Clear noise state (call on enable/disable).
     */
    public void reset() {
        currentYawSpeed = 0;
        currentPitchSpeed = 0;
        randomYawOffset = 0;
        randomPitchOffset = 0;
        smoothYawNoise = 0;
        smoothPitchNoise = 0;
        yawNoiseTarget = 0;
        pitchNoiseTarget = 0;
        rotations = null;
        rotationRandomizationTimer.reset();
        rotationNoiseTimer.reset();
    }
}
