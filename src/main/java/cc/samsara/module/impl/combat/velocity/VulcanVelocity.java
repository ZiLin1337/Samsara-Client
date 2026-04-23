package cc.samsara.module.impl.combat.velocity;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import samsara.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.player.PlayerUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class VulcanVelocity extends SubModule {
    public VulcanVelocity(Module parentClass)  {
        super(parentClass,"Vulcan");
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) {
                return;
            }

            event.setCancelled(true);
            PlayerUtil.setMotionY(velocityPacket.getMovement().y);
        }
    }
}
