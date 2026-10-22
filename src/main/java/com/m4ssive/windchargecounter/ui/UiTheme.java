package com.m4ssive.windchargecounter.ui;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.theme.ColorMath;

/**
 * Editor chrome colors (ARGB). Only the accent follows the HUD theme, and only when it is readable on the
 * dark editor surface; editor text is always light so a dark HUD text color can't hide the editor.
 */
public record UiTheme(int accent, int text, int muted, int surface, int surfaceAlt, int line, int onAccent) {
    private static final int SURFACE_RGB = 0x0D0E22;
    private static final int TEXT = 0xEAF2FF;
    private static final int FALLBACK_ACCENT = 0x8BE9FF;

    public static UiTheme from(ModConfig c) {
        return new UiTheme(
            ColorMath.argb(ColorMath.readableOn(c.accentColor, SURFACE_RGB, FALLBACK_ACCENT), 255),
            ColorMath.argb(TEXT, 255),
            0xFF8FA3C0,
            0xFA0D0E22,
            0xFF15172E,
            0xFF2A2E55,
            0xFF0A1020);
    }

    /** Same RGB with a different alpha. */
    public static int alpha(int argb, int a) {
        return ColorMath.argb(argb, a);
    }
}
