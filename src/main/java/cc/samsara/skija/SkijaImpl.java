package cc.samsara.skija;

import cc.samsara.Samsara;
import cc.samsara.event.events.impl.render.Render2DEvent;
import cc.samsara.event.events.impl.render.ShaderEvent;
import cc.samsara.interfaces.IAccess;
import cc.samsara.skija.utils.SkijaUtil;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

/**
 * Simplified SkijaImpl - skija library may not be available at runtime,
 * so this is a stub that provides the interface without direct skija dependencies.
 */
public class SkijaImpl implements IAccess {

    private int width, height;

    public SkijaImpl() {
        Samsara.getInstance().getEventManager().register(this);
    }

    public void init(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void begin() {
        final var win = mc.getWindow();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0f, 0f, 0f, 0f);
    }

    public void render() {
        final var win = mc.getWindow();
        Samsara.getInstance().getEventManager().call(new Render2DEvent());
    }

    public void end() {
        // no-op in stub mode
    }

    public Object framebufferImage() {
        return null;
    }
}
