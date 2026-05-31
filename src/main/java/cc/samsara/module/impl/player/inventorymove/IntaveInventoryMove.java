package cc.samsara.module.impl.player.inventorymove;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.player.InventoryMoveModule;
import net.minecraft.client.KeyMapping;

public class IntaveInventoryMove extends SubModule {
    private final InventoryMoveModule parentClass;

    public IntaveInventoryMove(Module parentClass) {
        super(parentClass, "Intave");
        this.parentClass = (InventoryMoveModule) parentClass;
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (mc.screen == null || !parentClass.ScreenCheck()) {
            return;
        }

        KeyMapping.setAll();

        mc.options.keyShift.setDown(true);
        mc.options.keyJump.setDown(false);
    }
}
