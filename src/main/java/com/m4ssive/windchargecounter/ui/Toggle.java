package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Label on the left, square switch on the right. The whole row is clickable. */
public class Toggle extends Widget {
    private static final int SW = 18, SH = 10;
    private final String label;
    private final BooleanSupplier get;
    private final Consumer<Boolean> set;

    public Toggle(int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
        super(w, 14);
        this.label = label;
        this.get = get;
        this.set = set;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        boolean on = get.getAsBoolean();
        Draw.text(ctx, label, x, y + 3, contains(mx, my) ? t.text() : UiTheme.alpha(t.text(), 225));
        int sx = x + w - SW, sy = y + 2;
        if (on) {
            Draw.rect(ctx, sx, sy, SW, SH, t.accent());
            Draw.rect(ctx, sx + SW - 8, sy + 2, 6, 6, t.onAccent());
        } else {
            Draw.rect(ctx, sx, sy, SW, SH, t.surfaceAlt());
            Draw.border(ctx, sx, sy, SW, SH, 1, t.line());
            Draw.rect(ctx, sx + 2, sy + 2, 6, 6, t.muted());
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        set.accept(!get.getAsBoolean());
        return true;
    }
}
