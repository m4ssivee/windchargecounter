package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeTracker;
import com.m4ssive.windchargecounter.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Wind charge HUD paneli.
 *
 * <pre>
 *  ┌──────────────────────────┐
 *  │ ◉ WIND CHARGES           │  başlık (opsiyonel)
 *  ├──────────────────────────┤
 *  ▌You                   112 │
 *  ▌▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀░░░░  │  dolum çubuğu (opsiyonel)
 *  ▌Enemy                  74 │  rakip atınca satır kısa süre parlar
 *  └──────────────────────────┘
 * </pre>
 *
 * Bütün çizim, 1.21–1.21.11 arasında değişmeyen fill / drawTextWithShadow / drawItem ile yapılır.
 * Sürüme özel tek kısım matris yığını, o da {@link #push}/{@link #pop}/{@link #translate}/{@link #scale}'de.
 */
public class WindChargeHud {
    private static final ItemStack WIND_CHARGE_STACK = new ItemStack(Items.WIND_CHARGE);

    // ölçeksiz (scale=1) piksel değerleri
    private static final int PAD = 4;
    private static final int HEADER_H = 13;
    private static final int ROW_H_BAR = 14;
    private static final int ROW_H_NOBAR = 11;
    private static final int MIN_WIDTH = 96;
    private static final int MAX_NAME_W = 110;
    private static final int SCREEN_MARGIN = 4;
    private static final long FLASH_MS = 650;

    // renkler (1.21.6+ alfa ister, hepsi 0xFF.. ile)
    private static final int COL_TITLE = 0xFFB8C4D6;
    private static final int COL_NAME = 0xFFF2F4F8;
    private static final int COL_NAME_SELF = 0xFF7FD8FF;
    private static final int COL_NAME_AWAY = 0xFF8A93A3;
    private static final int COL_SEPARATOR = 0x30FFFFFF;
    private static final int COL_BAR_BG = 0xFF262C38;
    private static final int COL_EDIT_BORDER = 0xFF55DDFF;
    private static final int COL_EDIT_BORDER_DRAG = 0xFFFFE066;
    private static final int PANEL_RGB = 0x0B0F17;

    private final WindChargeTracker tracker;
    private final ModConfig config;

    private boolean editMode = false;
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;

    // son çizilen panelin ekran koordinatlarındaki kutusu (ölçek dahil)
    private int lastX, lastY, lastW, lastH;

    public WindChargeHud(WindChargeTracker tracker, ModConfig config) {
        this.tracker = tracker;
        this.config = config;
    }

    // ------------------------------------------------------------------ render

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!config.enableHud && !editMode) return;

        List<Row> rows = editMode ? previewRowsIfEmpty(collectRows(client)) : collectRows(client);
        if (rows.isEmpty()) return;

        TextRenderer tr = client.textRenderer;
        float scale = config.scale;

        // panel boyutu (ölçeksiz)
        int nameW = 0, countW = 0;
        for (Row r : rows) {
            nameW = Math.max(nameW, Math.min(MAX_NAME_W, tr.getWidth(r.name)));
            countW = Math.max(countW, tr.getWidth(String.valueOf(r.count)));
        }
        int rowH = config.showBars ? ROW_H_BAR : ROW_H_NOBAR;
        int width = Math.max(MIN_WIDTH, PAD + 3 + nameW + 12 + countW + PAD);
        if (config.showHeader) width = Math.max(width, PAD + 12 + tr.getWidth("WIND CHARGES") + PAD);
        int height = (config.showHeader ? HEADER_H + 2 : 0) + PAD / 2 + rows.size() * rowH + PAD / 2;

        // ekran konumu: ilk açılışta sağ üst, her karede ekrana sığdırılır
        int sw = Math.round(width * scale), sh = Math.round(height * scale);
        int screenW = client.getWindow().getScaledWidth(), screenH = client.getWindow().getScaledHeight();
        if (config.hudX == ModConfig.UNSET || config.hudY == ModConfig.UNSET) {
            config.hudX = screenW - sw - SCREEN_MARGIN;
            config.hudY = SCREEN_MARGIN;
        }
        int x = clamp(config.hudX, 0, Math.max(0, screenW - sw));
        int y = clamp(config.hudY, 0, Math.max(0, screenH - sh));
        lastX = x; lastY = y; lastW = sw; lastH = sh;

        push(context);
        translate(context, x, y);
        scale(context, scale);
        drawPanel(context, tr, rows, width, height, rowH);
        pop(context);

        if (editMode) drawEditBorder(context);
    }

    private void drawPanel(DrawContext ctx, TextRenderer tr, List<Row> rows, int w, int h, int rowH) {
        long now = System.currentTimeMillis();

        // yuvarlatılmış köşeli arka plan
        int bg = (clamp(config.panelOpacity * 255 / 100, 0, 255) << 24) | PANEL_RGB;
        if ((bg >>> 24) != 0) {
            ctx.fill(1, 0, w - 1, h, bg);
            ctx.fill(0, 1, 1, h - 1, bg);
            ctx.fill(w - 1, 1, w, h - 1, bg);
        }

        int cy = 0;
        if (config.showHeader) {
            push(ctx);
            translate(ctx, PAD - 1, 2);
            scale(ctx, 0.625f); // 16px ikon -> 10px
            ctx.drawItem(WIND_CHARGE_STACK, 0, 0);
            pop(ctx);
            ctx.drawTextWithShadow(tr, "WIND CHARGES", PAD + 11, 3, COL_TITLE);
            cy = HEADER_H;
            ctx.fill(PAD, cy, w - PAD, cy + 1, COL_SEPARATOR);
            cy += 2;
        }
        cy += PAD / 2;

        for (Row r : rows) {
            int color = getChargeColor(r.count);

            // rakip charge atınca satır parlar
            long since = now - r.lastUse;
            if (config.flashOnThrow && r.lastUse > 0 && since >= 0 && since < FLASH_MS) {
                int a = (int) (0x70 * (1f - since / (float) FLASH_MS));
                ctx.fill(1, cy, w - 1, cy + rowH, (a << 24) | 0xFFFFFF);
            }

            // sol renk şeridi
            ctx.fill(PAD - 1, cy + 1, PAD + 1, cy + rowH - 1, color);

            int nameColor = r.self ? COL_NAME_SELF : (r.inRange ? COL_NAME : COL_NAME_AWAY);
            ctx.drawTextWithShadow(tr, ellipsize(tr, r.name, MAX_NAME_W), PAD + 4, cy + 2, nameColor);

            String count = String.valueOf(r.count);
            ctx.drawTextWithShadow(tr, count, w - PAD - tr.getWidth(count), cy + 2, color);

            if (config.showBars) {
                int bx1 = PAD + 4, bx2 = w - PAD, by = cy + 11;
                ctx.fill(bx1, by, bx2, by + 2, COL_BAR_BG);
                float ratio = Math.min(1f, Math.max(0f, r.count / (float) WindChargeTracker.MAX_WIND_CHARGES));
                int fill = Math.round((bx2 - bx1) * ratio);
                if (fill > 0) ctx.fill(bx1, by, bx1 + fill, by + 2, color);
            }
            cy += rowH;
        }
    }

    private void drawEditBorder(DrawContext ctx) {
        int c = isDragging ? COL_EDIT_BORDER_DRAG : COL_EDIT_BORDER;
        int x1 = lastX - 2, y1 = lastY - 2, x2 = lastX + lastW + 2, y2 = lastY + lastH + 2;
        ctx.fill(x1, y1, x2, y1 + 1, c);
        ctx.fill(x1, y2 - 1, x2, y2, c);
        ctx.fill(x1, y1, x1 + 1, y2, c);
        ctx.fill(x2 - 1, y1, x2, y2, c);
    }

    // ------------------------------------------------------------------ data

    private List<Row> collectRows(MinecraftClient client) {
        List<Row> rows = new ArrayList<>();
        List<WindChargeTracker.Opponent> opponents = tracker.getOpponents();

        if (config.showSelf && (tracker.hasWindCharge() || !opponents.isEmpty())) {
            rows.add(new Row("You", tracker.getLocalPlayerCharges(), true, true, tracker.getLastLocalUse()));
        }
        for (WindChargeTracker.Opponent o : opponents) {
            boolean inRange = client.world.getPlayerByUuid(o.uuid()) != null;
            rows.add(new Row(o.name(), o.remaining(), false, inRange, o.lastUse()));
        }
        return rows;
    }

    private List<Row> previewRowsIfEmpty(List<Row> real) {
        if (!real.isEmpty()) return real;
        long now = System.currentTimeMillis();
        List<Row> rows = new ArrayList<>();
        rows.add(new Row("You", 112, true, true, 0));
        // önizlemede parlama efekti de görünsün
        rows.add(new Row("Enemy", 74, false, true, now - (now % 2000)));
        rows.add(new Row("FarAwayPlayer", 23, false, false, 0));
        return rows;
    }

    // ------------------------------------------------------------------ input (edit mode)

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!editMode || button != 0) return false;
        isDragging = true;
        // ekranın neresinden tutulursa tutulsun HUD taşınır
        dragOffsetX = (int) mouseX - lastX;
        dragOffsetY = (int) mouseY - lastY;
        return true;
    }

    public boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        if (!editMode || button != 0) return false;
        boolean was = isDragging;
        isDragging = false;
        return was;
    }

    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!editMode || !isDragging) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        config.hudX = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, client.getWindow().getScaledWidth() - lastW));
        config.hudY = clamp((int) mouseY - dragOffsetY, 0, Math.max(0, client.getWindow().getScaledHeight() - lastH));
        return true;
    }

    public boolean handleScroll(double amount) {
        if (!editMode || amount == 0) return false;
        float s = config.scale + (amount > 0 ? 0.1f : -0.1f);
        config.scale = Math.round(Math.max(0.5f, Math.min(3.0f, s)) * 10f) / 10f;
        return true;
    }

    // ------------------------------------------------------------------ helpers

    /** Yeşil (dolu) → sarı (yarı) → kırmızı (boş). ARGB. */
    public static int getChargeColor(int charges) {
        float ratio = Math.max(0f, Math.min(1f, (float) charges / WindChargeTracker.MAX_WIND_CHARGES));
        int r, g;
        if (ratio > 0.5f) {
            float t = (ratio - 0.5f) * 2f;
            r = (int) (255 * (1f - t));
            g = (int) (200 + 55 * t);
        } else {
            r = 255;
            g = (int) (200 * ratio * 2f);
        }
        return 0xFF000000 | (r << 16) | (g << 8) | 0x40;
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

    // matris yığını 1.21.6'da MatrixStack'ten Matrix3x2fStack'e geçti
    private static void push(DrawContext ctx) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().pushMatrix();
        *///?} else {
        ctx.getMatrices().push();
        //?}
    }

    private static void pop(DrawContext ctx) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().popMatrix();
        *///?} else {
        ctx.getMatrices().pop();
        //?}
    }

    private static void translate(DrawContext ctx, float x, float y) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().translate(x, y);
        *///?} else {
        ctx.getMatrices().translate(x, y, 0f);
        //?}
    }

    private static void scale(DrawContext ctx, float s) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().scale(s, s);
        *///?} else {
        ctx.getMatrices().scale(s, s, 1f);
        //?}
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        if (!editMode) isDragging = false;
    }

    public boolean isDragging() { return isDragging; }
    public boolean isEditMode() { return editMode; }
    public ModConfig getConfig() { return config; }

    private record Row(String name, int count, boolean self, boolean inRange, long lastUse) {}
}
