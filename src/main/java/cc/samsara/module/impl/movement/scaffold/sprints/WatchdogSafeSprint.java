package cc.samsara.module.impl.movement.scaffold.sprints;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldModule;
import cc.samsara.module.impl.movement.SpeedModule;
import cc.samsara.util.player.MoveUtil;

public class WatchdogSafeSprint extends SubModule {
    private final ScaffoldModule sc = (ScaffoldModule) getParentClass();

    public WatchdogSafeSprint(Module parentClass) {
        super(parentClass,"Watchdog Safe");
    }
    //so this class is useless either u redo it or remove it

    @EventTarget
    public void onMotion(MotionEvent event) {
        SpeedModule speedModule = Samsara.getInstance().getModuleManager().getModule(SpeedModule.class);

        if (mc.options.keyJump.isDown()) {
            return;
        }

        if (MoveUtil.isGoingDiagonally()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.98, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 0.98);
        }

        if (MoveUtil.isPressingForwardAndStrafe()) {
            if (speedModule.isToggled())
                speedModule.toggle();
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.5, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 0.5);
        }
    }
}
