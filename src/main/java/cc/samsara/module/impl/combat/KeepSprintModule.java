package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.LoseSprintEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;

public class KeepSprintModule extends Module {
    private final BooleanProperty groundOnly = new BooleanProperty("Ground Only", false);

    public KeepSprintModule() {
        super(Category.COMBAT);
        this.registerProperty(groundOnly);
    }

    @EventTarget
    public void onLoseSprint(LoseSprintEvent event) {
        if (!groundOnly.getProperty() || mc.player.onGround())
            event.setCancelled(true);
    }
}
