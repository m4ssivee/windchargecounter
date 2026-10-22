package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/** A clickable color square (RGB supplier). Selected = white 1 px ring. */
public class Swatch extends Widget {
    private final IntSupplier rgb;
    private final BooleanSupplier selected;
    private final Runnable onPress;

    public Swatch(int size, IntSupplier rgb, BooleanSupplier selected, Runnable onPress) {
        super(size, size);
        this.rgb = rgb;
        this.selected = selected;
        this.onPress = onPress;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        boolean sel = selected.getAsBoolean();
        Draw.rect(ctx, x, y, w, h, 0xFF000000 | rgb.getAsInt());
        Draw.border(ctx, x, y, w, h, 1, sel ? 0xFFFFFFFF : contains(mx, my) ? t.text() : t.line());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        onPress.run();
        return true;
    }
}
