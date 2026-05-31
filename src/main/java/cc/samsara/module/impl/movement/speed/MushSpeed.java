package cc.samsara.module.impl.movement.speed;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.player.MoveUtil;

public class MushSpeed extends SubModule {
    public MushSpeed(Module parentClass) {
        super(parentClass,"Mush");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround())
            mc.player.jumpFromGround();
        else
            MoveUtil.strafe();

        /*if (mc.player.hurtTime != 0) {
            PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.80);
        }*/
    }
}

