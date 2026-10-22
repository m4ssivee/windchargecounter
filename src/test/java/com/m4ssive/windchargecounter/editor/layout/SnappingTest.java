package com.m4ssive.windchargecounter.editor.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnappingTest {
    @Test
    void snapsToLeftEdge() {
        SnapResult r = Snapping.snap(5, 300, 100, 60, 1280, 720, 6);
        assertEquals(0, r.x());
        assertEquals(0, r.guideX());
        assertEquals(300, r.y());
        assertNull(r.guideY());
    }

    @Test
    void snapsToHorizontalCenterAndTop() {
        SnapResult r = Snapping.snap(593, 3, 100, 60, 1280, 720, 6);
        assertEquals(590, r.x());
        assertEquals(640, r.guideX());
        assertEquals(0, r.y());
    }

    @Test
    void clampsOffScreenPositions() {
        SnapResult r = Snapping.snap(1300, 800, 100, 60, 1280, 720, 6);
        assertEquals(1180, r.x());
        assertEquals(660, r.y());
    }

    @Test
    void zeroThresholdOnlyClamps() {
        SnapResult r = Snapping.snap(3, 3, 100, 60, 1280, 720, 0);
        assertEquals(3, r.x());
        assertNull(r.guideX());
    }

    @Test
    void anchorTopRightWithMargin() {
        assertArrayEquals(new int[]{1176, 4}, Snapping.anchor(2, 0, 100, 60, 1280, 720, 4));
        assertArrayEquals(new int[]{590, 330}, Snapping.anchor(1, 1, 100, 60, 1280, 720, 4));
    }
}
