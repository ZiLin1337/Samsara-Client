package cc.samsara.module.impl.player.nofall;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.util.network.PacketUtil;
import cc.samsara.util.player.PlayerUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class WatchdogNofall extends SubModule {
    private final BooleanProperty timo = new BooleanProperty("Timer", false);

    private int voidTicks;

    public WatchdogNofall(Module parentClass){
        super(parentClass,"Watchdog");
        this.registerPropertyToParentClass(timo);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (PlayerUtil.isPlayerOverVoid())
            voidTicks++;
        else
            voidTicks = 0;

        if (mc.player.fallDistance - mc.player.getDeltaMovement().y > 3 && !mc.player.getAbilities().mayfly && voidTicks < 20
        ) {
            if (timo.getProperty()) {

                timer = 0.5f;
            }
            PacketUtil.send(new ServerboundMovePlayerPacket.StatusOnly(true,false));
            mc.player.fallDistance = 0;
        } else {
            if (timo.getProperty()) {
                timer = 1.0f;
            }
        }
    }
}
