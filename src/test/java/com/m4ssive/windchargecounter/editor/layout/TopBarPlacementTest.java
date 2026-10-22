package com.m4ssive.windchargecounter.editor.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopBarPlacementTest {
    @Test
    void centeredWhenHudIsNotInTheWay() {
        assertArrayEquals(new int[]{473, 9, 0}, TopBarPlacement.place(333, 303, 20, 1280, 720, new int[]{1150, 300, 100, 60}));
    }

    @Test
    void movesAwayFromDefaultHudAtAutoScale() {
        int[] r = TopBarPlacement.place(333, 303, 20, 426, 240, new int[]{321, 4, 101, 61});
        int w = r[2] == 1 ? 303 : 333;
        assertEquals(9, r[1]);
        assertTrue(r[0] + w <= 321 - 2 || r[0] >= 422 + 2, "overlaps HUD: x=" + r[0] + " w=" + w);
    }

    @Test
    void goesToTheBottomWhenNothingFitsAtTheTop() {
        int[] r = TopBarPlacement.place(333, 303, 20, 426, 240, new int[]{100, 4, 200, 40});
        assertEquals(240 - 22 - 6 - 20, r[1]);
    }
}
