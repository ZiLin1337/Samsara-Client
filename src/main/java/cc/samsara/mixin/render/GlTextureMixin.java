package cc.samsara.mixin.render;

import cc.samsara.interfaces.access.GlTextureItf;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlTexture.class)
public class GlTextureMixin implements GlTextureItf {

    @Shadow
    @Final
    protected int id;
    @Shadow
    @Final
    private Int2IntMap fboCache;
    @Unique
    private Object image; // Changed from Image to Object

    @Override
    public Object samsara$getOrCreateSkikoImage(Object context, RenderTarget framebuffer, boolean hasAlpha) {
        // Simplified - skija not available at runtime
        return null;
    }

    @Inject(method = "destroyImmediately", at = @At("HEAD"), cancellable = true)
    private void samsara$free(CallbackInfo ci) {
        if (image != null) {
            // image.close(); // skija Image not available
            image = null;
            IntIterator var1 = this.fboCache.values().iterator();

            while (var1.hasNext()) {
                int i = var1.next();
                GlStateManager._glDeleteFramebuffers(i);
            }

            ci.cancel();
        }
    }
}