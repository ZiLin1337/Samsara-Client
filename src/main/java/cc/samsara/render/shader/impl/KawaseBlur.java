package cc.samsara.render.shader.impl;

import cc.samsara.render.shader.Framebuffer;
import cc.samsara.render.shader.PostProcessRenderer;
import cc.samsara.render.shader.Shader;
import cc.samsara.render.shader.ShaderHelper;
import net.minecraft.client.MinecraftClient;

public class KawaseBlur {

    public static final KawaseBlur GUI_BLUR = new KawaseBlur();
    public static final KawaseBlur INGAME_BLUR = new KawaseBlur();

    private static final int[][] STRENGTHS = new int[][] {
        {1,125},{1,225},{2,200},{2,300},{2,425},
        {3,250},{3,325},{3,425},{3,550},{4,325},
        {4,400},{4,500},{4,600},{4,725},{4,825},
        {5,450},{5,525},{5,625},{5,725},{5,850}
    };

    private Shader shaderDown, shaderUp, shaderPassthrough;
    private final Framebuffer[] fbos = new Framebuffer[6];
    private long lastBlurTime = 0;
    private boolean firstTick = true;

    public void resize() {
        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] != null) fbos[i].resize();
            else fbos[i] = new Framebuffer(1.0 / Math.pow(2, i));
        }
    }

    public void draw(int radius) {
        if (radius < 1) radius = 1;
        if (radius > 20) radius = 20;

        if (shaderDown == null) {
            shaderDown = Shader.fromResources("samsara", "blur.vert", "blur_down.frag");
            shaderUp = Shader.fromResources("samsara", "blur.vert", "blur_up.frag");
            shaderPassthrough = Shader.fromResources("samsara", "passthrough.vert", "passthrough.frag");
        }

        if (firstTick) {
            for (int i = 0; i < fbos.length; i++) {
                if (fbos[i] == null) fbos[i] = new Framebuffer(1.0 / Math.pow(2, i));
            }
            firstTick = false;
        }

        long now = System.currentTimeMillis();
        if (now - lastBlurTime < 16) return;
        lastBlurTime = now;

        int iterations = STRENGTHS[radius - 1][0];
        double offset = STRENGTHS[radius - 1][1] / 100.0;

        PostProcessRenderer.beginRender();
        renderToFbo(fbos[0], MinecraftClient.getInstance().getFramebuffer().getColorAttachment(), shaderDown, offset);
        for (int i = 0; i < iterations; i++)
            renderToFbo(fbos[i + 1], fbos[i].texture, shaderDown, offset);
        for (int i = iterations; i >= 1; i--)
            renderToFbo(fbos[i - 1], fbos[i].texture, shaderUp, offset);

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        shaderPassthrough.bind();
        ShaderHelper.bindTexture(fbos[0].texture);
        shaderPassthrough.set("uTexture", 0);
        PostProcessRenderer.endRender();
    }

    public int getTexture() { return fbos[0].texture; }

    private void renderToFbo(Framebuffer targetFbo, int sourceText, Shader shader, double offset) {
        targetFbo.bind();
        targetFbo.setViewport();
        shader.bind();
        ShaderHelper.bindTexture(sourceText);
        shader.set("uTexture", 0);
        shader.set("uHalfTexelSize", 0.5 / targetFbo.width, 0.5 / targetFbo.height);
        shader.set("uOffset", offset);
        PostProcessRenderer.render();
    }
}
