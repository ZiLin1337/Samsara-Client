package cc.samsara.module.impl.movement;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.util.player.MoveUtil;

public class SprintModule extends Module {
    public SprintModule() {
        super(Category.MOVEMENT);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (Samsara.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled()) {
            return;
        }

        mc.options.keySprint.setDown(MoveUtil.isMoving());
    }
}
