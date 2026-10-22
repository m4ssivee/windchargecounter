package com.m4ssive.windchargecounter.editor;

import com.m4ssive.windchargecounter.theme.Theme;
import com.m4ssive.windchargecounter.theme.Themes;
import com.m4ssive.windchargecounter.ui.Draw;
import com.m4ssive.windchargecounter.ui.HexField;
import com.m4ssive.windchargecounter.ui.UiTheme;
import com.m4ssive.windchargecounter.ui.Widget;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Small palette (8×3, every preset color + neutrals) with a hex field. Opens next to the swatch that was clicked. */
public class ColorPicker extends Widget {
    private static final int COLS = 8, ROWS = 3, CELL = 10, GAP = 2, PAD = 6;
    public static final List<Integer> PALETTE = buildPalette();

    private final IntSupplier get;
    private final IntConsumer set;
    private final HexField hex;

    public ColorPicker(IntSupplier get, IntConsumer set) {
        super(PAD * 2 + COLS * CELL + (COLS - 1) * GAP, PAD * 2 + ROWS * CELL + (ROWS - 1) * GAP + 4 + 14);
        this.get = get;
        this.set = set;
        this.hex = new HexField(w - PAD * 2, get, set);
    }

    private static List<Integer> buildPalette() {
        Set<Integer> s = new LinkedHashSet<>();
        for (Theme t : Themes.ALL) {
            s.add(t.accent());
            s.add(t.full());
            s.add(t.half());
            s.add(t.low());
        }
        for (Theme t : Themes.ALL) s.add(t.text());
        int[] neutrals = {0xFFFFFF, 0x000000, 0x0B0F17, 0x4A5263}; // keeps dark panel colors within the 24 cells
        for (int n : neutrals) s.add(n);
        List<Integer> l = new ArrayList<>(s);
        return l.subList(0, Math.min(COLS * ROWS, l.size()));
    }

    /** Puts the picker under (or above) the given point, on screen. */
    public void placeNear(int px, int py, int screenW, int screenH) {
        int nx = Math.max(4, Math.min(px, screenW - w - 4));
        int ny = py + h > screenH - 4 ? py - h - 16 : py;
        at(nx, Math.max(4, ny));
        hex.at(x + PAD, y + PAD + ROWS * CELL + (ROWS - 1) * GAP + 4);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        Draw.rect(ctx, x, y, w, h, 0xFF0D0E22);
        Draw.border(ctx, x, y, w, h, 1, t.accent());
        int cur = get.getAsInt() & 0xFFFFFF;
        for (int i = 0; i < PALETTE.size(); i++) {
            int cx = x + PAD + (i % COLS) * (CELL + GAP), cy = y + PAD + (i / COLS) * (CELL + GAP);
            int c = PALETTE.get(i);
            Draw.rect(ctx, cx, cy, CELL, CELL, 0xFF000000 | c);
            boolean hover = mx >= cx && mx < cx + CELL && my >= cy && my < cy + CELL;
            if (c == cur) Draw.border(ctx, cx - 1, cy - 1, CELL + 2, CELL + 2, 1, 0xFFFFFFFF);
            else if (hover) Draw.border(ctx, cx, cy, CELL, CELL, 1, t.text());
        }
        hex.render(ctx, mx, my, t);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean inHex = hex.mouseClicked(mx, my, button);
        if (inHex) return true;
        for (int i = 0; i < PALETTE.size(); i++) {
            int cx = x + PAD + (i % COLS) * (CELL + GAP), cy = y + PAD + (i / COLS) * (CELL + GAP);
            if (mx >= cx && mx < cx + CELL && my >= cy && my < cy + CELL) {
                hex.cancel();
                set.accept(PALETTE.get(i));
                return true;
            }
        }
        return contains(mx, my);
    }

    @Override
    public boolean keyPressed(int key, int modifiers) {
        return hex.keyPressed(key, modifiers);
    }

    @Override
    public boolean charTyped(String chars) {
        return hex.charTyped(chars);
    }

    public boolean isTyping() {
        return hex.focused;
    }
}
