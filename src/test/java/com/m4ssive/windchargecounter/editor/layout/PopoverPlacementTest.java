package com.m4ssive.windchargecounter.editor.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PopoverPlacementTest {
    // HUD group at GUI Auto on 1280×720 (426×240): HUD 321..422 × 4..65, toolbar 71..91
    private static final int[] GROUP_SMALL = {321, 4, 422, 91};

    private static Placement place(int[] group, int anchorX, int anchorTop, int anchorBottom, int w, int h,
                                   int sw, int sh, int target, int minY, int maxY) {
        return PopoverPlacement.place(anchorX, anchorTop, anchorBottom, group, w, h, sw, sh, 6, target, minY, maxY);
    }

    @Test
    void roomBelowStaysBelow() {
        Placement p = place(new int[]{100, 30, 300, 150}, 100, 130, 150, 160, 200, 1280, 720, 130, 35, 716);
        assertEquals(Placement.Side.BELOW, p.side());
        assertEquals(156, p.y());
        assertEquals(100, p.x());
        assertEquals(130, p.caret());
    }

    @Test
    void flipsAboveWhenOnlyAboveFits() {
        Placement p = place(new int[]{500, 300, 700, 420}, 500, 400, 420, 160, 200, 854, 480, 650, 35, 476);
        assertEquals(Placement.Side.ABOVE, p.side());
        assertEquals(194, p.y());
    }

    @ParameterizedTest
    @ValueSource(ints = {137, 156, 175, 194})
    void autoScale1280x720GoesBesideTheHudAndAvoidsTheTopBar(int h) {
        int w = h == 194 ? 212 : 160;
        Placement p = place(GROUP_SMALL, 321, 71, 91, w, h, 426, 240, 415, 35, 236);
        assertTrue(p.y() >= 35 && p.y() + h <= 236, "vertical y=" + p.y());
        assertTrue(p.x() >= 4 && p.x() + w <= 422, "horizontal x=" + p.x());
        boolean overlapsGroup = p.x() < GROUP_SMALL[2] && p.x() + w > GROUP_SMALL[0]
            && p.y() < GROUP_SMALL[3] && p.y() + h > GROUP_SMALL[1];
        assertFalse(overlapsGroup, "covers HUD/toolbar: " + p);
    }

    @Test
    void besideOnTheRightWhenHudIsOnTheLeft() {
        Placement p = place(new int[]{4, 40, 105, 127}, 4, 107, 127, 212, 194, 426, 240, 60, 35, 236);
        assertEquals(Placement.Side.RIGHT, p.side());
        assertEquals(111, p.x());
    }
}
