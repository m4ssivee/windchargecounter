package com.m4ssive.windchargecounter.editor;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.theme.ColorMath;
import com.m4ssive.windchargecounter.theme.Themes;
import com.m4ssive.windchargecounter.ui.Draw;
import com.m4ssive.windchargecounter.ui.UiTheme;
import com.m4ssive.windchargecounter.ui.Widget;
import net.minecraft.client.gui.DrawContext;

/** Attached under the HUD: [-] 1.0x [+] | [theme chip] | [...]. */
public class Toolbar extends Widget {
    private static final int H = 20, PAD = 2, BTN = 14, GAP_TO_HUD = 6, EDGE = 4;

    private final EditorScreen screen;
    private final ModConfig config;

    private int minusX, scaleX, plusX, chipX, chipW, moreX;

    public Toolbar(EditorScreen screen, ModConfig config) {
        super(0, H);
        this.screen = screen;
        this.config = config;
    }

    private String chipLabel() {
        return config.customTheme ? "Custom" : Themes.byId(config.themeId).name();
    }

    /** Places the bar 6 px under the HUD (above it when there is no room), clamped to the screen. */
    public void layout(int[] hud, int screenW, int screenH) {
        int scaleW = Draw.textWidth("3.0x") + 4;
        chipW = 3 * 4 + 4 + Draw.textWidth(chipLabel()) + 10;
        w = PAD + BTN + scaleW + BTN + 7 + chipW + 3 + BTN + PAD;
        int nx = hud[0];
        int ny = hud[1] + hud[3] + GAP_TO_HUD;
        if (ny + H > screenH - EDGE) ny = hud[1] - GAP_TO_HUD - H;
        x = Math.max(EDGE, Math.min(nx, screenW - w - EDGE));
        y = Math.max(EDGE, ny);
        minusX = x + PAD;
        scaleX = minusX + BTN;
        plusX = scaleX + scaleW;
        chipX = plusX + BTN + 7;
        moreX = chipX + chipW + 3;
    }

    public int chipCenterX() {
        return chipX + chipW / 2;
    }

    public int moreCenterX() {
        return moreX + BTN / 2;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        Draw.rect(ctx, x, y, w, h, t.surface());
        Draw.border(ctx, x, y, w, h, 1, UiTheme.alpha(t.accent(), 100));
        int ty = y + 6;

        button(ctx, mx, my, minusX, "-", t);
        String s = String.format("%.1fx", config.scale);
        Draw.textCentered(ctx, s, scaleX + (plusX - scaleX) / 2, ty, t.accent());
        button(ctx, mx, my, plusX, "+", t);
        Draw.rect(ctx, plusX + BTN + 3, y + 4, 1, h - 8, t.line());

        boolean chipHover = hit(mx, my, chipX, chipW);
        boolean themeOpen = screen.isPopoverOpen(EditorScreen.Tab.THEME);
        Draw.rect(ctx, chipX, y + 3, chipW, h - 6, themeOpen || chipHover ? t.surfaceAlt() : 0x00000000);
        Draw.border(ctx, chipX, y + 3, chipW, h - 6, 1, themeOpen ? t.accent() : t.line());
        int sx = chipX + 4;
        int[] stripes = {config.accentColor, config.fullColor, config.lowColor};
        for (int c : stripes) {
            Draw.rect(ctx, sx, y + 6, 4, 8, ColorMath.argb(c, 255));
            sx += 4;
        }
        Draw.text(ctx, chipLabel(), sx + 4, ty, t.text());

        button(ctx, mx, my, moreX, "...", t);
    }

    private void button(DrawContext ctx, int mx, int my, int bx, String label, UiTheme t) {
        boolean hover = hit(mx, my, bx, BTN);
        if (hover) Draw.rect(ctx, bx, y + 3, BTN, h - 6, t.surfaceAlt());
        Draw.textCentered(ctx, label, bx + BTN / 2, y + 6, hover ? t.text() : UiTheme.alpha(t.text(), 220));
    }

    private boolean hit(double mx, double my, int bx, int bw) {
        return mx >= bx && mx < bx + bw && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !contains(mx, my)) return false;
        if (hit(mx, my, minusX, BTN)) screen.changeScale(-0.1f);
        else if (hit(mx, my, plusX, BTN)) screen.changeScale(0.1f);
        else if (hit(mx, my, chipX, chipW)) screen.togglePopover(EditorScreen.Tab.THEME);
        else if (hit(mx, my, moreX, BTN)) screen.togglePopover(screen.activeTab());
        return true;
    }
}
