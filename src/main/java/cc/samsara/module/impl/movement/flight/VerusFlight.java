package cc.samsara.module.impl.movement.flight;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.player.MoveUtil;
import net.minecraft.core.Direction;

public class VerusFlight extends SubModule {

    public VerusFlight(Module parentClass) {
        super(parentClass, "Verus Glide");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        mc.player.setDeltaMovement(mc.player.getDeltaMovement().with(Direction.Axis.Y, -0.078400001525878));
        if(!mc.player.onGround())
            MoveUtil.strafe(0.37);
    }
}
