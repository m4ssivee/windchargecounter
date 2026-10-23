package com.m4ssive.windchargecounter.editor.layout;

/**
 * Places a popover next to the tab that opened it without covering the HUD: below the tab (or above it when
 * the tab bar sits at the bottom), slid sideways off the HUD when needed, else beside the HUD; always on screen.
 */
public final class PopoverPlacement {
    private static final int EDGE = 4;
    private static final int CARET_INSET = 10;

    private PopoverPlacement() {}

    /**
     * @param avoid {left, top, right, bottom} the popover must not cover (the HUD)
     * @param minY  lowest allowed y
     * @param maxY  highest allowed bottom edge
     */
    public static Placement place(int anchorX, int anchorTop, int anchorBottom, int[] avoid, int w, int h,
                                  int screenW, int screenH, int gap, int targetCenterX, int minY, int maxY) {
        int belowY = anchorBottom + gap;
        if (belowY >= minY && belowY + h <= maxY) {
            Integer x = freeX(anchorX, belowY, w, h, avoid, screenW, gap);
            if (x != null) return vertical(x, belowY, Placement.Side.BELOW, w, targetCenterX);
        }
        int aboveY = anchorTop - gap - h;
        if (aboveY >= minY && aboveY + h <= maxY) {
            Integer x = freeX(anchorX, aboveY, w, h, avoid, screenW, gap);
            if (x != null) return vertical(x, aboveY, Placement.Side.ABOVE, w, targetCenterX);
        }
        int y = clamp(avoid[1], minY, Math.max(minY, maxY - h));
        int caretY = clamp((anchorTop + anchorBottom) / 2, y + CARET_INSET, y + h - CARET_INSET);
        int leftX = avoid[0] - gap - w;
        if (leftX >= EDGE) return new Placement(leftX, y, Placement.Side.LEFT, caretY);
        int rightX = avoid[2] + gap;
        if (rightX + w <= screenW - EDGE) return new Placement(rightX, y, Placement.Side.RIGHT, caretY);
        // nothing fits: stay on screen, covering as little as possible
        int x = clamp(anchorX, EDGE, Math.max(EDGE, screenW - w - EDGE));
        return vertical(x, clamp(belowY, minY, Math.max(minY, maxY - h)), Placement.Side.BELOW, w, targetCenterX);
    }

    /** x near the anchor that keeps the popover off {@code avoid}, or null when there is none. */
    private static Integer freeX(int anchorX, int y, int w, int h, int[] avoid, int screenW, int gap) {
        int maxX = Math.max(EDGE, screenW - w - EDGE);
        int x = clamp(anchorX, EDGE, maxX);
        boolean verticalHit = y < avoid[3] && y + h > avoid[1];
        if (!verticalHit || x >= avoid[2] || x + w <= avoid[0]) return x;
        int left = avoid[0] - gap - w;
        if (left >= EDGE) return left;
        int right = avoid[2] + gap;
        if (right <= maxX) return right;
        return null;
    }

    private static Placement vertical(int x, int y, Placement.Side side, int w, int targetCenterX) {
        return new Placement(x, y, side, clamp(targetCenterX, x + CARET_INSET, x + w - CARET_INSET));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
