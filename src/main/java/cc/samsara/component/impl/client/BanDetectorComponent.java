package cc.samsara.component.impl.client;

import cc.samsara.component.Component;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

/**
 * @author Kawase
 * @since 31.08.2025
 */
public class BanDetectorComponent extends Component {
    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        if (packetEvent.getPacket() instanceof ClientboundSystemChatPacket gameMessageS2CPacket &&
                gameMessageS2CPacket.content().getString().equalsIgnoreCase("An exception occurred in your connection, so you have been routed to limbo!")
        ) {
            ChatUtil.print("Limbo Detected at" + System.currentTimeMillis());
        }
    }
}
