package cc.samsara.module.impl.client;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;

public class RotationsModule extends Module {
    public final BooleanProperty movementCorrection = new BooleanProperty("Movement Correction", false),
        oldHitBoxOffset = new BooleanProperty("Old Hit Box Offset", false),
        modernHitVec = new BooleanProperty("Modern Hit Vec", true);

    public RotationsModule() {
        super(Category.PLAYER);
        this.registerProperties(movementCorrection, oldHitBoxOffset, modernHitVec);
    }
}
