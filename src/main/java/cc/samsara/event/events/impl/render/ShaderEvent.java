package cc.samsara.event.events.impl.render;

import cc.samsara.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShaderEvent implements Event {
    private final float width, height;
}
