package cc.samsara.module.impl.player;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class FlagDetectorModule extends Module {
    public FlagDetectorModule() {
        super(Category.PLAYER);
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ChatUtil.print("Flag detected!");
        }
    }
}
