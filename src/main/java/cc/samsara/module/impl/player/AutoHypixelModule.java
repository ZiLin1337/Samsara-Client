package cc.samsara.module.impl.player;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.PlayerJoinEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.ModeProperty;
import cc.samsara.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

/**
 * @author Kawase
 * @since 03.10.2025
 */
public class AutoHypixelModule extends Module {
    private final BooleanProperty autoLanguageSet = new BooleanProperty("Auto Set Language", true);

    private final BooleanProperty autoPlay = new BooleanProperty("Auto Play", true);
    private final ModeProperty autoPlayMode = new ModeProperty("Auto Play Mode", "Skywars Normal",
            "Skywars Normal", "Skywars Insane", "Bedwars Solo", "Bedwars Duo", "Bedwars Trio", "Bedwars Squad"
    );

    public AutoHypixelModule() {
        super(Category.PLAYER);
        this.registerProperties(autoLanguageSet, autoPlay, autoPlayMode.setVisible(autoPlay::getProperty));
    }

    @EventTarget
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!autoLanguageSet.getProperty())
            return;

        ChatUtil.print("set language");
        mc.player.connection.sendChat("/language english");
    }

    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        if (!autoPlay.getProperty())
            return;

        if (packetEvent.getPacket() instanceof ClientboundSystemChatPacket gameMessageS2CPacket &&
                (gameMessageS2CPacket.content().getString().equalsIgnoreCase("You have been eliminated!") ||
                        gameMessageS2CPacket.content().getString().equalsIgnoreCase("You died! Want to play again? Click here! "))
        ) {
            String command = switch (autoPlayMode.getProperty()) {
                case "Skywars Normal" -> "/play solo_normal";
                case "Skywars Insane" -> "/play solo_insane";
                case "Bedwars Solo" -> "/play bedwars_eight_one";
                case "Bedwars Duo" -> "/play bedwars_eight_two";
                case "Bedwars Trio" -> "/play bedwars_four_three";
                case "Bedwars Squad" -> "/play bedwars_four_four";
                default -> null;
            };

            if (command != null) {
                mc.player.connection.sendChat(command);
            }
        }
    }
}
