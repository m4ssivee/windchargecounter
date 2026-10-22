package com.m4ssive.windchargecounter.nametag;

import com.m4ssive.windchargecounter.nametag.SuffixFormat.Segments;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuffixFormatTest {
    @Test
    void defaultPipeWithIcon() {
        assertEquals(new Segments("|", true, "74", false), SuffixFormat.segments(74, 128, "|", true, false, 16));
    }

    @Test
    void noSeparatorNoIconLowWarning() {
        assertEquals(new Segments("", false, "9", true), SuffixFormat.segments(9, 128, "", false, false, 16));
    }

    @Test
    void showMaxAndWarningAtThreshold() {
        assertEquals(new Segments("-", true, "16/128", true), SuffixFormat.segments(16, 128, "-", true, true, 16));
    }

    @Test
    void bracketSeparatorWrapsCount() {
        assertEquals(new Segments("[", true, "74]", false), SuffixFormat.segments(74, 128, "[]", true, false, 16));
    }

    @Test
    void thresholdZeroDisablesWarning() {
        assertFalse(SuffixFormat.segments(0, 128, "|", true, false, 0).warning());
    }

    @Test
    void countAbove128() {
        assertEquals("140/128", SuffixFormat.segments(140, 128, "|", true, true, 16).count());
        assertFalse(SuffixFormat.segments(140, 128, "|", true, true, 16).warning());
    }
}
