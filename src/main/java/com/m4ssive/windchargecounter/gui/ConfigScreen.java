package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen extends Screen {
    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int GAP = 4;
    private static final int[] OPACITY_STEPS = {0, 25, 50, 63, 80, 100};

    private final Screen parent;
    private final ModConfig config;

    public ConfigScreen(Screen parent) {
        super(Text.literal("WindChargeCounter"));
        this.parent = parent;
        this.config = WindChargeCounterMod.getInstance().getConfig();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - BTN_W - GAP / 2;
        int right = this.width / 2 + GAP / 2;
        int y = 36;
        int step = BTN_H + GAP;

        // sol sütun: görünüm
        toggle(left, y, "HUD", () -> config.enableHud, v -> config.enableHud = v);
        toggle(right, y, "Nametag Counter", () -> config.showInNametags, v -> config.showInNametags = v);
        y += step;
        toggle(left, y, "Show Yourself", () -> config.showSelf, v -> config.showSelf = v);
        toggle(right, y, "Reset on Death", () -> config.autoResetOnDeath, v -> config.autoResetOnDeath = v);
        y += step;
        toggle(left, y, "Header", () -> config.showHeader, v -> config.showHeader = v);
        toggle(right, y, "Flash on Throw", () -> config.flashOnThrow, v -> config.flashOnThrow = v);
        y += step;
        toggle(left, y, "Progress Bars", () -> config.showBars, v -> config.showBars = v);
        this.addDrawableChild(ButtonWidget.builder(opacityLabel(), b -> {
            int next = OPACITY_STEPS[0];
            for (int s : OPACITY_STEPS) if (s > config.panelOpacity) { next = s; break; }
            config.panelOpacity = next;
            b.setMessage(opacityLabel());
            config.save();
        }).dimensions(right, y, BTN_W, BTN_H).build());
        y += step;

        // boyut: [-] [Scale] [+]
        this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> changeScale(-0.1f))
            .dimensions(left, y, 20, BTN_H).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(String.format("Scale: %.1fx", config.scale)), b -> {
            config.scale = 1.0f;
            config.save();
            this.clearAndInit();
        }).dimensions(left + 22, y, BTN_W - 44, BTN_H).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> changeScale(0.1f))
            .dimensions(left + BTN_W - 20, y, 20, BTN_H).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Move / Resize HUD"),
            b -> this.client.setScreen(new EditModeScreen(this))
        ).dimensions(right, y, BTN_W, BTN_H).build());
        y += step;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset Counters"), b -> {
            WindChargeCounterMod.getInstance().getTracker().clearAll();
            b.setMessage(Text.literal("Counters Reset ✔"));
        }).dimensions(left, y, BTN_W, BTN_H).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset HUD Position"), b -> {
            config.hudX = ModConfig.UNSET;
            config.hudY = ModConfig.UNSET;
            config.save();
            b.setMessage(Text.literal("Position Reset ✔"));
        }).dimensions(right, y, BTN_W, BTN_H).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
            .dimensions(this.width / 2 - 100, this.height - 28, 200, BTN_H).build());
    }

    private void toggle(int x, int y, String label, Supplier<Boolean> get, Consumer<Boolean> set) {
        this.addDrawableChild(ButtonWidget.builder(toggleLabel(label, get.get()), b -> {
            boolean v = !get.get();
            set.accept(v);
            b.setMessage(toggleLabel(label, v));
            config.save();
        }).dimensions(x, y, BTN_W, BTN_H).build());
    }

    private static Text toggleLabel(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "§aON" : "§cOFF"));
    }

    private Text opacityLabel() {
        return Text.literal("Background: " + config.panelOpacity + "%");
    }

    private void changeScale(float delta) {
        config.scale = Math.round(Math.max(0.5f, Math.min(3.0f, config.scale + delta)) * 10f) / 10f;
        config.save();
        this.clearAndInit();
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFFFF);
    }
}
