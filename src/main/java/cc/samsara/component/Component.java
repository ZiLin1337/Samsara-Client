package cc.samsara.component;

import cc.samsara.Samsara;
import cc.samsara.interfaces.Fonts;
import cc.samsara.interfaces.IAccess;
import cc.samsara.util.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component extends Data implements IAccess, Fonts {
    public static boolean activate;

    public Component() {
        onEnable();
    }

    public void onEnable() {
        Samsara.getInstance().getEventManager().register(this);
    }

    public void onDisable() {
        Samsara.getInstance().getEventManager().unregister(this);
    }
}
