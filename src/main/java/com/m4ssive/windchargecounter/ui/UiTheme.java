package com.m4ssive.windchargecounter.ui;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.theme.ColorMath;

/** Editor chrome colors (ARGB). The accent and text follow the selected HUD theme. */
public record UiTheme(int accent, int text, int muted, int surface, int surfaceAlt, int line, int onAccent) {
    public static UiTheme from(ModConfig c) {
        return new UiTheme(
            ColorMath.argb(c.accentColor, 255),
            ColorMath.argb(c.textColor, 255),
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
