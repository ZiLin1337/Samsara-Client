package cc.samsara.module.impl.combat.velocity;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.velocity.VelocityBuilder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class NormalVelocity extends SubModule {
    private final NumberProperty horizontalVelocity = new NumberProperty("Horizontal Velocity", 0, 0, 100, 1),
            verticalVelocity = new NumberProperty("Vertical Velocity", 0, 0, 100, 1);

    public NormalVelocity(Module parentClass)  {
        super(parentClass,"Normal");
        this.registerPropertiesToParentClass(horizontalVelocity, verticalVelocity);
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) {
                return;
            }

            final Vec3 velocity = velocityAccessor.getMovement();

            velocityAccessor.setMovement(
                    VelocityBuilder.from(velocityAccessor.getMovement())
                    .setVelocityX(velocity.x * (horizontalVelocity.getProperty().intValue() / 100D))
                    .setVelocityZ(velocity.z * (horizontalVelocity.getProperty().intValue() / 100D))
                    .setVelocityY(velocity.y * (verticalVelocity.getProperty().intValue() / 100D))
                    .build()
            );
        }
    }
}
