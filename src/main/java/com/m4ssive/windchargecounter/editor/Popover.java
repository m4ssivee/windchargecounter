package com.m4ssive.windchargecounter.editor;

import com.m4ssive.windchargecounter.editor.layout.Placement;
import com.m4ssive.windchargecounter.ui.Draw;
import com.m4ssive.windchargecounter.ui.UiTheme;
import com.m4ssive.windchargecounter.ui.Widget;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** Floating panel with a title, a vertical list of widgets and a caret pointing at its opener. */
public class Popover extends Widget {
    private static final int PAD = 7, GAP = 5, TITLE_H = 12, CARET = 4;

    private final String title;
    private final List<Widget> rows;
    private final Widget headerAction;
    private Placement placement;

    /** headerAction may be null; it is placed at the right of the title (e.g. "Reset"). */
    public Popover(String title, List<Widget> rows, int width, Widget headerAction) {
        super(width, 0);
        this.title = title;
        this.rows = rows;
        this.headerAction = headerAction;
        int inner = width - PAD * 2;
        int hh = PAD + TITLE_H;
        for (Widget r : rows) {
            if (r.w <= 0 || r.w > inner) r.w = inner;
            hh += r.h + GAP;
        }
        this.h = hh - GAP + PAD;
    }

    /** Moves the popover and its rows to the placement. */
    public void place(Placement p) {
        placement = p;
        x = p.x();
        y = p.y();
        int cy = y + PAD + TITLE_H;
        for (Widget r : rows) {
            r.at(x + PAD, cy);
            cy += r.h + GAP;
        }
        if (headerAction != null) headerAction.at(x + w - PAD - headerAction.w, y + PAD - 2);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        if (placement == null) return;
        int border = UiTheme.alpha(t.accent(), 130);
        Draw.rect(ctx, x, y, w, h, t.surface());
        Draw.border(ctx, x, y, w, h, 1, border);
        if (placement.above()) {
            Draw.caretDown(ctx, placement.caretX(), y + h + CARET - 1, CARET, border);
        } else {
            Draw.caretUp(ctx, placement.caretX(), y - CARET, CARET, border);
        }
        Draw.text(ctx, title, x + PAD, y + PAD, t.text());
        if (headerAction != null) headerAction.render(ctx, mx, my, t);
        for (Widget r : rows) r.render(ctx, mx, my, t);
    }

    private Iterable<Widget> all() {
        if (headerAction == null) return rows;
        java.util.ArrayList<Widget> l = new java.util.ArrayList<>(rows);
        l.add(headerAction);
        return l;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean consumed = false;
        for (Widget r : all()) consumed |= r.mouseClicked(mx, my, button); // every child sees it (hex fields commit on outside click)
        return consumed || contains(mx, my);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button) {
        for (Widget r : rows) if (r.mouseDragged(mx, my, button)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        boolean any = false;
        for (Widget r : rows) any |= r.mouseReleased(mx, my, button);
        return any;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        for (Widget r : rows) if (r.mouseScrolled(mx, my, amount)) return true;
        return contains(mx, my);
    }

    @Override
    public boolean keyPressed(int key, int modifiers) {
        for (Widget r : rows) if (r.keyPressed(key, modifiers)) return true;
        return false;
    }

    @Override
    public boolean charTyped(String chars) {
        for (Widget r : rows) if (r.charTyped(chars)) return true;
        return false;
    }
}
