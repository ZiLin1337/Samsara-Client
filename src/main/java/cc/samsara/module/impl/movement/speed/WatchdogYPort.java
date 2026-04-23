package cc.samsara.module.impl.movement.speed;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MoveEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.player.MoveUtil;

public class WatchdogYPort extends SubModule {

    public WatchdogYPort(Module parentClass) {
        super(parentClass,"Watchdog Yport");
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (mc.player.fallDistance > 0.07)
            MoveUtil.stop();

        if (mc.player.onGround()) event.setY(0.003);
        if (mc.player.onGround() || mc.player.getDeltaMovement().y == (0.003 - 0.08) * 0.98F)
            MoveUtil.strafe(event, MoveUtil.getPerfectValue(0.3296F, 0.3689F, 0.4252F));
    }
}
