package cc.samsara.module;

import cc.samsara.Samsara;
import cc.samsara.interfaces.IAccess;
import cc.samsara.property.Property;
import cc.samsara.util.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SubModule extends Data implements IAccess {
    private final Module parentClass;
    private boolean hooked;
    private String name;

    @Setter
    private boolean selected;

    public SubModule(final Module parentClass, String name) {
        this.parentClass = parentClass;
        this.name = name;
    }

    public void setHooked(boolean hooked) {
        this.hooked = hooked;

        if (hooked) {
            if (parentClass.isToggled())
                onEnable();
        } else {
            onDisable();
        }
    }

    // wild
    public String getFormatedName() {
        return name;
    }

    public void registerPropertyToParentClass(Property<?> property) {
        parentClass.registerProperty(property.setVisible(this::isSelected));
    }

    // we have to do a for loop cuz I need to check the visibility
    public <T> void registerPropertiesToParentClass(Property<?>... properties) {
        for (Property<?> property : properties) {
            parentClass.registerProperty(property.setVisible(this::isSelected));
        }
    }

    public void onEnable() {
        Samsara.getInstance().getEventManager().register(this);
    }

    public void onDisable() {
        Samsara.getInstance().getEventManager().unregister(this);
    }
}
