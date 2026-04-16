package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    public ConfigScreen(Screen parent) {
        super(Text.literal("WindChargeCounter Config"));
        this.parent = parent;
        this.config = WindChargeCounterMod.getInstance().getConfig();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int btnHeight = 20;
        int spacing = 24;

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Enable HUD: " + (config.enableHud ? "ON" : "OFF")),
            button -> {
                config.enableHud = !config.enableHud;
                button.setMessage(Text.literal("Enable HUD: " + (config.enableHud ? "ON" : "OFF")));
                config.save();
            }
        ).dimensions(centerX - 100, startY, 200, btnHeight).build());

        startY += spacing;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Show Self: " + (config.showSelf ? "ON" : "OFF")),
            button -> {
                config.showSelf = !config.showSelf;
                button.setMessage(Text.literal("Show Self: " + (config.showSelf ? "ON" : "OFF")));
                config.save();
            }
        ).dimensions(centerX - 100, startY, 200, btnHeight).build());

        startY += spacing;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Show in Nametags: " + (config.showInNametags ? "ON" : "OFF")),
            button -> {
                config.showInNametags = !config.showInNametags;
                button.setMessage(Text.literal("Show in Nametags: " + (config.showInNametags ? "ON" : "OFF")));
                config.save();
            }
        ).dimensions(centerX - 100, startY, 200, btnHeight).build());
        
        startY += spacing;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto Reset on Death: " + (config.autoResetOnDeath ? "ON" : "OFF")),
            button -> {
                config.autoResetOnDeath = !config.autoResetOnDeath;
                button.setMessage(Text.literal("Auto Reset on Death: " + (config.autoResetOnDeath ? "ON" : "OFF")));
                config.save();
            }
        ).dimensions(centerX - 100, startY, 200, btnHeight).build());

        startY += spacing;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Reset All Counters"),
            button -> {
                if (WindChargeCounterMod.getInstance().getTracker() != null) {
                    WindChargeCounterMod.getInstance().getTracker().clearAll();
                    button.setMessage(Text.literal("Reset All Counters (Reset!)"));
                }
            }
        ).dimensions(centerX - 100, startY, 200, btnHeight).build());

        // Scale controls: [-] [value] [+]
        startY += spacing;
        final int scaleY = startY;
        // Decrease scale
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("-"),
            button -> {
                config.scale = Math.max(0.5f, config.scale - 0.25f);
                config.save();
                this.clearAndInit();
            }
        ).dimensions(centerX - 100, scaleY, 30, btnHeight).build());
        // Scale display
        String scaleText = String.format("Scale: %.2fx", config.scale);
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(scaleText),
            button -> {
                // Reset to default
                config.scale = 1.0f;
                config.save();
                this.clearAndInit();
            }
        ).dimensions(centerX - 68, scaleY, 136, btnHeight).build());
        // Increase scale
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("+"),
            button -> {
                config.scale = Math.min(3.0f, config.scale + 0.25f);
                config.save();
                this.clearAndInit();
            }
        ).dimensions(centerX + 70, scaleY, 30, btnHeight).build());

        startY += spacing;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Edit HUD Position"),
            button -> {
                this.client.setScreen(new EditModeScreen());
            }
        ).dimensions(centerX - 100, startY, 200, btnHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Back"),
            button -> {
                this.client.setScreen(parent);
            }
        ).dimensions(centerX - 100, this.height - 30, 200, btnHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
