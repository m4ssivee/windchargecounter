package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

/** Small section heading, e.g. "SNAP TO". */
public class Label extends Widget {
    private final String text;

    public Label(String text) {
        super(0, 10);
        this.text = text;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        Draw.text(ctx, text, x, y + 1, t.muted());
    }
}
