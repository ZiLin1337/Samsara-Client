package cc.samsara.module.impl.visual;

import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.ModeProperty;

public class NotificationsModule extends Module {
    public final BooleanProperty displayNotificationOnToggle = new BooleanProperty("Notification on Toggle", true);
    public final ModeProperty mode = new ModeProperty("Mode", "Modern", "Modern", "Legacy");
    public final BooleanProperty popSound = new BooleanProperty("Pop Sound", true);

    public NotificationsModule() {
        super(Category.VISUAL);
        this.registerProperties(mode, displayNotificationOnToggle, popSound);
    }
}
