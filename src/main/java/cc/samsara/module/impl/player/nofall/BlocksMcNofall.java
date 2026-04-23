package cc.samsara.module.impl.player.nofall;

import samsara.mixin.accessor.network.PlayerMoveC2SPacketAccessor;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class BlocksMcNofall extends SubModule {

    public BlocksMcNofall(Module parentClass) {
        super(parentClass, "Blocks MC");
    }

    private boolean clip = false;

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player.getDeltaMovement().y < -0.7) {
            clip = true;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
            PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) event.getPacket();
            if (mc.player.onGround() && clip) {
                accessor.setY(-0.1);
            }
        }
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            clip = false;
        }
    }
}
