package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeTracker;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.theme.ColorMath;
import com.m4ssive.windchargecounter.ui.Draw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The "Panel" HUD: header, one row per player with a colored stripe, count and progress bar.
 * All colors come from the theme in {@link ModConfig}. Input is handled by the editor screen.
 */
public class WindChargeHud {
    private static final ItemStack WIND_CHARGE_STACK = new ItemStack(Items.WIND_CHARGE);

    // unscaled GUI pixels
    private static final int PAD = 4;
    private static final int HEADER_H = 13;
    private static final int ROW_H_BAR = 14;
    private static final int ROW_H_NOBAR = 11;
    private static final int MIN_WIDTH = 96;
    private static final int MAX_NAME_W = 110;
    private static final int SCREEN_MARGIN = 4;
    private static final long FLASH_MS = 650;
    private static final String TITLE = "WIND CHARGES";

    private final WindChargeTracker tracker;
    private final ModConfig config;

    // last drawn panel in screen pixels (scale applied)
    private int lastX, lastY, lastW, lastH;

    public WindChargeHud(WindChargeTracker tracker, ModConfig config) {
        this.tracker = tracker;
        this.config = config;
    }

    // ------------------------------------------------------------------ public API

    /** Draws the HUD. {@code editing} adds the selection outline and preview rows when there is no data. */
    public void render(DrawContext ctx, boolean editing) {
        MinecraftClient client = MinecraftClient.getInstance();
        // the editor can be opened from Mod Menu on the title screen: preview rows need no world
        if (!editing && (client.player == null || client.world == null)) return;
        if (!config.enableHud && !editing) return;

        List<Row> rows = rows(client, editing);
        if (rows.isEmpty()) return;

        TextRenderer tr = client.textRenderer;
        int[] size = unscaledSize(tr, rows);
        int width = size[0], height = size[1], rowH = size[2];
        float scale = config.scale;
        int sw = Math.round(width * scale), sh = Math.round(height * scale);

        int screenW = client.getWindow().getScaledWidth(), screenH = client.getWindow().getScaledHeight();
        if (config.hudX == ModConfig.UNSET || config.hudY == ModConfig.UNSET) {
            config.hudX = screenW - sw - SCREEN_MARGIN;
            config.hudY = SCREEN_MARGIN;
        }
        int x = clamp(config.hudX, 0, Math.max(0, screenW - sw));
        int y = clamp(config.hudY, 0, Math.max(0, screenH - sh));
        lastX = x; lastY = y; lastW = sw; lastH = sh;

        Draw.push(ctx);
        Draw.translate(ctx, x, y);
        Draw.scale(ctx, scale);
        drawPanel(ctx, tr, rows, width, height, rowH);
        Draw.pop(ctx);

        if (editing) drawSelection(ctx);
    }

    /** {x, y, w, h} of the last drawn panel in screen pixels. */
    public int[] bounds() {
        return new int[]{lastX, lastY, lastW, lastH};
    }

    /** {w, h} the panel would have right now, in screen pixels (scale applied). */
    public int[] measure(MinecraftClient client, boolean editing) {
        List<Row> rows = rows(client, editing);
        if (rows.isEmpty()) return new int[]{0, 0};
        int[] s = unscaledSize(client.textRenderer, rows);
        return new int[]{Math.round(s[0] * config.scale), Math.round(s[1] * config.scale)};
    }

    /** Full → Half → Low color for a count, or the accent when "color by charges" is off. RGB. */
    public static int countColor(ModConfig c, int count) {
        return c.colorByCharges
            ? ColorMath.countColor(count, WindChargeTracker.MAX_WIND_CHARGES, c.fullColor, c.halfColor, c.lowColor)
            : c.accentColor;
    }

    // ------------------------------------------------------------------ drawing

    /** {width, height, rowHeight} in unscaled pixels. */
    private int[] unscaledSize(TextRenderer tr, List<Row> rows) {
        int nameW = 0, countW = 0;
        for (Row r : rows) {
            nameW = Math.max(nameW, Math.min(MAX_NAME_W, tr.getWidth(r.name)));
            countW = Math.max(countW, tr.getWidth(String.valueOf(r.count)));
        }
        int rowH = config.showBars ? ROW_H_BAR : ROW_H_NOBAR;
        int width = Math.max(MIN_WIDTH, PAD + 3 + nameW + 12 + countW + PAD);
        if (config.showHeader) width = Math.max(width, PAD + 12 + tr.getWidth(TITLE) + PAD);
        int height = (config.showHeader ? HEADER_H + 2 : 0) + PAD / 2 + rows.size() * rowH + PAD / 2;
        return new int[]{width, height, rowH};
    }

    private void drawPanel(DrawContext ctx, TextRenderer tr, List<Row> rows, int w, int h, int rowH) {
        long now = System.currentTimeMillis();
        int text = config.textColor;
        int accent = config.accentColor;

        int alpha = clamp(config.panelOpacity * 255 / 100, 0, 255);
        if (alpha > 0) {
            int bg = ColorMath.argb(config.panelColor, alpha);
            ctx.fill(1, 0, w - 1, h, bg);
            ctx.fill(0, 1, 1, h - 1, bg);
            ctx.fill(w - 1, 1, w, h - 1, bg);
        }

        int cy = 0;
        if (config.showHeader) {
            Draw.push(ctx);
            Draw.translate(ctx, PAD - 1, 2);
            Draw.scale(ctx, 0.625f); // 16 px item → 10 px
            ctx.drawItem(WIND_CHARGE_STACK, 0, 0);
            Draw.pop(ctx);
            ctx.drawTextWithShadow(tr, TITLE, PAD + 11, 3, ColorMath.argb(ColorMath.lerp(accent, text, 0.85f), 255));
            cy = HEADER_H;
            ctx.fill(PAD, cy, w - PAD, cy + 1, ColorMath.argb(text, 0x30));
            cy += 2;
        }
        cy += PAD / 2;

        int barBg = ColorMath.argb(ColorMath.lerp(config.panelColor, text, 0.2f), 255);
        for (Row r : rows) {
            int color = ColorMath.argb(countColor(config, r.count), 255);

            long since = now - r.lastUse;
            if (config.flashOnThrow && r.lastUse > 0 && since >= 0 && since < FLASH_MS) {
                int a = (int) (0x70 * (1f - since / (float) FLASH_MS));
                ctx.fill(1, cy, w - 1, cy + rowH, (a << 24) | 0xFFFFFF);
            }

            ctx.fill(PAD - 1, cy + 1, PAD + 1, cy + rowH - 1, color);

            int nameColor = r.self ? ColorMath.argb(accent, 255) : ColorMath.argb(text, r.inRange ? 255 : 140);
            ctx.drawTextWithShadow(tr, ellipsize(tr, r.name, MAX_NAME_W), PAD + 4, cy + 2, nameColor);

            String count = String.valueOf(r.count);
            ctx.drawTextWithShadow(tr, count, w - PAD - tr.getWidth(count), cy + 2, color);

            if (config.showBars) {
                int bx1 = PAD + 4, bx2 = w - PAD, by = cy + 11;
                ctx.fill(bx1, by, bx2, by + 2, barBg);
                float ratio = Math.min(1f, Math.max(0f, r.count / (float) WindChargeTracker.MAX_WIND_CHARGES));
                int fill = Math.round((bx2 - bx1) * ratio);
                if (fill > 0) ctx.fill(bx1, by, bx1 + fill, by + 2, color);
            }
            cy += rowH;
        }
    }

    private void drawSelection(DrawContext ctx) {
        int c = ColorMath.argb(config.accentColor, 255);
        Draw.border(ctx, lastX - 3, lastY - 3, lastW + 6, lastH + 6, 1, c);
        int[][] corners = {{lastX - 5, lastY - 5}, {lastX + lastW + 1, lastY - 5},
            {lastX - 5, lastY + lastH + 1}, {lastX + lastW + 1, lastY + lastH + 1}};
        for (int[] p : corners) {
            Draw.rect(ctx, p[0], p[1], 4, 4, 0xFFEAF2FF);
            Draw.border(ctx, p[0], p[1], 4, 4, 1, 0xFF0A1020);
        }
    }

    // ------------------------------------------------------------------ data

    private List<Row> rows(MinecraftClient client, boolean editing) {
        List<Row> rows = new ArrayList<>();
        List<WindChargeTracker.Opponent> opponents = tracker.getOpponents();
        if (config.showSelf && (tracker.hasWindCharge() || !opponents.isEmpty())) {
            rows.add(new Row("You", tracker.getLocalPlayerCharges(), true, true, tracker.getLastLocalUse()));
        }
        for (WindChargeTracker.Opponent o : opponents) {
            boolean inRange = client.world != null && client.world.getPlayerByUuid(o.uuid()) != null;
            rows.add(new Row(o.name(), o.remaining(), false, inRange, o.lastUse()));
        }
        if (editing && rows.isEmpty()) {
            long now = System.currentTimeMillis();
            rows.add(new Row("You", 112, true, true, 0));
            rows.add(new Row("Enemy", 74, false, true, now - (now % 2000))); // shows the flash
            rows.add(new Row("FarAway", 23, false, false, 0));
        }
        return rows;
    }

    private static String ellipsize(TextRenderer tr, String s, int maxW) {
        if (tr.getWidth(s) <= maxW) return s;
        String dots = "…";
        int dw = tr.getWidth(dots);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (tr.getWidth(b.toString() + s.charAt(i)) + dw > maxW) break;
            b.append(s.charAt(i));
        }
        return b + dots;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private record Row(String name, int count, boolean self, boolean inRange, long lastUse) {}
}
