package cc.samsara.event.events.impl.input;

import cc.samsara.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatInputEvent extends EventCancellable {
    private String input;
}
