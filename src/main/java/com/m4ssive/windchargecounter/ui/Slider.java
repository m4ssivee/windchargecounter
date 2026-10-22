package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Label + value on top, 2 px track with a knob below. Drag or scroll to change. */
public class Slider extends Widget {
    private final String label;
    private final Function<Float, String> format;
    private final float min, max, step;
    private final Supplier<Float> get;
    private final Consumer<Float> set;
    private final int fillColor;
    private boolean dragging;

    /** fillColor 0 = theme accent. */
    public Slider(int w, String label, Function<Float, String> format, float min, float max, float step,
                  Supplier<Float> get, Consumer<Float> set, int fillColor) {
        super(w, 20);
        this.label = label;
        this.format = format;
        this.min = min;
        this.max = max;
        this.step = step;
        this.get = get;
        this.set = set;
        this.fillColor = fillColor;
    }

    /** Value under the mouse, clamped to [min,max] and rounded to step. */
    public static float valueFromMouse(double mouseX, int left, int width, float min, float max, float step) {
        double ratio = width <= 0 ? 0 : (mouseX - left) / width;
        ratio = Math.max(0, Math.min(1, ratio));
        double raw = min + ratio * (max - min);
        double stepped = Math.round((raw - min) / step) * step + min;
        float v = (float) Math.max(min, Math.min(max, stepped));
        return Math.round(v * 1000f) / 1000f;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        float v = get.get();
        Draw.text(ctx, label, x, y + 1, t.text());
        String val = format.apply(v);
        Draw.text(ctx, val, x + w - Draw.textWidth(val), y + 1, t.accent());
        int ty = y + 14;
        float ratio = (v - min) / (max - min);
        int fx = Math.round((w - 4) * Math.max(0, Math.min(1, ratio)));
        Draw.rect(ctx, x, ty, w, 2, t.line());
        Draw.rect(ctx, x, ty, fx + 2, 2, fillColor == 0 ? t.accent() : fillColor);
        Draw.rect(ctx, x + fx, ty - 3, 4, 8, dragging || contains(mx, my) ? 0xFFFFFFFF : t.text());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        dragging = true;
        set.accept(valueFromMouse(mx, x, w, min, max, step));
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button) {
        if (!dragging) return false;
        set.accept(valueFromMouse(mx, x, w, min, max, step));
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        boolean was = dragging;
        dragging = false;
        return was;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (!contains(mx, my) || amount == 0) return false;
        float v = get.get() + (amount > 0 ? step : -step);
        set.accept(Math.round(Math.max(min, Math.min(max, v)) * 1000f) / 1000f);
        return true;
    }
}
