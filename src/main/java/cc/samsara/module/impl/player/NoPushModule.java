package cc.samsara.module.impl.player;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;

public class NoPushModule extends Module {
    public final BooleanProperty playerNoPush = new BooleanProperty("Player No Push", true);
    public final BooleanProperty waterNoPush = new BooleanProperty("Water No Push", false);
    public NoPushModule() {
        super(Category.PLAYER);
        registerProperties(playerNoPush, waterNoPush);
    }
}
