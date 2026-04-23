package cc.samsara.module.impl.movement.speed;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.player.MoveUtil;

public class StrafeSpeed extends SubModule {
    public StrafeSpeed(Module parentClass) {
        super(parentClass,"Strafe");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround())
            mc.player.jumpFromGround();

        MoveUtil.strafe(MoveUtil.getBaseSpeed());
    }
}
