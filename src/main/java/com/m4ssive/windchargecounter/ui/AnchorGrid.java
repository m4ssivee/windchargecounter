package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

/** 3×3 grid of screen anchors. selected() returns row*3+col of the current anchor, or -1. */
public class AnchorGrid extends Widget {
    private static final int CW = 14, CH = 9, GAP = 2;
    private final IntSupplier selected;
    private final BiConsumer<Integer, Integer> onPick;

    public AnchorGrid(IntSupplier selected, BiConsumer<Integer, Integer> onPick) {
        super(CW * 3 + GAP * 2, CH * 3 + GAP * 2);
        this.selected = selected;
        this.onPick = onPick;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        int sel = selected.getAsInt();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int cx = x + c * (CW + GAP), cy = y + r * (CH + GAP);
                boolean hover = mx >= cx && mx < cx + CW && my >= cy && my < cy + CH;
                boolean on = sel == r * 3 + c;
                Draw.rect(ctx, cx, cy, CW, CH, on ? UiTheme.alpha(t.accent(), 90) : t.surfaceAlt());
                Draw.border(ctx, cx, cy, CW, CH, 1, on || hover ? t.accent() : t.line());
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        int c = Math.min(2, (int) ((mx - x) / (CW + GAP)));
        int r = Math.min(2, (int) ((my - y) / (CH + GAP)));
        onPick.accept(c, r);
        return true;
    }
}
