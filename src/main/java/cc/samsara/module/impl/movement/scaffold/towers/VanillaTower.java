package cc.samsara.module.impl.movement.scaffold.towers;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldModule;

public class VanillaTower extends SubModule {
    public VanillaTower(Module parentClass) {
        super(parentClass,"Vanilla");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!mc.options.keyJump.isDown() || !((ScaffoldModule) getParentClass()).tower.getProperty()) {
            return;
        }
           // mc.player.setVelocity(mc.player.getVelocity().x, 0.42f, mc.player.getVelocity().z);

    }
}