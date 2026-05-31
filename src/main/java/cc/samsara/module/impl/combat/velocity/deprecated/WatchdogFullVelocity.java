package cc.samsara.module.impl.combat.velocity.deprecated;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.event.events.impl.game.WorldChangeEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.LongJumpModule;
import cc.samsara.util.network.PacketUtil;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class WatchdogFullVelocity extends SubModule {
    private final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private boolean canSpoof;

    public WatchdogFullVelocity(Module parentClass)  {
        super(parentClass,"Watchdog Full");
    }

    @Override
    public void onEnable() {
        super.onEnable();

        for (final Packet<?> packet : this.packets) {
            PacketUtil.receiveNoEvent(packet);
            this.packets.remove(packet);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST)
            return;


        if (mc.player == null || mc.player.tickCount <= 1 || mc.player.getAbilities().mayfly || Samsara.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled()) {
            for (final Packet<?> packet : this.packets) {
                PacketUtil.receiveNoEvent(packet);
                this.packets.remove(packet);
            }

            return;
        }

        if (Samsara.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled())
            return;
        if (mc.player.onGround() && this.canSpoof) {
            this.canSpoof = false;
            double oldMotionX = mc.player.getDeltaMovement().x;
            double oldMotionY = mc.player.getDeltaMovement().y;
            double oldMotionZ = mc.player.getDeltaMovement().z;

            for (final Packet<?> packet : this.packets) {
                PacketUtil.receiveNoEvent(packet);
                this.packets.remove(packet);
            }

            mc.player.setDeltaMovement(oldMotionX, oldMotionY, oldMotionZ);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() != EventModes.RECEIVE)
            return;

        if (mc.player == null || mc.player.tickCount <= 1 || mc.player.getAbilities().mayfly || Samsara.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled()) {
            for (final Packet<?> packet : this.packets) {
                PacketUtil.receiveNoEvent(packet);
                this.packets.remove(packet);
            }

            return;
        }

        if (Samsara.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled())
            return;

        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket wrapper) {
            if (wrapper.getId() != mc.player.getId()) {
                return;
            }

            this.canSpoof = true;
            this.packets.add(event.getPacket());
            event.setCancelled(true);
        }

        if (event.getPacket() instanceof ClientboundPingPacket && this.canSpoof) {
            this.packets.add(event.getPacket());
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onWorldChange(WorldChangeEvent event) {
        for (final Packet<?> packet : this.packets) {
            PacketUtil.receiveNoEvent(packet);
            this.packets.remove(packet);
        }
    }
}
