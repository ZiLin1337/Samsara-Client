package cc.samsara.module.impl.player.nofall;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.PostMotionEvent;
import samsara.mixin.accessor.entity.EntityAccessor;
import cc.samsara.module.Module;
import cc.samsara.module.SubModule;
import cc.samsara.util.network.PacketUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class UniversalNofall extends SubModule {

    public UniversalNofall(Module parentClass){
        super(parentClass,"Universal");
    }

    @EventTarget
    public void onPostMotion(PostMotionEvent event) {
            double motionY = mc.player.getDeltaMovement().y;
            if (mc.player.fallDistance > 3 && motionY < 0 && ((EntityAccessor) mc.player).callCollide(mc.player.getDeltaMovement()).y > motionY) {
                PacketUtil.send(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY() + 1E-14, mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(),
                        false,
                        false
                ));
            }
        }
    }
