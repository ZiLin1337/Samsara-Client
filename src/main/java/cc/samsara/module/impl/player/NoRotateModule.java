package cc.samsara.module.impl.player;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.event.events.impl.network.PacketEvent;
import cc.samsara.event.types.EventModes;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.ModeProperty;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class NoRotateModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Cancel", "Cancel", "Adjust");

    public NoRotateModule() {
        super(Category.EXPLOIT);
        registerProperties(mode);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
       this.setSuffix(mode.getProperty());
    }

    @EventTarget
    public void onPacket(PacketEvent event){
        if(event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            switch (mode.getProperty()){
                case "Cancel":
                    event.setCancelled(true);
                    break;
                case "Adjust":
                    break;
            }
        }
    }
}
