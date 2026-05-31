package cc.samsara.module.impl.player.nofall;

import cc.samsara.Samsara;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.module.impl.movement.ScaffoldModule;
import cc.samsara.property.properties.NumberProperty;
import cc.samsara.util.player.PlayerUtil;

public class VulcanNofall extends SubModule {
    private final NumberProperty motion = new NumberProperty("Motion", 10f, 0f, 10f, 1f);

    public VulcanNofall(Module parentClass){
        super(parentClass,"Vulcan");
        this.registerPropertyToParentClass(motion);
    }
    private int fallticks;

    @Override
    public void onDisable(){
        PlayerUtil.setMotionY(mc.player.getDeltaMovement().y());
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            fallticks = 0;
        }
        if (mc.player.fallDistance - mc.player.getDeltaMovement().y > 3 && !Samsara.getInstance().getModuleManager().getModule(ScaffoldModule.class).isToggled()) {
            fallticks++;
            if (fallticks > 1) {
                event.setOnGround(true);
            }
            if (fallticks > 2) {
                PlayerUtil.setMotionY(-motion.getProperty().floatValue());
            }
        }
    }

}
