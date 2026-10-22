package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.gui.DrawContext;

/**
 * Base of the custom widget kit. Coordinates are screen GUI pixels.
 * Input arrives through this version-independent model (EditorScreen adapts the real events).
 */
public abstract class Widget {
    public int x, y, w, h;
    public boolean focused;

    protected Widget(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public Widget at(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public abstract void render(DrawContext ctx, int mouseX, int mouseY, UiTheme t);

    public boolean contains(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public boolean mouseClicked(double mx, double my, int button) { return false; }
    public boolean mouseDragged(double mx, double my, int button) { return false; }
    public boolean mouseReleased(double mx, double my, int button) { return false; }
    public boolean mouseScrolled(double mx, double my, double amount) { return false; }
    public boolean keyPressed(int key, int modifiers) { return false; }
    public boolean charTyped(String chars) { return false; }
}
