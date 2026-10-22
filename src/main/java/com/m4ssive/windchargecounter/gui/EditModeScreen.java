package com.m4ssive.windchargecounter.gui;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Temporary shell; replaced by EditorScreen in the next task. */
public class EditModeScreen extends Screen {
    private final Screen parent;

    public EditModeScreen() {
        this(null);
    }

    public EditModeScreen(Screen parent) {
        super(Text.literal("WCC Editor"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        WindChargeCounterMod.getInstance().getHud().render(context, true);
    }

    @Override
    public void close() {
        WindChargeCounterMod.getInstance().getConfig().save();
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
