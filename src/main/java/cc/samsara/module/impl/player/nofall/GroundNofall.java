package cc.samsara.module.impl.player.nofall;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;

public class GroundNofall extends SubModule {

    public GroundNofall(Module parentClass)  {
        super(parentClass,"Ground");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        event.setOnGround(true);
    }
}
