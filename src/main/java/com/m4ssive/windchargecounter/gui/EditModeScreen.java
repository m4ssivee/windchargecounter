package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.util.TextHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EditModeScreen extends Screen {

    private final WindChargeHud hud;
    private final ModConfig config;

    public EditModeScreen() {
        super(Text.literal("Edit Mode - WindCharge HUD"));
        WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
        this.hud = mod != null ? mod.getHud() : null;
        this.config = mod != null ? mod.getConfig() : null;
        if (this.hud != null) {
            this.hud.setEditMode(true);
        }
    }

    @Override
    protected void init() {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x60000000);
        if (hud != null) {
            hud.render(context, delta);
        }

        int headerHeight = 40;
        context.fillGradient(0, 0, this.width, headerHeight, 0xFF0a1628, 0xFF0d2137);
        context.fill(0, headerHeight - 2, this.width, headerHeight, 0xAA55DDFF);

        TextHelper.drawCenteredTextWithShadow(context, this.textRenderer,
            Text.literal("\u00a76\u00a7l\u270e EDIT MODE"),
            this.width / 2, 10, 0xFFFFFF);

        TextHelper.drawCenteredTextWithShadow(context, this.textRenderer,
            Text.literal("\u00a7eDrag anywhere \u00a77to move HUD \u2022 \u00a7bScale \u00a77in Config"),
            this.width / 2, 22, 0xE0E0E0);

        int footerY = this.height - 35;
        context.fillGradient(0, footerY, this.width, this.height, 0x80000000, 0xC0000000);

        TextHelper.drawCenteredTextWithShadow(context, this.textRenderer,
            Text.literal("\u00a77Press \u00a7eK/ESC \u00a77to save \u2022 \u00a7cQ \u00a77to cancel"),
            this.width / 2, footerY + 8, 0xCCCCCC);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hud != null) {
            return hud.handleMouseClick(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (hud != null) {
            return hud.handleMouseRelease(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (hud != null) {
            return hud.handleMouseDrag(mouseX, mouseY, button, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 75 || keyCode == 256) {
            exitEditMode(true);
            return true;
        }
        if (keyCode == 81) {
            exitEditMode(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void exitEditMode(boolean save) {
        if (hud != null) hud.setEditMode(false);
        if (save && config != null) {
            config.save();
        } else if (config != null) {
            config.load();
        }
        this.close();
    }

    @Override
    public void close() {
        if (hud != null) hud.setEditMode(false);
        if (this.client != null) this.client.setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
