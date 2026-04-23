package cc.samsara.module.impl.combat.velocity;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.TickEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.property.properties.NumberProperty;

public class MMCVelocity extends SubModule {
    private final NumberProperty value = new NumberProperty("MMC Value", 0.8f,0.75f , 1f, 0.01f);

    public MMCVelocity(Module parentClass)  {
        super(parentClass,"MMC");
        registerPropertyToParentClass(value);
    }

    @EventTarget
    public void onMotion(TickEvent event) {
        if (mc.player.hurtTime > 1) {
            mc.player.setDeltaMovement( mc.player.getDeltaMovement().x * value.getProperty().floatValue(),
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z * value.getProperty().floatValue()
            );

            if(mc.player.onGround() && !mc.options.keyJump.isDown()){
                mc.player.jumpFromGround();
            }
        }
    }
}
