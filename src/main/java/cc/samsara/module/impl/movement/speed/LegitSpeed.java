package cc.samsara.module.impl.movement.speed;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldModule;

public class LegitSpeed extends SubModule {

    public LegitSpeed(Module parentClass) {
        super(parentClass, "Legit");
    }

    @EventTarget
    public void onUpdate(InputTickEvent event) {
        if (Samsara.getInstance().getModuleManager().getModule(ScaffoldModule.class).isToggled())
            return;

       event.jump = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
