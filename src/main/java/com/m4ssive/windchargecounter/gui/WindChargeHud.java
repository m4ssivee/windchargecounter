package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.WindChargeTracker;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.util.GuiHelper;
import com.m4ssive.windchargecounter.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.*;

public class WindChargeHud {
    private final WindChargeTracker tracker;
    private final ModConfig config;
    private static final ItemStack WIND_CHARGE_STACK = new ItemStack(Items.WIND_CHARGE);

    private boolean editMode = false;
    private boolean isDragging = false;
    private int dragStartX = 0;
    private int dragStartY = 0;

    private int lastHudX, lastHudY, lastHudWidth, lastHudHeight;

    public WindChargeHud(WindChargeTracker tracker, ModConfig config) {
        this.tracker = tracker;
        this.config = config;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        if (!editMode) {
            isDragging = false;
        }
    }

    public void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!config.enableHud && !editMode) return;
        if (!tracker.hasWindCharge() && !editMode) return;

        List<PlayerInfo> players = new ArrayList<>();

        if (config.showSelf) {
            int myCharges = tracker.getLocalPlayerCharges();
            players.add(new PlayerInfo("You", myCharges, client.player.getUuid()));
        }

        Map<UUID, Integer> opponentUsage = tracker.getAllOpponentUsage();
        for (Map.Entry<UUID, Integer> entry : opponentUsage.entrySet()) {
            PlayerEntity player = client.world.getPlayerByUuid(entry.getKey());
            if (player == null) continue;
            int remaining = WindChargeTracker.MAX_WIND_CHARGES - entry.getValue();
            players.add(new PlayerInfo(player.getName().getString(), remaining, player.getUuid()));
        }

        if (editMode && players.isEmpty()) {
            players.add(new PlayerInfo("You", 120, UUID.randomUUID()));
            players.add(new PlayerInfo("Enemy", 95, UUID.randomUUID()));
        }

        if (players.isEmpty()) return;

        renderHud(context, client, players);

        if (editMode) {
            renderEditOverlay(context, client);
        }
    }

    private void renderHud(DrawContext context, MinecraftClient client, List<PlayerInfo> players) {
        TextRenderer textRenderer = client.textRenderer;

        int hudX = config.hudX > -9999 ? config.hudX : client.getWindow().getScaledWidth() - 160;
        int hudY = config.hudY > -9999 ? config.hudY : 10;

        GuiHelper.push(context);
        GuiHelper.scale(context, config.scale, config.scale, 1.0f);

        int scaledX = (int) (hudX / config.scale);
        int scaledY = (int) (hudY / config.scale);

        int padding = 2;
        int lineHeight = 16;
        int iconSize = 16;
        int iconPadding = 2;

        int maxWidth = 0;
        for (PlayerInfo player : players) {
            boolean isSelf = player.name.equals("You") || (client.getSession() != null && player.name.equals(client.getSession().getUsername()));
            String maxDisplay = isSelf ? "" : "/" + WindChargeTracker.MAX_WIND_CHARGES;
            String line = player.name + " - " + player.charges + maxDisplay;
            int w = textRenderer.getWidth(line) + iconSize + iconPadding + 10;
            if (w > maxWidth) maxWidth = w;
        }

        int totalWidth = maxWidth + padding * 2;
        int totalHeight = players.size() * lineHeight + padding * 2;

        lastHudX = hudX;
        lastHudY = hudY;
        lastHudWidth = (int) (totalWidth * config.scale);
        lastHudHeight = (int) (totalHeight * config.scale);

        if (editMode) {
            context.fill(scaledX, scaledY, scaledX + totalWidth, scaledY + totalHeight, 0x40000000); // Faint bg in edit mode
        }

        int yOffset = scaledY + padding;

        for (PlayerInfo player : players) {
            int xOffset = scaledX + padding;

            context.drawItem(WIND_CHARGE_STACK, xOffset, yOffset);
            xOffset += iconSize + iconPadding;

            int textY = yOffset + (iconSize - textRenderer.fontHeight) / 2;

            String nameText = player.name + " - ";
            TextHelper.drawTextWithShadow(context, textRenderer, Text.literal(nameText), xOffset, textY, config.textColor);
            xOffset += textRenderer.getWidth(nameText);

            int chargeColor = getChargeColor(player.charges);
            boolean isSelf = player.name.equals("You") || (client.getSession() != null && player.name.equals(client.getSession().getUsername()));
            String maxDisplay = isSelf ? "" : "/" + WindChargeTracker.MAX_WIND_CHARGES;
            String chargeText = player.charges + maxDisplay;
            TextHelper.drawTextWithShadow(context, textRenderer, Text.literal(chargeText), xOffset, textY, chargeColor);

            yOffset += lineHeight;
        }

        GuiHelper.pop(context);
    }

    /**
     * Smooth gradient: dark green (full) → yellow (mid) → light red (empty)
     */
    public static int getChargeColor(int charges) {
        float ratio = (float) charges / WindChargeTracker.MAX_WIND_CHARGES;
        ratio = Math.max(0f, Math.min(1f, ratio));

        int r, g, b;
        if (ratio > 0.5f) {
            // Green → Yellow (ratio 1.0 → 0.5)
            float t = (ratio - 0.5f) * 2f; // 1.0 at full, 0.0 at mid
            r = (int) (255 * (1f - t));
            g = (int) (170 + 85 * t); // 170→255 (darker green at top)
            b = 0;
        } else {
            // Yellow → Red (ratio 0.5 → 0.0)
            float t = ratio * 2f; // 1.0 at mid, 0.0 at empty
            r = 255;
            g = (int) (255 * t);
            b = 0;
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void renderEditOverlay(DrawContext context, MinecraftClient client) {
        if (lastHudWidth > 0 && lastHudHeight > 0) {
            // Draw border around HUD area
            int x1 = lastHudX - 2;
            int y1 = lastHudY - 2;
            int x2 = lastHudX + lastHudWidth + 2;
            int y2 = lastHudY + lastHudHeight + 2;
            // Top
            context.fill(x1, y1, x2, y1 + 1, 0xFF55DDFF);
            // Bottom
            context.fill(x1, y2 - 1, x2, y2, 0xFF55DDFF);
            // Left
            context.fill(x1, y1, x1 + 1, y2, 0xFF55DDFF);
            // Right
            context.fill(x2 - 1, y1, x2, y2, 0xFF55DDFF);
            // Faint background
            context.fill(lastHudX, lastHudY, lastHudX + lastHudWidth, lastHudY + lastHudHeight, 0x30FFFFFF);
        }
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!editMode || button != 0) return false;

        // Ensure hudX/hudY are initialized
        if (config.hudX <= -9999 || config.hudY <= -9999) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                config.hudX = client.getWindow().getScaledWidth() - 160;
                config.hudY = 10;
            }
        }

        // Click anywhere on screen starts dragging the HUD
        isDragging = true;
        dragStartX = (int) mouseX - config.hudX;
        dragStartY = (int) mouseY - config.hudY;
        return true;
    }

    public boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        if (!editMode || button != 0) return false;
        boolean was = isDragging;
        isDragging = false;
        return was;
    }

    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!editMode) return false;

        if (isDragging) {
            config.hudX = (int) mouseX - dragStartX;
            config.hudY = (int) mouseY - dragStartY;
            // Clamp to screen bounds
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                int maxX = client.getWindow().getScaledWidth() - (int)(lastHudWidth > 0 ? lastHudWidth / config.scale : 10);
                int maxY = client.getWindow().getScaledHeight() - (int)(lastHudHeight > 0 ? lastHudHeight / config.scale : 10);
                config.hudX = Math.max(0, Math.min(config.hudX, maxX));
                config.hudY = Math.max(0, Math.min(config.hudY, maxY));
            }
            return true;
        }

        return false;
    }

    private boolean isOver(double mx, double my) {
        return mx >= lastHudX && mx <= lastHudX + lastHudWidth && my >= lastHudY && my <= lastHudY + lastHudHeight;
    }

    public boolean isDragging() { return isDragging; }
    public boolean isEditMode() { return editMode; }
    public ModConfig getConfig() { return config; }

    private static class PlayerInfo {
        final String name;
        final int charges;
        final UUID uuid;
        PlayerInfo(String name, int charges, UUID uuid) {
            this.name = name;
            this.charges = charges;
            this.uuid = uuid;
        }
    }
}
