package cc.samsara.module.impl.movement;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;


public class SafeWalkModule extends Module {
    public final BooleanProperty blocksOnly = new BooleanProperty("Blocks only", false);
    public final BooleanProperty pitchCheck = new BooleanProperty("Pitch check", false);
    public final BooleanProperty backwards = new BooleanProperty("Backwards Only", false);

    public SafeWalkModule() {
        super(Category.MOVEMENT);
        registerProperties(blocksOnly, pitchCheck,backwards);
    }
}
