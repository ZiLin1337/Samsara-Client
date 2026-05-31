package cc.samsara.module.impl.movement.speed;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.util.player.MoveUtil;

public class VanillaSpeed extends SubModule {
    private final NumberProperty speed = new NumberProperty("Speed", 1, 0, 10, 0.1f);

    public VanillaSpeed(Module parentClass) {
        super(parentClass,"Vanilla");
        registerPropertyToParentClass(speed);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        MoveUtil.strafe(speed.getProperty().floatValue());
    }
}