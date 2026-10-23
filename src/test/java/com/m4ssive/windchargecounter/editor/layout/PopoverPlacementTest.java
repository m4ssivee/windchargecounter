package com.m4ssive.windchargecounter.editor.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/** Popovers open from a tab in the editor's tab bar and must never cover the HUD. */
class PopoverPlacementTest {
    // GUI Auto on 1280×720 = 426×240; default HUD top-right; compact tab bar top-left (y 9..29)
    private static final int[] HUD_SMALL = {321, 4, 422, 65};

    private static Placement place(int[] hud, int anchorX, int anchorTop, int anchorBottom, int w, int h,
                                   int sw, int sh, int target, int minY, int maxY) {
        return PopoverPlacement.place(anchorX, anchorTop, anchorBottom, hud, w, h, sw, sh, 6, target, minY, maxY);
    }

    private static boolean overlaps(Placement p, int w, int h, int[] r) {
        return p.x() < r[2] && p.x() + w > r[0] && p.y() < r[3] && p.y() + h > r[1];
    }

    @Test
    void opensUnderTheTab() {
        Placement p = place(new int[]{1150, 300, 1250, 360}, 400, 9, 29, 212, 194, 1280, 720, 430, 35, 716);
        assertEquals(Placement.Side.BELOW, p.side());
        assertEquals(35, p.y());
        assertEquals(400, p.x());
        assertEquals(430, p.caret());
    }

    @ParameterizedTest
    @ValueSource(ints = {137, 156, 175, 194})
    void autoScaleUnderTheTabButNotOverTheHud(int h) {
        int w = h == 194 ? 212 : 160;
        Placement p = place(HUD_SMALL, 220, 9, 29, w, h, 426, 240, 245, 35, 236);
        assertEquals(Placement.Side.BELOW, p.side());
        assertTrue(p.y() >= 35 && p.y() + h <= 236, "vertical y=" + p.y());
        assertTrue(p.x() >= 4 && p.x() + w <= 422, "horizontal x=" + p.x());
        assertFalse(overlaps(p, w, h, HUD_SMALL), "covers the HUD: " + p);
        assertTrue(p.caret() >= p.x() + 10 && p.caret() <= p.x() + w - 10, "caret " + p.caret());
    }

    @Test
    void barAtTheBottomOpensAboveIt() {
        int[] hud = {100, 4, 200, 44};
        Placement p = place(hud, 150, 192, 212, 160, 137, 426, 240, 180, 4, 236);
        assertEquals(Placement.Side.ABOVE, p.side());
        assertEquals(49, p.y());
        assertFalse(overlaps(p, 160, 137, hud));
    }

    @Test
    void besideOnTheRightWhenNothingElseFits() {
        Placement p = place(new int[]{4, 40, 105, 127}, 4, 107, 127, 212, 194, 426, 240, 60, 35, 236);
        assertEquals(Placement.Side.RIGHT, p.side());
        assertEquals(111, p.x());
    }
}
