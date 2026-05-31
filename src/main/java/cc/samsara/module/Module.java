package cc.samsara.module;

import cc.samsara.Samsara;
import cc.samsara.event.events.impl.client.SuffixChangeEvent;
import cc.samsara.event.events.impl.client.ToggleModuleEvent;
import cc.samsara.interfaces.Fonts;
import cc.samsara.interfaces.IAccess;
import cc.samsara.module.impl.visual.NotificationsModule;
import cc.samsara.ui.notifications.render.Notification;
import cc.samsara.ui.notifications.NotificationBuilder;
import cc.samsara.property.Property;
import cc.samsara.property.properties.ClassModeProperty;
import cc.samsara.property.properties.TextProperty;
import cc.samsara.util.Data;
import club.serenityutils.modules.ModuleMetaData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Module extends Data implements IAccess, Fonts {
    public ModuleMetaData moduleMetaData = new ModuleMetaData("none", "none");
    private final Category category;
    public int keyCode;
    private final List<Property<?>> propertyList = new ArrayList<>();

    private String suffix, lastSuffix;
    private boolean toggled, hidden;

    protected Module(Category category) {
/*
        this.moduleMetaData = new ModuleMetaData(name, description + (description.endsWith(".") ? "" : "."));
*/
        this.category = category;
        this.keyCode = 0;
    }

    protected Module(Category category, int keyCode) {
/*
        this.moduleMetaData = new ModuleMetaData(name, description + (description.endsWith(".") ? "" : "."));
*/
        this.category = category;
        this.keyCode = keyCode;
    }

    public void registerProperty(Property<?> property) {
        propertyList.add(property);
    }

    public void registerProperties(Property<?>... properties) {
        propertyList.addAll(Arrays.asList(properties));
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        Samsara.getInstance().getEventManager().call(new ToggleModuleEvent());

        if (isToggled()) {
            if (Samsara.getInstance().getModuleManager().getModule(NotificationsModule.class) != null &&
                    Samsara.getInstance().getModuleManager().getModule(NotificationsModule.class).displayNotificationOnToggle.getProperty())
                NotificationBuilder.create()
                        .notification("Enabled " + getName(), Notification.NotificationType.INFO)
                        .duration(1000)
                        .build();

            onEnable();
        } else {
            if (Samsara.getInstance().getModuleManager().getModule(NotificationsModule.class) != null &&
                    Samsara.getInstance().getModuleManager().getModule(NotificationsModule.class).displayNotificationOnToggle.getProperty())
                NotificationBuilder.create()
                        .notification("Disabled " + getName(), Notification.NotificationType.INFO)
                        .duration(1000)
                        .build();

            // dad **curse of vanish** ahhh comment
            // we buy the milk
            onDisable();
        }
    }

    private void setSubModesHookState(boolean state) {
        for (Property<?> property : propertyList) {
            if (property instanceof ClassModeProperty classModeProperty) {
                final SubModule subModule = classModeProperty.getProperty();
                if (subModule != null && subModule.getParentClass().equals(this)) {
                    subModule.setHooked(state);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <P extends Property<?> /* higher bound */> P getPropertyByName(String name) {
        return (P) propertyList.stream().filter(property -> property.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public TextProperty infoLine(String text) {
        return new TextProperty(text);
    }

    public void toggle() {
        setToggled(!isToggled());
    }

    public void onEnable() {
        setSubModesHookState(true);
        Samsara.getInstance().getEventManager().register(this);
    }

    public void onDisable() {
        setSubModesHookState(false);
        Samsara.getInstance().getEventManager().unregister(this);
    }

    public String getDisplayName(boolean doesHaveSuffix) {
        String idk = getName();

        if (suffix != null) {
            idk += doesHaveSuffix ? getPreFix() + getSuffix() : "";
        }

        return idk;
    }

    public String getDesc() {
        return moduleMetaData.getDescription();
    }

    // add crash.
    public String getName() {
        return moduleMetaData.getName();
    }

    public void setSuffix(String suffix) {
        if (suffix == null && this.suffix != null || suffix != null && !suffix.equals(this.suffix)) {
            // Fire event with old + new suffix
            Samsara.getInstance().getEventManager().call(new SuffixChangeEvent());
        }

        this.lastSuffix = this.suffix;
        this.suffix = suffix;
    }


    private String getPreFix() {
        //todo: make this have multiple stuff like -, # and so on
        return ChatFormatting.GRAY +  " ";
    }
}
