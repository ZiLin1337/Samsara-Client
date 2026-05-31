package cc.samsara.module.impl.movement;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.module.impl.movement.flight.*;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.ClassModeProperty;
import cc.samsara.util.player.MoveUtil;

public class FlightModule extends Module {
    private final ClassModeProperty classModeProperty = new ClassModeProperty("Mode",
            new AirWalkFlight(this), new BlockFlight(this),
            new VanillaFlight(this), new VulcanFlight(this),
            new CubecraftFlight(this), new GrimFlight(this),
            new VerusFlight(this)
    );
    private final BooleanProperty stopOnDisable = new BooleanProperty("Stop on Disable",true);

    public FlightModule() {
        super(Category.MOVEMENT);
        registerProperties(classModeProperty,
                stopOnDisable
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable(){
        if (stopOnDisable.getProperty())
            MoveUtil.stop();

        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        this.setSuffix(classModeProperty.getProperty().getName());
    }
}