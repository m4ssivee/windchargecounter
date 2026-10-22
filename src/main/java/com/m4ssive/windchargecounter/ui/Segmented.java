package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Equal-width option buttons; the selected one is filled with the accent. */
public class Segmented extends Widget {
    private final List<String> options;
    private final IntSupplier selected;
    private final IntConsumer onSelect;

    public Segmented(int w, List<String> options, IntSupplier selected, IntConsumer onSelect) {
        super(w, 14);
        this.options = options;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    private int cellW() {
        return (w - (options.size() - 1) * 2) / options.size();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        int cw = cellW();
        for (int i = 0; i < options.size(); i++) {
            int cx = x + i * (cw + 2);
            boolean sel = selected.getAsInt() == i;
            boolean hover = mx >= cx && mx < cx + cw && my >= y && my < y + h;
            if (sel) Draw.rect(ctx, cx, y, cw, h, t.accent());
            else {
                Draw.rect(ctx, cx, y, cw, h, t.surfaceAlt());
                Draw.border(ctx, cx, y, cw, h, 1, hover ? t.accent() : t.line());
            }
            if (sel) Draw.textFlatCentered(ctx, options.get(i), cx + cw / 2, y + 3, t.onAccent());
            else Draw.textCentered(ctx, options.get(i), cx + cw / 2, y + 3, t.text());
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        int i = (int) ((mx - x) / (cellW() + 2));
        onSelect.accept(Math.max(0, Math.min(options.size() - 1, i)));
        return true;
    }
}
