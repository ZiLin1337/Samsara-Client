package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.callables.EventCancellable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlowDownEvent extends EventCancellable {
    public float slowDown;
}
