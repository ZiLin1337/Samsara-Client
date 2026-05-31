package cc.samsara.component;

import cc.samsara.Samsara;
import cc.samsara.component.impl.client.BanDetectorComponent;
import cc.samsara.component.impl.client.BedWhiteListComponent;
import cc.samsara.component.impl.client.ReEnableComponent;
import cc.samsara.component.impl.network.BlinkComponent;
import cc.samsara.component.impl.network.PacketLossDetector;
import cc.samsara.component.impl.player.RotationComponent;
import cc.samsara.component.impl.ui.NotificationComponent;
import cc.samsara.component.impl.ui.ProgressBarComponent;
import cc.samsara.manager.Manager;

import java.util.Objects;


public class ComponentManager extends Manager<Component> {
    public void registerComponents() {
        this.register(
                new RotationComponent(),
                new NotificationComponent(),
                new ReEnableComponent(),
                new PacketLossDetector(),
                new BlinkComponent(),
                new ProgressBarComponent(),
                new BanDetectorComponent(),
                new BedWhiteListComponent()
        );

        Samsara.getInstance().getEventManager().register(this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(final Class<T> clazz) {
        return (T) this.getBy(module -> Objects.equals(module.getClass(), clazz));
    }
}
