package com.m4ssive.windchargecounter.editor.layout;

/** Edge/center snapping and 3×3 anchors for the HUD. Pure math, no Minecraft classes. */
public final class Snapping {
    private Snapping() {}

    public static SnapResult snap(int x, int y, int w, int h, int screenW, int screenH, int threshold) {
        int[] sx = axis(x, w, screenW, threshold);
        int[] sy = axis(y, h, screenH, threshold);
        return new SnapResult(sx[0], sy[0], sx[1] < 0 ? null : sx[1], sy[1] < 0 ? null : sy[1]);
    }

    /** Returns {position, guideLine or -1}. */
    private static int[] axis(int pos, int size, int screen, int threshold) {
        int max = Math.max(0, screen - size);
        int[] targets = {0, max, max / 2};
        int[] guides = {0, screen, screen / 2};
        int best = -1, bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < targets.length; i++) {
            int d = Math.abs(pos - targets[i]);
            if (d <= threshold && d < bestDist) {
                best = i;
                bestDist = d;
            }
        }
        if (best >= 0) return new int[]{targets[best], guides[best]};
        return new int[]{Math.max(0, Math.min(max, pos)), -1};
    }

    /** Position for anchor (col,row) ∈ {0,1,2}², kept {@code margin} px from the screen edges. */
    public static int[] anchor(int col, int row, int w, int h, int screenW, int screenH, int margin) {
        return new int[]{pick(col, w, screenW, margin), pick(row, h, screenH, margin)};
    }

    private static int pick(int i, int size, int screen, int margin) {
        return switch (i) {
            case 0 -> margin;
            case 1 -> (screen - size) / 2;
            default -> screen - size - margin;
        };
    }
}
