package cc.samsara.module.impl.combat;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.NumberProperty;


public class ReachModule extends Module {

    public final NumberProperty reach = new NumberProperty("Reach", 3.0f, 3.0f, 6.0f, 0.05f);
    public final BooleanProperty block_reach = new BooleanProperty("Modify Block Reach", false);
    public final NumberProperty blockReach = new NumberProperty("Block Reach", 3.0f, 3.0f, 6.0f, 0.05f).setVisible(() -> block_reach.getProperty());

    public ReachModule() {
        super(Category.COMBAT);
        registerProperties(reach, block_reach, blockReach);
    }

    @EventTarget
    public void onTick(UpdateEvent e) {
        setSuffix(reach.getProperty().toString());
    }

}
