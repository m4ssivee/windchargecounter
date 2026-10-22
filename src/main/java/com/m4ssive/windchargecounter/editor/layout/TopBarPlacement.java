package com.m4ssive.windchargecounter.editor.layout;

/**
 * Puts the editor's tab bar where it does not cover the HUD:
 * top center, top left, top right (full, then compact), else bottom center.
 */
public final class TopBarPlacement {
    public static final int TOP = 9;
    private static final int EDGE = 4, MARGIN = 2, HINT_SPACE = 22 + 6;

    private TopBarPlacement() {}

    /** @param hud {x, y, w, h}; returns {x, y, compact ? 1 : 0}. */
    public static int[] place(int fullW, int compactW, int h, int screenW, int screenH, int[] hud) {
        for (int compact = 0; compact <= 1; compact++) {
            int w = compact == 0 ? fullW : compactW;
            int[] xs = {(screenW - w) / 2, EDGE, screenW - w - EDGE};
            for (int x : xs) {
                if (!overlaps(x, TOP, w, h, hud)) return new int[]{x, TOP, compact};
            }
        }
        return new int[]{(screenW - fullW) / 2, screenH - HINT_SPACE - h, 0};
    }

    private static boolean overlaps(int x, int y, int w, int h, int[] r) {
        return x < r[0] + r[2] + MARGIN && x + w > r[0] - MARGIN && y < r[1] + r[3] + MARGIN && y + h > r[1] - MARGIN;
    }
}
