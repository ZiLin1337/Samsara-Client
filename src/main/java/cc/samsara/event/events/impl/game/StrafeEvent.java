package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.callables.EventCancellable;
import cc.samsara.event.events.callables.EventDual;
import cc.samsara.event.types.EventModes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class StrafeEvent extends EventDual {
    private final Vec3 type;

    public StrafeEvent(Vec3 type, EventModes eventModes) {
        super(eventModes);
        this.type = type;
    }
}
