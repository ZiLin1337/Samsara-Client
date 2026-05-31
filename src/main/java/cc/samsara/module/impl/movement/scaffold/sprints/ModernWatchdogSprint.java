package cc.samsara.module.impl.movement.scaffold.sprints;

import cc.samsara.Samsara;
import cc.samsara.component.impl.network.BlinkComponent;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldModule;
import cc.samsara.util.math.TimeUtil;

public class ModernWatchdogSprint extends SubModule {
    private final ScaffoldModule sc = (ScaffoldModule) getParentClass();
    private final TimeUtil timeUtil = new TimeUtil();
    private final BlinkComponent blinkComponent = Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class);
    private boolean hasBlinked = false;

    public ModernWatchdogSprint(Module parentClass) {
        super(parentClass, "Modern Watchdog");
    }

    @Override
    public void onDisable() {
        blinkComponent.stopBlinking();
        timeUtil.reset();
        hasBlinked = false;
        super.onDisable();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!hasBlinked) {
            blinkComponent.startBlinking();
            timeUtil.reset();
            hasBlinked = true;
        } else if (timeUtil.finished(1000)) {
            blinkComponent.stopBlinking();
        }
    }
}

