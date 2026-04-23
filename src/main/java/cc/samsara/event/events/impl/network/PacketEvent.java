package cc.samsara.event.events.impl.network;

import cc.samsara.event.events.callables.EventDual;
import cc.samsara.event.types.EventModes;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;

@Getter
@Setter
public class PacketEvent extends EventDual {
    private Packet<?> packet;

    public PacketEvent(Packet<?> packet, EventModes eventModes) {
        super(eventModes);
        this.packet = packet;
    }
}
