package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.callables.EventDual;
import cc.samsara.event.types.EventModes;
import lombok.Getter;
import net.minecraft.world.entity.Entity;

@Getter
public class EntityInteractEvent extends EventDual {
    private final Entity target;

    public EntityInteractEvent(Entity target, EventModes eventMode) {
        super(eventMode);
        this.target = target;
    }
}
