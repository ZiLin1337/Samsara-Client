package cc.samsara.render;

import cc.samsara.render.animation.Animation;
import cc.samsara.render.animation.AnimationFactory;
import cc.samsara.render.animation.Delta;
import cc.samsara.render.shader.impl.KawaseBlur;

/**
 * 统一渲染引擎入口。
 * 按接口设计方案的混合模式（Context + Fluent Builder）提供。
 */
public final class RenderEngine {

    private static RenderEngine instance;

    private final AnimationFactory animations;
    private final BlurPipeline blur;
    private final KawaseBlur guiBlur;
    private final KawaseBlur ingameBlur;

    private RenderEngine() {
        this.animations = new AnimationFactory();
        this.guiBlur = KawaseBlur.GUI_BLUR;
        this.ingameBlur = KawaseBlur.INGAME_BLUR;
        this.blur = new BlurPipeline(guiBlur);
    }

    public static RenderEngine get() {
        if (instance == null) {
            instance = new RenderEngine();
        }
        return instance;
    }

    // ========== 帧更新 ==========

    /** 必须在主渲染循环每帧调用，驱动动画帧时间 */
    public void tick() {
        Delta.update();
    }

    // ========== 动画 ==========

    public AnimationFactory animations() {
        return animations;
    }

    // ========== 模糊 ==========

    public BlurPipeline blur() {
        return blur;
    }

    // ========== 颜色系统（待实现） ==========

    // 颜色系统移植完成后在此添加:
    // public DynamicColors colors() { ... }
}
