package cc.samsara.interfaces.access;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface GlTextureItf {
    Object samsara$getOrCreateSkikoImage(Object context, RenderTarget framebuffer, boolean hasAlpha);
}