package io.github.itzispyder.clickcrystals.gui.misc.animators;

import io.github.itzispyder.clickcrystals.util.MathUtils;

/**
 * A 0 to 1 value that eases towards whatever state it is told it is in, frame by frame.
 * <p>
 * Unlike {@link Animator} it can turn around halfway, which is what a highlight following
 * the mouse has to do.
 */
public class Hover {

    private final long length;
    private double value;
    private long last;

    public Hover(long length) {
        this.length = length;
        this.last = System.currentTimeMillis();
    }

    public double update(boolean on) {
        return update(on ? 1.0 : 0.0);
    }

    public double update(double target) {
        long now = System.currentTimeMillis();
        double step = MathUtils.clamp((now - last) / (double)length, 0.0, 1.0);
        this.last = now;
        this.value = MathUtils.lerp(value, target, step);
        return value;
    }

    public double value() {
        return value;
    }
}
