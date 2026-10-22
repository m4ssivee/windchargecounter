package com.m4ssive.windchargecounter.editor.layout;

/**
 * Where a popover goes: top-left corner, which side of its anchor it opened on, and the caret
 * position (x for BELOW/ABOVE, y for LEFT/RIGHT).
 */
public record Placement(int x, int y, Side side, int caret) {
    public enum Side { BELOW, ABOVE, LEFT, RIGHT }

    public boolean above() {
        return side == Side.ABOVE;
    }
}
