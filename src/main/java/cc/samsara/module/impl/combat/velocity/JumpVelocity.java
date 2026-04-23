package cc.samsara.module.impl.combat.velocity;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.input.InputTickEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.NumberProperty;

public class JumpVelocity extends SubModule {
    private final NumberProperty chance = new NumberProperty("Chance", 100, 0, 100, 1);

    public JumpVelocity(Module parent) {
        super(parent, "Jump");
        registerPropertyToParentClass(chance);
    }

    @EventTarget
    public void onInputTick(InputTickEvent event) {
        if (mc.player.hurtTime > 0) {
            if (Math.random() * 100 < chance.getProperty().intValue()) {
                event.up = true;
                if (mc.player.hurtTime == 9) {
                    event.jump = true;
                }
            }
        }
    }
}