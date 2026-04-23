package cc.samsara.module.impl.combat.velocity;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import samsara.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.velocity.VelocityBuilder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class AirVelocity extends SubModule {
    private final NumberProperty horizontalVelocity = new NumberProperty("Horizontal Velocity", 0, 0, 100, 1);

    public AirVelocity(Module parentClass)  {
        super(parentClass,"Air");
        this.registerPropertyToParentClass(horizontalVelocity);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket packet) {
            EntityVelocityUpdateS2CPacketAccessor accessor = (EntityVelocityUpdateS2CPacketAccessor) packet;

            if (accessor.getId() != mc.player.getId()) {
                return;
            }

            if (mc.player.onGround()) {
                accessor.setMovement(VelocityBuilder.from(accessor.getMovement())
                        .setVelocityX(accessor.getMovement().x * (horizontalVelocity.getProperty().intValue() / 100D))
                        .setVelocityZ(accessor.getMovement().z * (horizontalVelocity.getProperty().intValue() / 100D))
                        .build()
                );
            } else {
                event.setCancelled(true);
            }
        }
    }


}
