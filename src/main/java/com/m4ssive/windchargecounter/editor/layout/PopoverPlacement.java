package com.m4ssive.windchargecounter.editor.layout;

/** Places a popover under its anchor, or above when there is no room, always on screen. */
public final class PopoverPlacement {
    private static final int EDGE = 4;
    private static final int CARET_INSET = 10;

    private PopoverPlacement() {}

    public static Placement place(int anchorX, int anchorTop, int anchorBottom, int w, int h,
                                  int screenW, int screenH, int gap, int targetCenterX) {
        boolean above = anchorBottom + gap + h > screenH - EDGE;
        int y = above ? anchorTop - gap - h : anchorBottom + gap;
        y = clamp(y, EDGE, Math.max(EDGE, screenH - h - EDGE));
        int x = clamp(anchorX, EDGE, Math.max(EDGE, screenW - w - EDGE));
        int caretX = clamp(targetCenterX, x + CARET_INSET, x + w - CARET_INSET);
        return new Placement(x, y, above, caretX);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
