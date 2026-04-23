package cc.samsara.module.impl.movement.scaffold.towers;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldWalkModule;
import cc.samsara.module.impl.movement.SpeedModule;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.util.math.TimeUtil;
import cc.samsara.util.player.MoveUtil;
import cc.samsara.util.player.PlayerUtil;

public class WatchdogVanillaTower extends SubModule {
    private final BooleanProperty edge = new BooleanProperty("Edge", true);

    public boolean canTower = false;
    public boolean Tower = false;

    private final ScaffoldWalkModule sc = (ScaffoldWalkModule) getParentClass();
    private final TimeUtil towerDelayTimer = new TimeUtil();

    public WatchdogVanillaTower(Module parentClass) {
        super(parentClass,"Watchdog Vanilla");
        this.registerPropertyToParentClass(edge);
    }

    @Override
    public void onEnable() {
        canTower = false;
        Tower = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Tower = false;
        super.onDisable();
    }

    private boolean conditions() {
        return mc.options.keyJump.isDown() && canTower;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        boolean jumpPressed = mc.options.keyJump.isDown();
        SpeedModule speedModule = Samsara.getInstance().getModuleManager().getModule(SpeedModule.class);
        if (speedModule.isToggled() && jumpPressed)
            speedModule.toggle();

        if (conditions()) {
            MoveUtil.strafe(0.22f);
            switch (offGroundMotionTicks) {
                case 0 -> PlayerUtil.setMotionY(0.41999998688698f);
                case 1 -> PlayerUtil.setMotionY(0.33);
                case 2 -> {
                    offGroundMotionTicks = -1;
                    PlayerUtil.setMotionY(1 - mc.player.getY() % 1);
                }
            }
        } else {
            if (!canTower && mc.player.onGround()) canTower = true;
            else if (!mc.player.onGround()) canTower = false;
        }
    }
}