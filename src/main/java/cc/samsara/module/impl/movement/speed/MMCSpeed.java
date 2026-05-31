package cc.samsara.module.impl.movement.speed;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.player.MoveUtil;

public class MMCSpeed extends SubModule {
    public MMCSpeed(Module parentClass) {
        super(parentClass, "MMC");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()){
            mc.player.jumpFromGround();
        }

        if (offGroundTicks == 10 && mc.player.hurtTime == 0) {
            MoveUtil.strafe();
        }
    }
}