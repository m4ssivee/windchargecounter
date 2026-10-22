package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

/** Flat button: primary = accent fill, otherwise outlined. */
public class Button extends Widget {
    private final Supplier<String> label;
    private final boolean primary;
    private final Runnable onPress;

    public Button(int w, int h, Supplier<String> label, boolean primary, Runnable onPress) {
        super(w, h);
        this.label = label;
        this.primary = primary;
        this.onPress = onPress;
    }

    public static Button fit(String label, boolean primary, Runnable onPress) {
        return new Button(Draw.textWidth(label) + 12, 16, () -> label, primary, onPress);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        boolean hover = contains(mx, my);
        if (primary) {
            Draw.rect(ctx, x, y, w, h, hover ? UiTheme.alpha(t.accent(), 220) : t.accent());
        } else {
            Draw.rect(ctx, x, y, w, h, hover ? t.surfaceAlt() : 0x00000000);
            Draw.border(ctx, x, y, w, h, 1, hover ? t.accent() : t.line());
        }
        Draw.textCentered(ctx, label.get(), x + w / 2, y + (h - 8) / 2, primary ? t.onAccent() : t.text());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        onPress.run();
        return true;
    }
}
