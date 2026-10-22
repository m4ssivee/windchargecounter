package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeTracker;
import com.m4ssive.windchargecounter.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class WindChargeHud {
    private static final ItemStack WIND_CHARGE_STACK = new ItemStack(Items.WIND_CHARGE);
    private static final int DEFAULT_RIGHT_MARGIN = 160;

    private static final int PADDING = 2;
    private static final int LINE_HEIGHT = 16;
    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 2;

    private final WindChargeTracker tracker;
    private final ModConfig config;

    private boolean editMode = false;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // son çizilen HUD'un ekran koordinatlarındaki (ölçek uygulanmış) kutusu
    private int lastHudX, lastHudY, lastHudWidth, lastHudHeight;

    public WindChargeHud(WindChargeTracker tracker, ModConfig config) {
        this.tracker = tracker;
        this.config = config;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        if (!editMode) isDragging = false;
    }

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!config.enableHud && !editMode) return;
        // ne bizde charge var ne de takip edilen rakip -> PvP dışındayız, gizle
        if (!editMode && !tracker.hasWindCharge() && !tracker.hasTrackedOpponents()) return;

        List<PlayerInfo> players = new ArrayList<>();

        if (config.showSelf && (tracker.hasWindCharge() || editMode)) {
            players.add(new PlayerInfo("You", tracker.getLocalPlayerCharges(), true));
        }

        for (Map.Entry<UUID, Integer> entry : tracker.getAllOpponentUsage().entrySet()) {
            PlayerEntity player = client.world.getPlayerByUuid(entry.getKey());
            if (player == null) continue;
            int remaining = WindChargeTracker.MAX_WIND_CHARGES - entry.getValue();
            players.add(new PlayerInfo(player.getName().getString(), remaining, false));
        }

        if (editMode && players.isEmpty()) {
            players.add(new PlayerInfo("You", 120, true));
            players.add(new PlayerInfo("Enemy", 95, false));
        }
        if (players.isEmpty()) return;

        ensurePosition(client);
        renderHud(context, client.textRenderer, players);

        if (editMode) renderEditOverlay(context);
    }

    private void ensurePosition(MinecraftClient client) {
        if (config.hudX == ModConfig.UNSET || config.hudY == ModConfig.UNSET) {
            config.hudX = Math.max(0, client.getWindow().getScaledWidth() - DEFAULT_RIGHT_MARGIN);
            config.hudY = 10;
        }
    }

    private static String chargeText(PlayerInfo p) {
        return p.self ? String.valueOf(p.charges) : p.charges + "/" + WindChargeTracker.MAX_WIND_CHARGES;
    }

    private void renderHud(DrawContext context, TextRenderer textRenderer, List<PlayerInfo> players) {
        float scale = config.scale;

        int maxWidth = 0;
        for (PlayerInfo p : players) {
            int w = ICON_SIZE + ICON_PADDING + textRenderer.getWidth(p.name + " - " + chargeText(p));
            maxWidth = Math.max(maxWidth, w);
        }
        int width = maxWidth + PADDING * 2;
        int height = players.size() * LINE_HEIGHT + PADDING * 2;

        lastHudX = config.hudX;
        lastHudY = config.hudY;
        lastHudWidth = Math.round(width * scale);
        lastHudHeight = Math.round(height * scale);

        //? if >=1.21.6 {
        /*context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) config.hudX, (float) config.hudY);
        context.getMatrices().scale(scale, scale);
        *///?} else {
        context.getMatrices().push();
        context.getMatrices().translate(config.hudX, config.hudY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        //?}

        if (config.backgroundColor >>> 24 != 0) {
            context.fill(0, 0, width, height, config.backgroundColor);
        }

        int y = PADDING;
        for (PlayerInfo p : players) {
            int x = PADDING;
            context.drawItem(WIND_CHARGE_STACK, x, y);
            x += ICON_SIZE + ICON_PADDING;

            int textY = y + (ICON_SIZE - textRenderer.fontHeight) / 2 + 1;
            String nameText = p.name + " - ";
            context.drawTextWithShadow(textRenderer, nameText, x, textY, config.textColor);
            x += textRenderer.getWidth(nameText);
            context.drawTextWithShadow(textRenderer, chargeText(p), x, textY, getChargeColor(p.charges));

            y += LINE_HEIGHT;
        }

        //? if >=1.21.6 {
        /*context.getMatrices().popMatrix();
        *///?} else {
        context.getMatrices().pop();
        //?}
    }

    /**
     * Yumuşak geçiş: yeşil (dolu) → sarı (yarı) → kırmızı (boş). ARGB döner.
     */
    public static int getChargeColor(int charges) {
        float ratio = Math.max(0f, Math.min(1f, (float) charges / WindChargeTracker.MAX_WIND_CHARGES));
        int r, g;
        if (ratio > 0.5f) {
            float t = (ratio - 0.5f) * 2f;
            r = (int) (255 * (1f - t));
            g = (int) (170 + 85 * t);
        } else {
            r = 255;
            g = (int) (255 * ratio * 2f);
        }
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    private void renderEditOverlay(DrawContext context) {
        if (lastHudWidth <= 0 || lastHudHeight <= 0) return;
        int x1 = lastHudX - 2, y1 = lastHudY - 2;
        int x2 = lastHudX + lastHudWidth + 2, y2 = lastHudY + lastHudHeight + 2;
        int c = isDragging ? 0xFFFFFF55 : 0xFF55DDFF;
        context.fill(x1, y1, x2, y1 + 1, c);
        context.fill(x1, y2 - 1, x2, y2, c);
        context.fill(x1, y1, x1 + 1, y2, c);
        context.fill(x2 - 1, y1, x2, y2, c);
        context.fill(lastHudX, lastHudY, lastHudX + lastHudWidth, lastHudY + lastHudHeight, 0x30FFFFFF);
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!editMode || button != 0) return false;
        ensurePosition(MinecraftClient.getInstance());
        // ekranın herhangi bir yerine tıklayıp sürüklemek HUD'u taşır
        isDragging = true;
        dragOffsetX = (int) mouseX - config.hudX;
        dragOffsetY = (int) mouseY - config.hudY;
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
        int maxX = client.getWindow().getScaledWidth() - lastHudWidth;
        int maxY = client.getWindow().getScaledHeight() - lastHudHeight;
        config.hudX = Math.max(0, Math.min((int) mouseX - dragOffsetX, Math.max(0, maxX)));
        config.hudY = Math.max(0, Math.min((int) mouseY - dragOffsetY, Math.max(0, maxY)));
        return true;
    }

    public boolean isDragging() { return isDragging; }
    public boolean isEditMode() { return editMode; }
    public ModConfig getConfig() { return config; }

    private record PlayerInfo(String name, int charges, boolean self) {}
}
