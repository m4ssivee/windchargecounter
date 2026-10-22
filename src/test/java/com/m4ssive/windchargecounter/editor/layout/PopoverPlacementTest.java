package com.m4ssive.windchargecounter.editor.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopoverPlacementTest {
    @Test
    void smallScreenFlipsAboveAndClamps() {
        Placement p = PopoverPlacement.place(700, 300, 400, 392, 260, 854, 480, 12, 820);
        assertTrue(p.above());
        assertEquals(28, p.y());
        assertEquals(458, p.x());
        assertEquals(820, p.caretX());
    }

    @Test
    void roomBelowStaysBelow() {
        Placement p = PopoverPlacement.place(100, 50, 150, 392, 200, 1280, 720, 12, 130);
        assertFalse(p.above());
        assertEquals(162, p.y());
        assertEquals(100, p.x());
        assertEquals(130, p.caretX());
    }

    @Test
    void noRoomEitherWayStaysOnScreen() {
        Placement p = PopoverPlacement.place(10, 100, 200, 300, 400, 854, 480, 12, 50);
        assertTrue(p.y() >= 4 && p.y() + 400 <= 476, "y=" + p.y());
    }
}
