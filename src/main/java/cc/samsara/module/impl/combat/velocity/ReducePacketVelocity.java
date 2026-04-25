package cc.samsara.module.impl.combat.velocity;

import cc.samsara.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.network.PacketUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;

public class ReducePacketVelocity extends SubModule {

    public ReducePacketVelocity(Module parentClass)  {
        super(parentClass,"Reduce Packet");
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) return;

            for (int i = 0; i < 5; i++) {
                //It ain't hard bro
                PacketUtil.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }
}


