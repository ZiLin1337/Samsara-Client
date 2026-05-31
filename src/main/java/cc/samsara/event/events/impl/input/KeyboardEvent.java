package cc.samsara.event.events.impl.input;

import cc.samsara.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KeyboardEvent implements Event {
    private final int keyCode;
}
