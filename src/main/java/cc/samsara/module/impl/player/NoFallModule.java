package cc.samsara.module.impl.player;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.module.impl.player.nofall.*;
import cc.samsara.property.properties.ClassModeProperty;

public class NoFallModule extends Module {

    public final ClassModeProperty mode = new ClassModeProperty("Mode",
            new GroundNofall(this), new UniversalNofall(this),
            new VulcanNofall(this), new WatchdogNofall(this), new BlocksMcNofall(this)
    );

    public NoFallModule() {
        super(Category.PLAYER);
        this.registerProperty(mode);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        this.setSuffix(mode.getProperty().getFormatedName());
    }
}
