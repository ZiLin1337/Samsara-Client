package cc.samsara.module.impl.visual;

import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.UpdateEvent;
import cc.samsara.module.Category;
import cc.samsara.module.Module;
import cc.samsara.property.properties.BooleanProperty;
import cc.samsara.property.properties.ColorProperty;

import java.awt.*;

public class GlowEspModule extends Module {
    public final BooleanProperty reloadShader = new BooleanProperty("Reload Shader", false);
    public final ColorProperty color = new ColorProperty("Color", Color.RED);

    private boolean didReload = false;
    public GlowEspModule() {
        super(Category.VISUAL);
        this.registerProperties(color, reloadShader);
    }

    // this is retarded but since mc has removed .set for uniforms there is no way to update the shader dynamically.
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (reloadShader.getProperty() && !didReload) {
            mc.reloadResourcePacks();
            didReload = true;
        }

        if (!reloadShader.getProperty() && didReload)
            didReload = false;
    }
}