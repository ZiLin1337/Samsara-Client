package cc.samsara.module.impl.movement.flight;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.MotionEvent;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.math.TimeUtil;
import cc.samsara.util.player.PlayerUtil;
import cc.samsara.util.render.ChatUtil;
import net.minecraft.world.level.block.SlimeBlock;

public class VulcanFlight extends SubModule {
    private final TimeUtil timeUtil = new TimeUtil();

    public VulcanFlight(Module parentClass) {
        super(parentClass, "Vulcan");
    }

    @Override
    public void onEnable() {
        if (!(mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() instanceof SlimeBlock))
            ChatUtil.print("You need to be on a slime block to use this flight.");

        super.onEnable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround() && mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() instanceof SlimeBlock) {
            PlayerUtil.setMotionY(4);
            timeUtil.reset();
        }

        if (timeUtil.finished(1500)) {
            if (mc.player.fallDistance > 2) {
                mc.player.fallDistance = 0;
            }

            PlayerUtil.setMotionY(mc.player.tickCount % 3 != 0 ? -0.0972 : PlayerUtil.getMotionY() + 0.026);
        }
    }
}
