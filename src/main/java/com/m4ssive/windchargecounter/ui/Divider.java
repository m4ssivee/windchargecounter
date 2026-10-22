package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

/** 1 px horizontal line. */
public class Divider extends Widget {
    public Divider() {
        super(0, 3);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        Draw.rect(ctx, x, y + 1, w, 1, t.line());
    }
}
