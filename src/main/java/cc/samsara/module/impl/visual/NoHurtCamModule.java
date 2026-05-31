package cc.samsara.module.impl.visual;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.HurtCamEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;

public class NoHurtCamModule extends Module {
    public NoHurtCamModule() {
        super(Category.VISUAL);
    }

    @EventTarget
    public void onHurtCam(HurtCamEvent event) {
        event.setCancelled(true);
    }
}
