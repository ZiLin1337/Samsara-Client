package cc.samsara.module.impl.player;

import cc.samsara.Samsara;
import cc.samsara.component.impl.network.BlinkComponent;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;

import cc.samsara.util.math.TimeUtil;

public class BlinkModule extends Module {
    private final BooleanProperty flushAutomatically = new BooleanProperty("Flush Automatically", true);
    private final NumberProperty flushIntervalMs = new NumberProperty("Flush Interval (ms)", 500, 100, 2000, 50).setVisible(flushAutomatically::getProperty);

    private final TimeUtil timer = new TimeUtil();

    public BlinkModule() {
        super(Category.PLAYER);
        registerProperties(flushAutomatically, flushIntervalMs);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (!Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class).isBlinking())
            return;

        if (flushAutomatically.getProperty() && timer.finished(flushIntervalMs.getProperty().longValue())) {
            BlinkComponent blink = Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class);
            blink.stopBlinking();
            blink.startBlinking();
            timer.reset();
        }
    }

    @Override
    public void onEnable() {
        timer.reset();
        Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Samsara.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
        super.onDisable();
    }
}
