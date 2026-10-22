package com.m4ssive.windchargecounter.editor.tabs;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.editor.EditorScreen;
import com.m4ssive.windchargecounter.editor.Popover;
import com.m4ssive.windchargecounter.editor.layout.Snapping;
import com.m4ssive.windchargecounter.ui.AnchorGrid;
import com.m4ssive.windchargecounter.ui.Button;
import com.m4ssive.windchargecounter.ui.Label;
import com.m4ssive.windchargecounter.ui.Slider;
import com.m4ssive.windchargecounter.ui.Toggle;

import java.util.List;

/** Scale, snap-to anchors, snapping toggle, reset position (spec §7.3 Layout). */
public final class LayoutTab {
    public static final int WIDTH = 160;
    private static final int MARGIN = 4;

    private LayoutTab() {}

    public static Popover create(ModConfig cfg, EditorScreen screen) {
        Slider scale = new Slider(0, "Scale", v -> String.format("%.1fx", v), 0.5f, 3.0f, 0.1f,
            () -> cfg.scale, v -> cfg.scale = v, 0);
        AnchorGrid grid = new AnchorGrid(() -> currentAnchor(cfg, screen), (c, r) -> {
            int[] b = screen.hud().bounds();
            int[] p = Snapping.anchor(c, r, b[2], b[3], screen.width, screen.height, MARGIN);
            cfg.hudX = p[0];
            cfg.hudY = p[1];
        });
        Toggle snap = new Toggle(0, "Snap to edges", () -> cfg.snapToEdges, v -> cfg.snapToEdges = v);
        Button reset = new Button(0, 16, () -> "Reset position", false, () -> {
            cfg.hudX = ModConfig.UNSET;
            cfg.hudY = ModConfig.UNSET;
        });
        return new Popover("Layout", List.of(scale, new Label("SNAP TO"), grid, snap, reset), WIDTH, null);
    }

    /** row*3+col of the anchor the HUD sits on, or -1. */
    private static int currentAnchor(ModConfig cfg, EditorScreen screen) {
        int[] b = screen.hud().bounds();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int[] p = Snapping.anchor(c, r, b[2], b[3], screen.width, screen.height, MARGIN);
                if (Math.abs(p[0] - b[0]) <= 1 && Math.abs(p[1] - b[1]) <= 1) return r * 3 + c;
            }
        }
        return -1;
    }
}
