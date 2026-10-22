package com.m4ssive.windchargecounter.ui;

import com.m4ssive.windchargecounter.config.ModConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiThemeTest {
    @Test
    void editorTextStaysLightWhenHudTextIsDark() {
        ModConfig c = new ModConfig();
        c.textColor = 0x000000;
        c.accentColor = 0x000000;
        UiTheme t = UiTheme.from(c);
        assertEquals(0xFFEAF2FF, t.text());
        assertEquals(0xFF8BE9FF, t.accent());
    }

    @Test
    void readableAccentIsKept() {
        ModConfig c = new ModConfig();
        c.accentColor = 0xB8A6FF;
        assertEquals(0xFFB8A6FF, UiTheme.from(c).accent());
    }
}
