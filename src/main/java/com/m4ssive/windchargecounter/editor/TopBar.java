package com.m4ssive.windchargecounter.editor;

import com.m4ssive.windchargecounter.ui.Draw;
import com.m4ssive.windchargecounter.ui.UiTheme;
import com.m4ssive.windchargecounter.ui.Widget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Centered strip at the top: [icon WCC] | Layout Counter Nametag Theme | Cancel Done. */
public class TopBar extends Widget {
    private static final ItemStack ICON = new ItemStack(Items.WIND_CHARGE);
    private static final int H = 20, PAD = 3, GAP = 2, BTN_PAD = 6;
    private static final String[] TAB_LABELS = {"Layout", "Counter", "Nametag", "Theme"};

    private final Supplier<EditorScreen.Tab> active;
    private final Consumer<EditorScreen.Tab> onTab;
    private final Runnable onCancel, onDone;

    private final int[] tabX = new int[4], tabW = new int[4];
    private int cancelX, cancelW, doneX, doneW, brandW;

    public TopBar(Supplier<EditorScreen.Tab> active, Consumer<EditorScreen.Tab> onTab, Runnable onCancel, Runnable onDone) {
        super(0, H);
        this.active = active;
        this.onTab = onTab;
        this.onCancel = onCancel;
        this.onDone = onDone;
    }

    /** Computes widths and centers the bar horizontally at {@code top}. */
    public void layout(int screenW, int top) {
        brandW = 10 + 3 + Draw.textWidth("WCC") + 6;
        int width = PAD + brandW + 4;
        for (int i = 0; i < 4; i++) {
            tabW[i] = Draw.textWidth(TAB_LABELS[i]) + BTN_PAD * 2;
            width += tabW[i] + GAP;
        }
        cancelW = Draw.textWidth("Cancel") + BTN_PAD * 2;
        doneW = Draw.textWidth("Done") + BTN_PAD * 2;
        width += 6 + cancelW + GAP + doneW + PAD;
        w = width;
        x = (screenW - w) / 2;
        y = top;
        int cx = x + PAD + brandW + 4;
        for (int i = 0; i < 4; i++) {
            tabX[i] = cx;
            cx += tabW[i] + GAP;
        }
        cancelX = cx + 6;
        doneX = cancelX + cancelW + GAP;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        Draw.rect(ctx, x, y, w, h, t.surface());
        Draw.border(ctx, x, y, w, h, 1, UiTheme.alpha(t.accent(), 90));

        Draw.push(ctx);
        Draw.translate(ctx, x + PAD + 1, y + 5);
        Draw.scale(ctx, 0.625f);
        ctx.drawItem(ICON, 0, 0);
        Draw.pop(ctx);
        Draw.text(ctx, "WCC", x + PAD + 14, y + 6, t.text());
        Draw.rect(ctx, x + PAD + brandW, y + 4, 1, h - 8, t.line());

        EditorScreen.Tab cur = active.get();
        for (int i = 0; i < 4; i++) {
            boolean sel = cur.ordinal() == i;
            boolean hover = hit(mx, my, tabX[i], tabW[i]);
            if (sel) Draw.rect(ctx, tabX[i], y + PAD, tabW[i], h - PAD * 2, t.accent());
            else if (hover) Draw.rect(ctx, tabX[i], y + PAD, tabW[i], h - PAD * 2, t.surfaceAlt());
            if (sel) Draw.textFlat(ctx, TAB_LABELS[i], tabX[i] + BTN_PAD, y + 6, t.onAccent());
            else Draw.text(ctx, TAB_LABELS[i], tabX[i] + BTN_PAD, y + 6, hover ? t.text() : 0xFFC7D3E6);
        }
        Draw.rect(ctx, cancelX - 4, y + 4, 1, h - 8, t.line());
        boolean hc = hit(mx, my, cancelX, cancelW);
        Draw.text(ctx, "Cancel", cancelX + BTN_PAD, y + 6, hc ? t.text() : t.muted());
        boolean hd = hit(mx, my, doneX, doneW);
        Draw.rect(ctx, doneX, y + PAD, doneW, h - PAD * 2, hd ? 0xFF7FF08A : 0xFF5FE36A);
        Draw.textFlat(ctx, "Done", doneX + BTN_PAD, y + 6, 0xFF0A1020);
    }

    private boolean hit(double mx, double my, int bx, int bw) {
        return mx >= bx && mx < bx + bw && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        for (int i = 0; i < 4; i++) {
            if (hit(mx, my, tabX[i], tabW[i])) {
                onTab.accept(EditorScreen.Tab.values()[i]);
                return true;
            }
        }
        if (hit(mx, my, cancelX, cancelW)) onCancel.run();
        else if (hit(mx, my, doneX, doneW)) onDone.run();
        return true; // clicks on the bar never reach the HUD
    }
}
