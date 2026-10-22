package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditModeScreen extends Screen {

    private final Screen parent;
    private final WindChargeHud hud;
    private final ModConfig config;

    public EditModeScreen() {
        this(null);
    }

    public EditModeScreen(Screen parent) {
        super(Text.literal("Edit Mode - WindCharge HUD"));
        this.parent = parent;
        WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
        this.hud = mod.getHud();
        this.config = mod.getConfig();
        this.hud.setEditMode(true);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // vanilla blur yerine hafif karartma; oyun arkada görünsün
        context.fill(0, 0, this.width, this.height, 0x60000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        hud.render(context);

        int headerHeight = 40;
        context.fillGradient(0, 0, this.width, headerHeight, 0xFF0a1628, 0xFF0d2137);
        context.fill(0, headerHeight - 2, this.width, headerHeight, 0xAA55DDFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("§6§l✎ EDIT MODE"), this.width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("§eDrag anywhere §7to move HUD • §bScale §7in Config"),
            this.width / 2, 22, 0xE0E0E0);

        int footerY = this.height - 35;
        context.fillGradient(0, footerY, this.width, this.height, 0x80000000, 0xC0000000);

        KeyBinding key = WindChargeCounterMod.getToggleEditModeKey();
        String keyName = key != null ? key.getBoundKeyLocalizedText().getString() : "K";
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("§7Press §e" + keyName + "/ESC §7to save • §cQ §7to cancel"),
            this.width / 2, footerY + 8, 0xCCCCCC);
    }

    //? if >=1.21.9 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        return hud.handleMouseClick(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        return hud.handleMouseRelease(click.x(), click.y(), click.button()) || super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        return hud.handleMouseDrag(click.x(), click.y(), click.button(), deltaX, deltaY) || super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        KeyBinding key = WindChargeCounterMod.getToggleEditModeKey();
        if (input.key() == GLFW.GLFW_KEY_ESCAPE || (key != null && key.matchesKey(input))) {
            exit(true);
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_Q) {
            exit(false);
            return true;
        }
        return super.keyPressed(input);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return hud.handleMouseClick(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return hud.handleMouseRelease(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return hud.handleMouseDrag(mouseX, mouseY, button, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyBinding key = WindChargeCounterMod.getToggleEditModeKey();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (key != null && key.matchesKey(keyCode, scanCode))) {
            exit(true);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_Q) {
            exit(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    private void exit(boolean save) {
        if (save) config.save();
        else config.load(); // kaydedilmemiş konumu geri al
        close();
    }

    @Override
    public void close() {
        hud.setEditMode(false);
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void removed() {
        // pencere başka bir yolla kapanırsa (ör. disconnect) edit mode takılı kalmasın
        hud.setEditMode(false);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
