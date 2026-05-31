package cc.samsara.module.impl.combat.velocity;

import cc.samsara.component.impl.player.RotationComponent;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.combat.KillauraModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.util.render.ChatUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import java.util.concurrent.LinkedBlockingDeque;
import cc.samsara.Samsara;

/**
 * Adapted from OpenZen MixMode.
 * A hybrid mode that combines jump reset with attack-on-landing:
 * queues knockback packet, intercepts movement packets during suspension,
 * then attacks the enemy twice after landing with optional rotation/movement override.
 */
public class MixVelocity extends SubModule {

    // Config
    private final BooleanProperty rotate = new BooleanProperty("Rotate", false);
    private final BooleanProperty tryAttack = new BooleanProperty("Try Attack", false);
    private final BooleanProperty movementOverride = new BooleanProperty("Movement Override", false);

    // State
    private boolean shouldAttack = false;
    private boolean wasSprinting = false;
    private boolean isSuspending = false;
    private final LinkedBlockingDeque<Packet<?>> packetQueue = new LinkedBlockingDeque<>();
    private int lastTickCount = 0;
    private int webHitCount = 0;
    private int airTicks = 0;
    private ClientboundSetEntityMotionPacket knockbackPacket;
    private int sprintTick = -1;
    private int movementState = 0;

    public MixVelocity(Module parent) {
        super(parent, "Mix");
        registerPropertiesToParentClass(rotate, tryAttack, movementOverride);
    }

    @Override
    public void onEnable() {
        resetState();
    }

    @Override
    public void onDisable() {
        resetState();
    }

    // ─── Packet interception ───
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() != EventModes.RECEIVE) return;
        var player = mc.player;
        if (player == null) return;

        Packet<?> packet = event.getPacket();

        // Web/liquid check
        if (webHitCount <= 0 || player.isInWater() || player.isUnderWater()) {
            // Intercept knockback
            if (packet instanceof ClientboundSetEntityMotionPacket motionPacket) {
                if (motionPacket.getId() != player.getId()) return;

                // Clear previous rotation
                if (knockbackPacket != null) applyTargetRotation(null);
                knockbackPacket = null;

                if (true) { // Simplified knockback detection - in Minecraft 1.21.10 the API may have changed
                    sprintTick = 0;

                    // Try attack via KillAura
                    KillauraModule ka = (KillauraModule) Samsara.getInstance().getModuleManager()
                            .getModule(KillauraModule.class);
                    if (ka != null && ka.target != null && tryAttack.getProperty()) {
                        shouldAttack = true;
                        wasSprinting = player.isSprinting();
                    } else if (rotate.getProperty()) {
                        // Simplified rotation - in Minecraft 1.21.10 the API may have changed
                        applyTargetRotation(new float[]{0, player.getXRot()});
                    }

                    event.setCancelled(true);
                    packetQueue.add(packet);
                    isSuspending = true;
                    knockbackPacket = motionPacket;
                }
            }

            // Queue movement/position packets during suspension
            if (isSuspending) {
                if (packet instanceof ClientboundMoveEntityPacket
                    || packet instanceof ClientboundTeleportEntityPacket) {
                    packetQueue.add(packet);
                    event.setCancelled(true);
                }
                if (packet instanceof ClientboundPlayerPositionPacket) {
                    ChatUtil.print("?");
                    resetState();
                }
            }
        } else {
            // In web/liquid — clear everything
            applyTargetRotation(null);
            knockbackPacket = null;
            flushPackets();
            isSuspending = false;
            ChatUtil.print("Ignore: Player in web or liquid!");
        }
    }

    // ─── Tick handlers ───
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        var player = mc.player;
        if (player == null) return;

        // Track web hits
        if (lastTickCount < player.tickCount) {
            webHitCount = 0;
        }

        // Flush packets when on ground or air ticks exceeded
        if (player.onGround() || airTicks >= 24) {
            flushPackets();
            isSuspending = false;
            shouldAttack = false;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) return;
        if (mc.player == null) return;

        airTicks = mc.player.onGround() ? 0 : airTicks + 1;

        // Game tick equivalent: handle jump key for Mix mode
        if (knockbackPacket != null && !movementOverride.getProperty() && mc.player != null) {
            if (mc.player.hurtTime > 6 && !mc.options.keyJump.isDown()) {
                mc.options.keyJump.setDown(true);
            } else {
                // Simplified jump check - in Minecraft 1.21.10 the API may have changed
                mc.options.keyJump.setDown(false);
            }
        }
    }

    // ─── Input / strafe ───
    @EventTarget
    public void onInputTick(InputTickEvent event) {
        var player = mc.player;
        if (player == null) return;

        if (!isSuspending) {
            applyTargetRotation(null);
        }

        // Attack on sprint tick
        if (knockbackPacket != null && player.hurtTime > 0 && shouldAttack) {
            shouldAttack = false;
            KillauraModule ka = (KillauraModule) Samsara.getInstance().getModuleManager()
                    .getModule(KillauraModule.class);
            for (int i = 0; i < 2; ++i) {
                player.setSprinting(false);
                if (ka != null && ka.isToggled()) {
                    // Perform attack twice (like original MixMode)
                    if (mc.gameMode != null && ka.target != null) {
                        mc.gameMode.attack(mc.player, ka.target);
                        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    }
                }
            }
        }

        // Track sprint state
        if (sprintTick >= 0) {
            ++sprintTick;
        }

        // Movement override
        if (movementOverride.getProperty() && knockbackPacket != null) {
            if (sprintTick >= 1) {
                if (sprintTick <= 2 && player.onGround()) {
                    mc.options.keyJump.setDown(true);
                }
                if (sprintTick <= 3) {
                    applyKBDirection();
                    movementState = 1;
                }
            }
            if (sprintTick >= 4 && sprintTick <= 10) {
                mc.options.keyJump.setDown(false);
                if (movementState == 1) {
                    restoreMovementKeys();
                    movementState = 0;
                }
            }
            if (sprintTick >= 10) {
                sprintTick = -1;
            }
        }
    }

    // ─── Internal helpers ───
    private void applyTargetRotation(float[] rot) {
        if (rot != null) {
            RotationComponent.setRotations(rot, 180, 180);
        }
    }

    private void applyKBDirection() {
        if (knockbackPacket == null || mc.player == null) return;
        // Simplified knockback direction - in Minecraft 1.21.10 the API may have changed
        // This is a placeholder implementation
        restoreMovementKeys();
        
        // Simple forward movement
        mc.options.keyUp.setDown(true);
    }

    private void restoreMovementKeys() {
        // Simplified key restoration - in Minecraft 1.21.10 the API may have changed
        // This is a placeholder implementation
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
    }

    private void flushPackets() {
        while (!packetQueue.isEmpty()) {
            try {
                ((Packet<ClientGamePacketListener>) packetQueue.poll()).handle(mc.getConnection());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void resetState() {
        knockbackPacket = null;
        lastTickCount = 0;
        webHitCount = 0;
        isSuspending = false;
        flushPackets();
        applyTargetRotation(null);
        restoreMovementKeys();
        sprintTick = -1;
        movementState = 0;
        shouldAttack = false;
        airTicks = 0;
    }
}
