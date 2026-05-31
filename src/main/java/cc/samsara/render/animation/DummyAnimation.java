package cc.samsara.render.animation;

public class DummyAnimation extends Animation {
    public DummyAnimation(float duration, float start, float end) {
        super(duration, start, end);
    }

    @Override
    protected float animate(float progress) {
        return progress;
    }
}
