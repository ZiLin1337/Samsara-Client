package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.EntityInteractEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Category;
import cc.samsara.module.Module;

public class ForceCritModule extends Module {

    public ForceCritModule() {
        super(Category.COMBAT);
    }

    @EventTarget
    public void onAttack(EntityInteractEvent event) {
        if (event.getEventMode() == EventModes.PRE && !mc.player.onGround()) {
            mc.player.setSprinting(false);
        }
    }
}
