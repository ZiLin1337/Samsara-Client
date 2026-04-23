package cc.samsara.module.impl.player;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import samsara.mixin.accessor.entity.LivingEntityAccessor;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.module.impl.movement.ScaffoldModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;

public class NoJumpDelayModule extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 0, 0, 4, 1);
    public final BooleanProperty notWhileScaffold = new BooleanProperty("Not while Scaffold", true);

    public NoJumpDelayModule() {
        super(Category.PLAYER);
        registerProperties(delay, notWhileScaffold);
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (mc.player == null || !mc.player.onGround()) {
            return;
        }

        if (notWhileScaffold.getProperty() && (Samsara.getInstance().getModuleManager().getModule(ScaffoldModule.class).isToggled())) {
            return;
        }
        ((LivingEntityAccessor) mc.player).setNoJumpDelay(delay.getProperty().intValue());
    }
}