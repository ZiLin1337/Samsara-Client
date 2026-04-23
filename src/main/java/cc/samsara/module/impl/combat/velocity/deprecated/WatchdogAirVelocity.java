package cc.samsara.module.impl.combat.velocity.deprecated;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import samsara.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.LongJumpModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.util.network.PacketUtil;
import cc.samsara.util.player.PlayerUtil;
import cc.samsara.util.render.ChatUtil;
import java.util.ArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class WatchdogAirVelocity extends SubModule {
    private final BooleanProperty breaker = new BooleanProperty("Breaker", true);

    private final ArrayList<Packet<?>> delayed = new ArrayList<>();
    private int stage = 0;

    public WatchdogAirVelocity(Module parentClass)  {
        super(parentClass,"Watchdog Air");
        this.registerPropertyToParentClass(breaker);
    }

    @Override
    public void onDisable() {
        if (!delayed.isEmpty()) {
            delayed.forEach(PacketUtil::sendNoEvent);
            delayed.clear();
        }

        super.onDisable();
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) {
                return;
            }

            if (Samsara.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled())
                return;

          /*  if (breaker.getProperty() && Samsara.getInstance().getModuleManager().getModule(BreakerModule.class).breakCache.isBreaking) {
                return;
            }
*/
            event.setCancelled(true);

            if (PlayerUtil.getDistanceToGround() < 3 && !mc.player.onGround()) {
                stage = 1;
            } else {
                PlayerUtil.setMotionY(velocityPacket.getMovement().y);
            }
        }

        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.stage = 2;
        }

        if (event.getEventMode() == EventModes.SEND) {
            if ( (event.getPacket() instanceof ServerboundPongPacket || event.getPacket() instanceof ServerboundKeepAlivePacket)) {
                if (stage >= 1 && !event.isCancelled()) {
                    event.setCancelled(true);

                    ChatUtil.printDebug("add");
                    delayed.add(event.getPacket());
                    if (stage == 2) {
                        stage = 0;
                        delayed.forEach(PacketUtil::sendNoEvent);
                        delayed.clear();
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if ((offGroundTicks <= 2 && !mc.player.onGround()) || (offGroundTicks > 3)) {
            if (stage == 1) {
                stage = 2;
            }
        }
    }
}
