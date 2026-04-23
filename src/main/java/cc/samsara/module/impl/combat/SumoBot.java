package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;

public class SumoBot extends Module {

    public SumoBot() {
        super(Category.COMBAT);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {

    }
}
