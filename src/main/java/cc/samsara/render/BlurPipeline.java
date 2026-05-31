package cc.samsara.render;

import cc.samsara.render.shader.impl.KawaseBlur;

/**
 * 模糊管线入口。封装 KawaseBlur 的调用。
 * 可扩展为支持区域模糊、质量选择等。
 */
public class BlurPipeline {

    private final KawaseBlur blur;

    public BlurPipeline(KawaseBlur blur) {
        this.blur = blur;
    }

    /** 对整个屏幕应用给定半径的模糊 */
    public void blur(int radius) {
        blur.draw(radius);
    }

    /** 窗口大小变化时重建帧缓冲 */
    public void resize() {
        blur.resize();
    }

    /** 获取底层 KawaseBlur 实例的纹理 ID */
    public int getTexture() {
        return blur.getTexture();
    }
}
