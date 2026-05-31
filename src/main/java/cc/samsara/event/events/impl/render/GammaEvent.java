package cc.samsara.event.events.impl.render;

import cc.samsara.event.events.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GammaEvent implements Event {
    public int x, y, color;

    public GammaEvent(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }
}
