package com.m4ssive.windchargecounter.config;

import com.m4ssive.windchargecounter.theme.Themes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModConfigTest {
    @Test
    void copyIsDeepAndRestorable() {
        ModConfig a = new ModConfig();
        ModConfig snap = a.copy();
        a.applyTheme(Themes.byId("ember"));
        a.scale = 2.0f;
        a.nametagSeparator = "-";
        a.lowWarning = 5;
        a.hudX = 50;
        a.markCustom();
        a.copyFrom(snap);
        assertEquals("wind", a.themeId);
        assertFalse(a.customTheme);
        assertEquals(0x8BE9FF, a.accentColor);
        assertEquals(1.0f, a.scale);
        assertEquals("|", a.nametagSeparator);
        assertEquals(16, a.lowWarning);
        assertEquals(ModConfig.UNSET, a.hudX);
    }

    @Test
    void applyThemeCopiesAllColorsAndClearsCustom() {
        ModConfig c = new ModConfig();
        c.markCustom();
        c.applyTheme(Themes.byId("breeze"));
        assertEquals("breeze", c.themeId);
        assertFalse(c.customTheme);
        assertEquals(0xB8A6FF, c.accentColor);
        assertEquals(0xEAF2FF, c.textColor);
        assertEquals(0x0E0B1E, c.panelColor);
        assertEquals(0x7FF0C8, c.fullColor);
        assertEquals(0xF2C46D, c.halfColor);
        assertEquals(0xFF6A9A, c.lowColor);
    }

    @Test
    void sanitizeClampsAndRepairs() {
        ModConfig a = new ModConfig();
        a.scale = 9f;
        a.panelOpacity = 300;
        a.lowWarning = -1;
        a.nametagSeparator = "?";
        a.themeId = "x";
        a.sanitize();
        assertEquals(3.0f, a.scale);
        assertEquals(100, a.panelOpacity);
        assertEquals(0, a.lowWarning);
        assertEquals("|", a.nametagSeparator);
        assertEquals("wind", a.themeId);
    }
}
