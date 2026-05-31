package cc.samsara.render.animation;

import cc.samsara.render.animation.cubicbezier.CubicBezier;
import cc.samsara.render.animation.cubicbezier.impl.EaseStandard;

/**
 * 动画工厂。提供 Fluent Builder 风格的动画创建。
 */
public class AnimationFactory {

    /**
     * 创建一个从 start 到 end 的线性动画。
     * @param start 起始值
     * @param end 结束值
     * @param durationMs 持续时间（毫秒）
     */
    public Animation create(float start, float end, long durationMs) {
        return new DummyAnimation(durationMs, start, end);
    }

    /**
     * 创建一个使用贝塞尔曲线的动画。
     * @param start 起始值
     * @param end 结束值
     * @param durationMs 持续时间（毫秒）
     * @param curve 贝塞尔曲线
     */
    public CubicBezierAnimation create(float start, float end, long durationMs, CubicBezier curve) {
        return new CubicBezierAnimation(durationMs, start, end, curve);
    }

    /**
     * 创建一个简易追赶动画。
     */
    public SimpleAnimation simple() {
        return new SimpleAnimation();
    }

    /**
     * 使用 EaseStandard 曲线（默认缓动）的贝塞尔动画。
     */
    public static class CubicBezierAnimation {
        private final long durationMs;
        private final float start, end;
        private final CubicBezier curve;
        private float timePassed = 0f;

        CubicBezierAnimation(long durationMs, float start, float end, CubicBezier curve) {
            this.durationMs = durationMs;
            this.start = start;
            this.end = end;
            this.curve = curve;
        }

        public float getValue() {
            timePassed += (float) Delta.getDeltaTime();
            float progress = Math.min(timePassed / durationMs, 1f);
            return start + (end - start) * curve.solve(progress);
        }

        public boolean isFinished() {
            return timePassed >= durationMs;
        }

        public float getEnd() {
            return end;
        }
    }
}
