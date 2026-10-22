package com.m4ssive.windchargecounter.theme;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class ColorMathTest {
    private static final int FULL = 0x5FE36A, HALF = 0xD9E04A, LOW = 0xFF6A4A;

    @Test
    void argbSetsAlpha() {
        assertEquals(0xFF5FE36A, ColorMath.argb(0x5FE36A, 255));
        assertEquals(0x805FE36A, ColorMath.argb(0x5FE36A, 128));
    }

    @Test
    void countColorHitsTheThreeStops() {
        assertEquals(FULL, ColorMath.countColor(128, 128, FULL, HALF, LOW));
        assertEquals(HALF, ColorMath.countColor(64, 128, FULL, HALF, LOW));
        assertEquals(LOW, ColorMath.countColor(0, 128, FULL, HALF, LOW));
    }

    @Test
    void countColorClamps() {
        assertEquals(FULL, ColorMath.countColor(140, 128, FULL, HALF, LOW));
        assertEquals(LOW, ColorMath.countColor(-3, 128, FULL, HALF, LOW));
    }

    @Test
    void parseHexAcceptsSixDigitsWithOrWithoutHash() {
        assertEquals(OptionalInt.of(0xB8A6FF), ColorMath.parseHex("#b8a6ff"));
        assertEquals(OptionalInt.of(0xB8A6FF), ColorMath.parseHex("B8A6FF"));
    }

    @Test
    void parseHexRejectsInvalid() {
        assertTrue(ColorMath.parseHex("fff").isEmpty());
        assertTrue(ColorMath.parseHex("zzzzzz").isEmpty());
        assertTrue(ColorMath.parseHex("").isEmpty());
        assertTrue(ColorMath.parseHex(null).isEmpty());
    }

    @Test
    void toHexIsSixUpperCaseDigits() {
        assertEquals("0B0F17", ColorMath.toHex(0x0B0F17));
        assertEquals("B8A6FF", ColorMath.toHex(0xFFB8A6FF));
    }
}
