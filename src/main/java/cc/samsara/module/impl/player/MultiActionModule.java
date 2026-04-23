package cc.samsara.module.impl.player;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;

public class MultiActionModule extends Module {
    public final BooleanProperty placeIfBreaking = new BooleanProperty("Place while mining", false);
    public final BooleanProperty breakIfUsing = new BooleanProperty("Break while using item", false);

    public MultiActionModule() {
        super(Category.PLAYER);
        registerProperties(placeIfBreaking, breakIfUsing);
    }
}
