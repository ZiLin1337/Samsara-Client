package cc.samsara.property.properties;

import cc.samsara.property.Property;

public class BooleanProperty extends Property<Boolean> {
    public BooleanProperty(String name, Boolean toggled) {
        super(name, toggled);
    }
}
