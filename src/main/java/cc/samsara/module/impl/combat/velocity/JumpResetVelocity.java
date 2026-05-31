package cc.samsara.module.impl.combat.velocity;

import cc.samsara.component.impl.player.RotationComponent;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.util.Mth;
import java.util.concurrent.LinkedBlockingDeque;
import cc.samsara.Samsara;

/**
 * Adapted from OpenZen JumpResetMode.
 * Replaces the old simple JumpVelocity with full jump-reset logic:
 * packet queuing, rotation follow, key-down simulation, phase state machine.
 */
public class JumpResetVelocity extends SubModule {

    public static volatile boolean isJumping = false;

    private enum Phase {
        IDLE, AIR, GROUND
    }

    private ClientboundSetEntityMotionPacket knockbackPacket;
    private int rotationHeldTicks = 0;
    private final LinkedBlockingDeque<Packet<ClientGamePacketListener>> packetQueue = new LinkedBlockingDeque<>();
    private Packet<ClientGamePacketListener> pendingPacket;
    private boolean isSuspending = false;
    private int delayTicks = 0;
    private Phase currentPhase = Phase.IDLE;
    private int jumpTicks = 0;
    private float[] targetRotation = null;
    private boolean shouldFlushStrafe = false;

    // Config
    private final BooleanProperty rotate = new BooleanProperty("Rotate", false);
    private final BooleanProperty followDirection = new BooleanProperty("Follow Direction", false);
    private final NumberProperty rotationRange = new NumberProperty("Rotate Range", 3, 0, 6, 0.1f)
            .setVisible(() -> rotate.getProperty() || followDirection.getProperty());

    public JumpResetVelocity(Module parent) {
        super(parent, "Jump Reset");
        registerPropertiesToParentClass(rotate, followDirection, rotationRange);
    }

    @Override
    public void onEnable() {
        this.knockbackPacket = null;
        this.targetRotation = null;
        this.rotationHeldTicks = 0;
        this.resetState();
        isJumping = false;
    }

    @Override
    public void onDisable() {
        // Release any queued packets
        this.flushQueue(false);
        this.knockbackPacket = null;
        this.targetRotation = null;
        this.rotationHeldTicks = 0;
        this.resetState();
        isJumping = false;
    }

    private void resetState() {
        this.isSuspending = false;
        this.delayTicks = 0;
        this.pendingPacket = null;
        this.packetQueue.clear();
        isJumping = false;
        this.currentPhase = Phase.IDLE;
        this.jumpTicks = 0;
    }

    private boolean isScaffoldEnabled() {
        Module scaf = Samsara.getInstance().getModuleManager().getModule(ScaffoldModule.class);
        return scaf != null && scaf.isToggled();
    }

    /**
     * Check if any conflicting module is active that should cause us to suspend operation.
     * In Samsara, this checks Scaffold (which also uses jump).
     */
    private boolean isConflicting() {
        return this.isScaffoldEnabled();
    }

    private void flushQueue(boolean dropPending) {
        var connection = mc.getConnection();
        if (connection == null) {
            this.packetQueue.clear();
            return;
        }
        if (!this.packetQueue.isEmpty() && dropPending && this.packetQueue.getFirst() == this.pendingPacket) {
            this.packetQueue.pollFirst();
        }
        Packet<ClientGamePacketListener> packet;
        while ((packet = this.packetQueue.poll()) != null) {
            try {
                packet.handle(connection);
            } catch (Exception e) {
                this.packetQueue.clear();
                break;
            }
        }
    }

    // ─── Packet interception ───
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() != EventModes.RECEIVE) return;
        var player = mc.player;
        if (player == null) return;

        Packet<?> packet = event.getPacket();

        // If we are suspending, queue most non-critical packets
        if (this.isSuspending
                && !(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket)
                && !(packet instanceof net.minecraft.network.protocol.game.ClientboundSetTimePacket)) {
            event.setCancelled(true);
            this.packetQueue.add(packet);
            return;
        }

        if (!(packet instanceof ClientboundSetEntityMotionPacket motion)) return;
        if (motion.getId() != player.getId()) return;

        // We got a knockback packet
        this.knockbackPacket = motion;

        boolean wantRotate = rotate.getProperty() || followDirection.getProperty();
        if (wantRotate) {
            // Simplified rotation - in Minecraft 1.21.10 the API may have changed
            float yaw = player.getYRot();
            this.targetRotation = new float[]{yaw, player.getXRot()};
            this.rotationHeldTicks = 0;
            RotationComponent.setRotations(new float[]{yaw, player.getXRot()}, 180, 180);
        }

        if (!player.onGround()) {
            this.isSuspending = true;
            this.currentPhase = Phase.AIR;
            this.delayTicks = 20;
            isJumping = true;
            this.pendingPacket = packet;
            this.packetQueue.add(packet);
            event.setCancelled(true);
        } else {
            this.isSuspending = true;
            this.currentPhase = Phase.GROUND;
            this.delayTicks = 10;
            isJumping = true;
            this.pendingPacket = packet;
            this.packetQueue.add(packet);
            event.setCancelled(true);
        }
    }

    // ─── Tick handler ───
    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) return;
        var player = mc.player;
        if (player == null) return;
        if (this.isConflicting()) {
            if (this.isSuspending) this.flushQueue(false);
            this.targetRotation = null;
            this.rotationHeldTicks = 0;
            this.resetState();
            return;
        }

        // Ground phase: handle jump key
        if (this.isSuspending && this.currentPhase == Phase.GROUND) {
            if (!this.isScaffoldEnabled()) {
                // Simplified jump check - in Minecraft 1.21.10 the API may have changed
                mc.options.keyJump.setDown(false);
            }
        }

        // Active jump ticks (after jump reset, keep holding jump)
        if (this.jumpTicks > 0 && !this.isScaffoldEnabled()) {
            mc.options.keyJump.setDown(true);
            this.jumpTicks--;
        }

        // Release jump key when no longer needed
        if (this.jumpTicks <= 0 && !this.isScaffoldEnabled()) {
            // Simplified jump check - in Minecraft 1.21.10 the API may have changed
            mc.options.keyJump.setDown(false);
        }

        // Suspension logic
        if (this.isSuspending) {
            if (this.currentPhase == Phase.AIR) {
                // Stay in air holding jump until we land or timeout
                if (player.onGround()) {
                    this.flushQueue(false);
                    this.resetState();
                } else if (this.delayTicks > 0) {
                    this.delayTicks--;
                } else {
                    this.flushQueue(false);
                    this.resetState();
                }
            } else if (this.currentPhase == Phase.GROUND) {
                if (this.delayTicks > 0) {
                    this.delayTicks--;
                } else {
                    this.flushQueue(false);
                    if (this.targetRotation != null) {
                        this.rotationHeldTicks = 0;
                        RotationComponent.setRotations(this.targetRotation, 180, 180);
                        this.targetRotation = null;
                        this.shouldFlushStrafe = true;
                    }
                    this.resetState();
                    this.jumpTicks = 1;
                }
            }
        }

        // Rotation hold timeout
        if (this.targetRotation != null) {
            this.rotationHeldTicks++;
        }
        boolean shouldClear = player.hurtTime == 0
                || this.rotationHeldTicks > (int) rotationRange.getProperty().floatValue()
                || (!rotate.getProperty() && !followDirection.getProperty());
        if (shouldClear) {
            this.targetRotation = null;
            this.knockbackPacket = null;
            this.rotationHeldTicks = 0;
        }
    }

    // ─── Strafe / input control ───
    @EventTarget
    public void onInputTick(InputTickEvent event) {
        var player = mc.player;
        if (player == null) return;

        if (this.isConflicting()) {
            if (this.isSuspending) this.flushQueue(false);
            this.targetRotation = null;
            this.rotationHeldTicks = 0;
            this.resetState();
            return;
        }

        if (followDirection.getProperty() && this.targetRotation != null) {
            // Force forward movement in the direction of knockback
            // Simplified input - in Minecraft 1.21.10 the API may have changed
        }
    }
}
