package com.m4ssive.windchargecounter.editor;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.editor.layout.PopoverPlacement;
import com.m4ssive.windchargecounter.editor.layout.SnapResult;
import com.m4ssive.windchargecounter.editor.layout.Snapping;
import com.m4ssive.windchargecounter.editor.layout.TopBarPlacement;
import com.m4ssive.windchargecounter.editor.tabs.CounterTab;
import com.m4ssive.windchargecounter.editor.tabs.LayoutTab;
import com.m4ssive.windchargecounter.editor.tabs.NametagTab;
import com.m4ssive.windchargecounter.editor.tabs.ThemeTab;
import com.m4ssive.windchargecounter.gui.WindChargeHud;
import com.m4ssive.windchargecounter.ui.Draw;
import com.m4ssive.windchargecounter.ui.UiTheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * The one WCC editor: game stays visible, HUD is edited in place, settings live in popovers.
 * All version-specific input lives in this class and is translated into the widget kit's model.
 */
public class EditorScreen extends Screen {
    public enum Tab { LAYOUT, COUNTER, NAMETAG, THEME }

    private static final int DIM = 0x610A1020;
    private static final int SNAP_THRESHOLD = 6;
    private static final int POPOVER_GAP = 6;
    private static final String HINT = "Click select · Drag move · Scroll resize · Arrows nudge 1px";

    private final Screen parent;
    private final ModConfig config;
    private final ModConfig snapshot;
    private final WindChargeHud hud;
    private final TopBar topBar;

    private Tab activeTab = Tab.LAYOUT;
    /** Open popover on top of everything, null when closed. */
    private Popover overlay;
    private Tab overlayTab;
    /** Color picker opened from the Theme popover; drawn above everything. */
    private ColorPicker picker;
    private final Toolbar toolbar;

    private boolean dragging;
    private boolean dragFree;
    private int dragOffX, dragOffY;
    private Integer guideX, guideY;

    public EditorScreen(Screen parent) {
        super(Text.literal("WCC Editor"));
        this.parent = parent;
        WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
        this.config = mod.getConfig();
        this.hud = mod.getHud();
        this.snapshot = config.copy();
        this.topBar = new TopBar(() -> activeTab, this::openTab, this::cancel, this::done);
        this.toolbar = new Toolbar(this, config);
    }

    public ModConfig config() {
        return config;
    }

    public WindChargeHud hud() {
        return hud;
    }

    public Tab activeTab() {
        return activeTab;
    }

    /** Selects a tab and opens its popover. */
    public void openTab(Tab tab) {
        activeTab = tab;
        overlay = createPopover(tab);
        overlayTab = overlay == null ? null : tab;
    }

    /** Opens the tab's popover, or closes it when it is already open. */
    public void togglePopover(Tab tab) {
        if (isPopoverOpen(tab)) closeOverlay();
        else openTab(tab);
    }

    public boolean isPopoverOpen(Tab tab) {
        return overlay != null && overlayTab == tab;
    }

    public void closeOverlay() {
        overlay = null;
        overlayTab = null;
        picker = null;
    }

    /** Opens the color picker at (x, y) for one color field. */
    public void openPicker(int x, int y, IntSupplier get, IntConsumer set) {
        picker = new ColorPicker(get, set);
        picker.placeNear(x, y, this.width, this.height);
    }

    protected Popover createPopover(Tab tab) {
        return switch (tab) {
            case LAYOUT -> LayoutTab.create(config, this);
            case COUNTER -> CounterTab.create(config, this);
            case NAMETAG -> NametagTab.create(config, this);
            case THEME -> ThemeTab.create(config, this);
        };
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void init() {
        layoutTopBar();
    }

    /** Keeps the tab bar off the HUD (re-run every frame because the HUD can move). */
    private void layoutTopBar() {
        int full = topBar.measure(false), compact = topBar.measure(true);
        int[] r = TopBarPlacement.place(full, compact, topBar.h, this.width, this.height, hud.bounds());
        topBar.setPosition(r[0], r[1], r[2] == 1);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Esc is handled as "Done"
    }

    private void done() {
        config.save();
        returnToParent();
    }

    private void cancel() {
        config.copyFrom(snapshot);
        config.save();
        returnToParent();
    }

    private void returnToParent() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void close() {
        done();
    }

    // ------------------------------------------------------------------ render

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // outside a world (Mod Menu on the title screen) draw the normal menu background first
        if (this.client == null || this.client.world == null) super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, this.width, this.height, DIM);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        UiTheme t = UiTheme.from(config);
        renderBackground(ctx, mouseX, mouseY, delta);
        hud.render(ctx, true);

        if (dragging) {
            int g = UiTheme.alpha(t.accent(), 160);
            if (guideX != null) Draw.dashedV(ctx, Math.min(guideX, this.width - 1), 0, this.height, g);
            if (guideY != null) Draw.dashedH(ctx, Math.min(guideY, this.height - 1), 0, this.width, g);
        }

        int[] hb = hud.bounds();
        toolbar.layout(hb, this.width, this.height);
        layoutTopBar();
        toolbar.render(ctx, mouseX, mouseY, t);
        topBar.render(ctx, mouseX, mouseY, t);
        renderHint(ctx, t);
        if (overlay != null) {
            int target = overlayTab == Tab.THEME ? toolbar.chipCenterX() : toolbar.moreCenterX();
            int[] group = {Math.min(hb[0], toolbar.x), Math.min(hb[1], toolbar.y),
                Math.max(hb[0] + hb[2], toolbar.x + toolbar.w), Math.max(hb[1] + hb[3], toolbar.y + toolbar.h)};
            boolean barOnTop = topBar.y < this.height / 2;
            int minY = barOnTop ? topBar.y + topBar.h + POPOVER_GAP : 4;
            int maxY = barOnTop ? this.height - 4 : topBar.y - POPOVER_GAP;
            overlay.place(PopoverPlacement.place(toolbar.x, toolbar.y, toolbar.y + toolbar.h, group,
                overlay.w, overlay.h, this.width, this.height, POPOVER_GAP, target, minY, maxY));
            overlay.render(ctx, mouseX, mouseY, t);
        }
        if (picker != null) picker.render(ctx, mouseX, mouseY, t);
    }

    private void renderHint(DrawContext ctx, UiTheme t) {
        int w = Draw.textWidth(HINT) + 16;
        int x = (this.width - w) / 2, y = this.height - 22;
        Draw.rect(ctx, x, y, w, 14, UiTheme.alpha(t.surface(), 225));
        Draw.border(ctx, x, y, w, 14, 1, UiTheme.alpha(t.accent(), 60));
        Draw.text(ctx, HINT, x + 8, y + 3, 0xFFC7D3E6);
    }

    // ------------------------------------------------------------------ internal input model

    private boolean onClick(double mx, double my, int button, boolean shift) {
        if (picker != null) {
            if (!picker.mouseClicked(mx, my, button)) picker = null; // outside: close the picker only
            return true;
        }
        if (overlay != null) {
            if (overlay.mouseClicked(mx, my, button)) return true;
            if (!overlay.contains(mx, my) && !toolbar.contains(mx, my)) {
                closeOverlay(); // click outside closes the popover
            }
        }
        if (topBar.mouseClicked(mx, my, button)) return true;
        if (toolbar.mouseClicked(mx, my, button)) return true;

        int[] b = hud.bounds();
        if (button == 0 && mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3]) {
            dragging = true;
            dragFree = shift || !config.snapToEdges;
            dragOffX = (int) mx - b[0];
            dragOffY = (int) my - b[1];
            return true;
        }
        return false;
    }

    private boolean onDrag(double mx, double my, int button) {
        if (overlay != null && overlay.mouseDragged(mx, my, button)) return true;
        if (!dragging) return false;
        int[] b = hud.bounds();
        int nx = (int) mx - dragOffX, ny = (int) my - dragOffY;
        SnapResult r = Snapping.snap(nx, ny, b[2], b[3], this.width, this.height, dragFree ? 0 : SNAP_THRESHOLD);
        config.hudX = r.x();
        config.hudY = r.y();
        guideX = r.guideX();
        guideY = r.guideY();
        return true;
    }

    private boolean onRelease(double mx, double my, int button) {
        if (overlay != null && overlay.mouseReleased(mx, my, button)) return true;
        if (dragging) {
            dragging = false;
            guideX = guideY = null;
            return true;
        }
        return false;
    }

    private boolean onScroll(double mx, double my, double amount) {
        if (overlay != null && overlay.mouseScrolled(mx, my, amount)) return true;
        int[] b = hud.bounds();
        if (amount != 0 && mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3]) {
            changeScale(amount > 0 ? 0.1f : -0.1f);
            return true;
        }
        return false;
    }

    /** Changes the HUD scale by delta, clamped to 0.5–3.0. */
    public void changeScale(float delta) {
        config.scale = Math.round(Math.max(0.5f, Math.min(3.0f, config.scale + delta)) * 10f) / 10f;
    }

    private boolean onKey(int key, int mods, boolean editorKey) {
        if (picker != null) {
            if (picker.keyPressed(key, mods)) return true;
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                picker = null;
                return true;
            }
            if (picker.isTyping()) return true; // don't trigger the editor key while typing hex
        }
        if (overlay != null && overlay.keyPressed(key, mods)) return true;
        if (key == GLFW.GLFW_KEY_ESCAPE || editorKey) {
            done();
            return true;
        }
        int step = (mods & GLFW.GLFW_MOD_SHIFT) != 0 ? 10 : 1;
        int[] b = hud.bounds();
        int dx = 0, dy = 0;
        switch (key) {
            case GLFW.GLFW_KEY_LEFT -> dx = -step;
            case GLFW.GLFW_KEY_RIGHT -> dx = step;
            case GLFW.GLFW_KEY_UP -> dy = -step;
            case GLFW.GLFW_KEY_DOWN -> dy = step;
            default -> { return false; }
        }
        SnapResult r = Snapping.snap(b[0] + dx, b[1] + dy, b[2], b[3], this.width, this.height, 0);
        config.hudX = r.x();
        config.hudY = r.y();
        return true;
    }

    private boolean onChar(String chars) {
        if (picker != null && picker.charTyped(chars)) return true;
        return overlay != null && overlay.charTyped(chars);
    }

    // ------------------------------------------------------------------ version adapters

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return onScroll(mouseX, mouseY, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    //? if >=1.21.9 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        boolean shift = (click.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
        return onClick(click.x(), click.y(), click.button(), shift) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        return onRelease(click.x(), click.y(), click.button()) || super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        return onDrag(click.x(), click.y(), click.button()) || super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        KeyBinding k = WindChargeCounterMod.getToggleEditModeKey();
        boolean editorKey = k != null && k.matchesKey(input);
        return onKey(input.key(), input.modifiers(), editorKey) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        return onChar(input.asString()) || super.charTyped(input);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onClick(mouseX, mouseY, button, Screen.hasShiftDown()) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return onRelease(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return onDrag(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyBinding k = WindChargeCounterMod.getToggleEditModeKey();
        boolean editorKey = k != null && k.matchesKey(keyCode, scanCode);
        return onKey(keyCode, modifiers, editorKey) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return onChar(String.valueOf(chr)) || super.charTyped(chr, modifiers);
    }
    //?}
}
