package cc.samsara.module.impl.player;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.ModeProperty;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.util.Data;

public class GameSpeedModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode","Custom", "Normal","Custom");
    private final NumberProperty timer = new NumberProperty("Timer", 1, 0, 10, 0.1f).setVisible(() -> mode.is("Normal"));
    private final NumberProperty groundTimer = new NumberProperty("Ground Timer", 1, 0, 10, 0.1f).setVisible(() -> mode.is("Custom"));
    private final NumberProperty airTimer = new NumberProperty("Air Timer", 1, 0, 10, 0.1f).setVisible(() -> mode.is("Custom"));

    public GameSpeedModule() {
        super(Category.EXPLOIT);
        registerProperties(mode, timer, groundTimer, airTimer);
    }

    @Override
    public void onDisable() {
        Data.timer = 1f;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        switch (mode.getProperty()) {
            case "Normal" -> Data.timer = timer.getProperty().floatValue();
            case "Custom" -> {
                if (mc.player.onGround()) {
                    Data.timer = groundTimer.getProperty().floatValue();
                } else {
                    Data.timer = airTimer.getProperty().floatValue();
                }
            }
        }
    }
}
