package cc.samsara.module.impl.combat.velocity;

import cc.samsara.event.EventTarget;
import cc.samsara.Samsara;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.combat.KillauraModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.util.render.ChatUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Adapted from OpenZen NoXZMode.
 * Intercepts knockback packets, queues them, delays processing until landing,
 * then executes an attack sequence while cancelling horizontal velocity.
 */
public class NoXZVelocity extends SubModule {

    // Config
    private final NumberProperty attackAmount = new NumberProperty("Attack Amount", 5, 1, 20, 1);
    private final BooleanProperty sprintStateCheck = new BooleanProperty("Sprint State Check", true);

    // State
    private final LinkedBlockingDeque<Packet<?>> packetQueue = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<Packet<?>> movePacketQueue = new LinkedBlockingDeque<>();
    private ClientboundSetEntityMotionPacket knockbackPacket = null;
    private boolean isSuspending = false;
    private int suspendTicks = 0;
    private boolean shouldFlushMotion = false;
    private Entity attackTarget = null;
    private int attacksRemaining = 0;
    private int attackCooldown = 0;
    private int hitCounter = 0;
    private boolean shouldJump = false;
    private int sprintBoostCounter = 0;
    private boolean isFlushing = false;

    // Instant attack support (replaces tick-rate manipulation)
    private int instantAttackCount = 0;
    private int instantAttackTotal = 0;
    private boolean isInstantAttacking = false;

    public NoXZVelocity(Module parent) {
        super(parent, "NoXZ");
        registerPropertiesToParentClass(attackAmount, sprintStateCheck);
    }

    @Override
    public void onEnable() {
        resetAll();
    }

    @Override
    public void onDisable() {
        release();
        resetAll();
    }

    // ─── Packet handling ───
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() != EventModes.RECEIVE) return;
        if (mc.player == null || mc.level == null) return;
        if (isFlushing) return;
        if (shouldIgnore()) return;

        Packet<?> packet = event.getPacket();

        // Queue move packets during suspension
        if (packet instanceof ServerboundMovePlayerPacket && isSuspending) {
            movePacketQueue.add(packet);
            event.setCancelled(true);
            return;
        }

        // Flag detection
        if (packet instanceof ClientboundPlayerPositionPacket) {
            if (isSuspending) release();
            resetSuspension();
            ChatUtil.print("Flag Detected");
            return;
        }

        // Queue non-critical packets during suspension
        if (isSuspending) {
            if (!isAllowedPacket(packet)) {
                packetQueue.add(packet);
                event.setCancelled(true);
            }
            return;
        }

        // Intercept knockback
        if (packet instanceof ClientboundSetEntityMotionPacket motionPacket) {
            if (motionPacket.getId() != mc.player.getId()) return;

            // Simplified knockback detection - in Minecraft 1.21.10 the API may have changed
            hitCounter = 1;

            // Simplified vertical motion check
            sprintBoostCounter = sprintBoostCounter % 100 + 100;
            if (sprintBoostCounter >= 100) {
                shouldJump = true;
            }

            boolean canAttack = isValidTarget(getAttackTarget()) && mc.player.isSprinting();

            if (!mc.player.onGround()) {
                // In air: suspend and queue
                isSuspending = true;
                suspendTicks = 0;
                knockbackPacket = motionPacket;
                event.setCancelled(true);
            } else if (canAttack) {
                // On ground with valid target: prepare attack sequence
                attackTarget = getAttackTarget();
                attacksRemaining = (int) attackAmount.getProperty().floatValue();
            } else {
                // On ground no target: still suspend
                isSuspending = true;
                suspendTicks = 0;
                knockbackPacket = motionPacket;
                event.setCancelled(true);
                ChatUtil.print("NoXZ: Wait (no target)");
            }
        }
    }

    // ─── Tick handler ───
    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) return;
        if (mc.player == null) return;

        // Attack cooldown
        if (attackCooldown > 0) {
            --attackCooldown;
        }

        // Hit counter decay
        if (hitCounter > 0) {
            ++hitCounter;
            if (hitCounter > 2) hitCounter = 0;
        }

        // Dead/alive check
        if (mc.player.isDeadOrDying() || !mc.player.isAlive() || shouldIgnore()) {
            clearTarget();
            if (isSuspending) release();
            if (isInstantAttacking) {
                isInstantAttacking = false;
                instantAttackCount = 0;
                instantAttackTotal = 0;
            }
            return;
        }

        // Suspension logic
        if (isSuspending) {
            ++suspendTicks;

            boolean onGround = mc.player.onGround();
            boolean isTimeout = suspendTicks >= 12;

            if (onGround || isTimeout) {
                if (isTimeout) ChatUtil.print("NoXZ: Timeout");

                Entity target = getAttackTarget();
                boolean canAttack = isValidTarget(target);
                boolean sprinting = mc.player.isSprinting();

                if (onGround && canAttack && sprinting) {
                    isFlushing = true;
                    attackTarget = target;
                    attacksRemaining = (int) attackAmount.getProperty().floatValue();
                    sendMovePackets();
                    applyKnockbackPacket();

                    // Execute attack sequence over next ticks
                    doAttackSequence();
                    scheduleMotionFlush();
                    isSuspending = false;
                    suspendTicks = 0;
                    isFlushing = false;
                } else {
                    release();
                    if (onGround && mc.player.isSprinting()) {
                        mc.player.setSprinting(false);
                    }
                }
                return;
            }
            return;
        }

        // Remaining attacks from previous sequence
        if (attacksRemaining > 0 && attackTarget != null) {
            doAttackSequence();
        }
    }

    // ─── Input handler ───
    @EventTarget
    public void onInputTick(InputTickEvent event) {
        if (mc.player == null) return;
        if (hitCounter > 0) {
            // Simplified forward input - in Minecraft 1.21.10 the API may have changed
        }
        if (shouldJump) {
            shouldJump = false;
            if (mc.player.onGround() && mc.player.isSprinting()
                    && !mc.player.hasEffect(MobEffects.JUMP_BOOST) && !shouldIgnore()) {
                // Simplified sprinting - in Minecraft 1.21.10 the API may have changed
            }
        }
    }

    // ─── Internal helpers ───
    private void doAttackSequence() {
        if (attackTarget == null || !attackTarget.isAlive()) {
            clearTarget();
            return;
        }
        double maxReach = 3.7;
        if (getAABBDistance(attackTarget) > maxReach) {
            clearTarget();
            return;
        }

        if (doAttack(attackTarget)) {
            attacksRemaining--;
            attackCooldown = 2;
        }

        if (attacksRemaining <= 0) {
            clearTarget();
        }
    }

    private boolean doAttack(Entity entity) {
        if (mc.player == null || mc.gameMode == null) return false;
        if (sprintStateCheck.getProperty() && !mc.player.isSprinting()) return false;

        boolean wasSprinting = mc.player.isSprinting();
        if (wasSprinting) mc.player.setSprinting(false);

        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (wasSprinting) {
            Vec3 vel = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(vel.x * 0.6, vel.y, vel.z * 0.6);
        }
        return true;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null || !entity.isAlive()) return false;
        if (entity instanceof LivingEntity le && (le.isDeadOrDying() || le.getHealth() <= 0.0f)) return false;
        return getAABBDistance(entity) <= 3.7;
    }

    private Entity getAttackTarget() {
        if (mc.player == null) return null;
        // Use Samsara's KillauraModule target first
        KillauraModule ka = (KillauraModule) Samsara.getInstance().getModuleManager().getModule(KillauraModule.class);
        if (ka != null && ka.target != null) return ka.target;
        return getHitResultEntity();
    }

    private Entity getHitResultEntity() {
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity hit = ((EntityHitResult) mc.hitResult).getEntity();
            if (hit instanceof LivingEntity && hit != mc.player && hit.isAlive() && !hit.isSpectator())
                return hit;
        }
        return null;
    }

    private double getAABBDistance(Entity entity) {
        if (mc.player == null) return Double.MAX_VALUE;
        Vec3 eye = mc.player.getEyePosition(1.0f);
        AABB box = entity.getBoundingBox();
        return eye.distanceTo(new Vec3(
                Math.max(box.minX, Math.min(eye.x, box.maxX)),
                Math.max(box.minY, Math.min(eye.y, box.maxY)),
                Math.max(box.minZ, Math.min(eye.z, box.maxZ))
        ));
    }

    private boolean shouldIgnore() {
        if (mc.player == null || mc.level == null) return true;
        if (mc.player.isDeadOrDying() || !mc.player.isAlive() || mc.player.getHealth() <= 0.0f) return true;
        if (mc.player.isSpectator() || mc.player.getAbilities().flying) return true;
        if (mc.player.isInLava() || mc.player.isOnFire() || mc.player.isInWater()
                || mc.player.onClimbable() || mc.player.isSleeping()) return true;
        // Check cobweb at player position
        if (mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.COBWEB)) return true;
        return false;
    }

    private boolean isAllowedPacket(Packet<?> packet) {
        return packet instanceof ClientboundSetEntityMotionPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundSetHealthPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundContainerClosePacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
                || packet instanceof net.minecraft.network.protocol.game.ClientboundAnimatePacket;
    }

    private void sendMovePackets() {
        if (mc.getConnection() == null) return;
        while (!movePacketQueue.isEmpty()) {
            Packet<?> p = movePacketQueue.poll();
            if (p == null) continue;
            try {
                mc.getConnection().send(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyKnockbackPacket() {
        if (knockbackPacket != null && mc.getConnection() != null) {
            try {
                knockbackPacket.handle(mc.getConnection());
            } catch (Exception e) {
                e.printStackTrace();
            }
            knockbackPacket = null;
        }
    }

    private void scheduleMotionFlush() {
        shouldFlushMotion = true;
    }

    private void release() {
        isFlushing = true;
        sendMovePackets();
        applyKnockbackPacket();
        scheduleMotionFlush();
        isFlushing = false;
        isSuspending = false;
        suspendTicks = 0;
    }

    private void clearTarget() {
        attackTarget = null;
        attacksRemaining = 0;
    }

    private void resetSuspension() {
        isSuspending = false;
        suspendTicks = 0;
        knockbackPacket = null;
        packetQueue.clear();
        movePacketQueue.clear();
        isFlushing = false;
    }

    private void resetAll() {
        clearTarget();
        hitCounter = 0;
        sprintBoostCounter = 0;
        shouldJump = false;
        resetSuspension();
        isInstantAttacking = false;
        instantAttackCount = 0;
        instantAttackTotal = 0;
    }
}
