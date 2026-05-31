package cc.samsara.module.impl.client;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;

public class AutoSaveModule extends Module {
    public final BooleanProperty autoSaveConfig = new BooleanProperty("Auto Save Config", true);
    public final NumberProperty autoSaveConfigDelay = new NumberProperty("Auto Save Config Delay", 300000, 1000, 3600000f, 1000);

    public AutoSaveModule() {
        super(Category.EXPLOIT);
    }
}
