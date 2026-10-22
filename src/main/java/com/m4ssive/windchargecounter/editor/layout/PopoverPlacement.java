package com.m4ssive.windchargecounter.editor.layout;

/**
 * Places a popover next to the toolbar without covering the top bar or the HUD group:
 * below, else above, else beside the HUD+toolbar group (left, then right), always on screen.
 */
public final class PopoverPlacement {
    private static final int EDGE = 4;
    private static final int CARET_INSET = 10;

    private PopoverPlacement() {}

    /**
     * @param group  {left, top, right, bottom} of the HUD + toolbar
     * @param minY   lowest allowed y (below a top bar)
     * @param maxY   highest allowed bottom edge (above a bottom bar / screen edge)
     */
    public static Placement place(int anchorX, int anchorTop, int anchorBottom, int[] group, int w, int h,
                                  int screenW, int screenH, int gap, int targetCenterX, int minY, int maxY) {
        int x = clamp(anchorX, EDGE, Math.max(EDGE, screenW - w - EDGE));
        int belowY = anchorBottom + gap;
        if (belowY + h <= maxY) {
            return new Placement(x, belowY, Placement.Side.BELOW, clamp(targetCenterX, x + CARET_INSET, x + w - CARET_INSET));
        }
        int aboveY = anchorTop - gap - h;
        if (aboveY >= minY) {
            return new Placement(x, aboveY, Placement.Side.ABOVE, clamp(targetCenterX, x + CARET_INSET, x + w - CARET_INSET));
        }
        int y = clamp(group[1], minY, Math.max(minY, maxY - h));
        int caretY = clamp((anchorTop + anchorBottom) / 2, y + CARET_INSET, y + h - CARET_INSET);
        int leftX = group[0] - gap - w;
        if (leftX >= EDGE) return new Placement(leftX, y, Placement.Side.LEFT, caretY);
        int rightX = group[2] + gap;
        if (rightX + w <= screenW - EDGE) return new Placement(rightX, y, Placement.Side.RIGHT, caretY);
        // nothing fits: stay on screen, covering as little as possible
        return new Placement(x, clamp(belowY, minY, Math.max(minY, maxY - h)), Placement.Side.BELOW,
            clamp(targetCenterX, x + CARET_INSET, x + w - CARET_INSET));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
