package cc.samsara.module.impl.movement.scaffold.sprints;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;

public class IntaveSprint extends SubModule {
    public IntaveSprint(Module parentClass) {
        super(parentClass,"Intave");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            final float multiply = 1.1f;
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * multiply, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * multiply);
        }
    }
}
