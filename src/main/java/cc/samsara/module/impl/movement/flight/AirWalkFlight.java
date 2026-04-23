package cc.samsara.module.impl.movement.flight;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.BlockShapeEvent;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;

public class AirWalkFlight extends SubModule {
    private final BooleanProperty spoofGround = new BooleanProperty("Spoof Ground", false);

    public AirWalkFlight(Module parentClass) {
        super(parentClass, "Air Walk");
        this.registerPropertyToParentClass(spoofGround);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (spoofGround.getProperty())
            event.setOnGround(true);
    }

    @EventTarget
    public void onBlockShape(BlockShapeEvent event) {
        if (event.getPos().getY() < mc.player.getBlockY()) {
            event.setShape(Shapes.block());
        }
    }
}
