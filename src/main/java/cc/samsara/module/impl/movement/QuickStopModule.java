package cc.samsara.module.impl.movement;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.util.player.MoveUtil;

public class QuickStopModule extends Module {

    public QuickStopModule() {
        super(Category.MOVEMENT);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!MoveUtil.isMoving())
            MoveUtil.stop();
    }
}
