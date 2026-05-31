package cc.samsara.event.events.impl.game;

import cc.samsara.event.events.Event;
import cc.samsara.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NiggerEvent extends EventCancellable {
    private boolean sprint;
}
