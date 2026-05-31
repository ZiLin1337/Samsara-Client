package cc.samsara.render.animation.cubicbezier;

public class CubicBezier {
    private final double x1, y1, x2, y2;

    public CubicBezier(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public double sampleCurveX(double t) {
        return ((1 - t) * (1 - t) * (1 - t)) * 0
                + 3 * ((1 - t) * (1 - t)) * t * x1
                + 3 * (1 - t) * (t * t) * x2
                + (t * t * t) * 1;
    }

    public double sampleCurveY(double t) {
        return ((1 - t) * (1 - t) * (1 - t)) * 0
                + 3 * ((1 - t) * (1 - t)) * t * y1
                + 3 * (1 - t) * (t * t) * y2
                + (t * t * t) * 1;
    }

    public double sampleCurveDerivativeX(double t) {
        return 3 * ((1 - t) * (1 - t)) * (x1 - 0)
                + 6 * (1 - t) * t * (x2 - x1)
                + 3 * (t * t) * (1 - x2);
    }

    public double solveCurveX(double x, double epsilon) {
        double t0, t1, t2, x2, d2;
        int i;

        for (t2 = x, i = 0; i < 8; i++) {
            x2 = sampleCurveX(t2) - x;
            if (Math.abs(x2) < epsilon) return t2;
            d2 = sampleCurveDerivativeX(t2);
            if (Math.abs(d2) < 1e-6) break;
            t2 = t2 - x2 / d2;
        }

        t0 = 0.0;
        t1 = 1.0;
        t2 = x;

        if (t2 < t0) return t0;
        if (t2 > t1) return t1;

        while (t0 < t1) {
            x2 = sampleCurveX(t2);
            if (Math.abs(x2 - x) < epsilon) return t2;
            if (x > x2) t0 = t2;
            else t1 = t2;
            t2 = (t1 - t0) / 2.0 + t0;
        }

        return t2;
    }

    public double solve(double x, double epsilon) {
        return sampleCurveY(solveCurveX(x, epsilon));
    }

    public float solve(float x) {
        return (float) solve(x, 1e-6);
    }
}
