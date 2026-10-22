package com.m4ssive.windchargecounter.theme;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThemesTest {
    @Test
    void presetsInOrder() {
        assertEquals(List.of("wind", "breeze", "night", "ember", "mono"),
            Themes.ALL.stream().map(Theme::id).toList());
    }

    @Test
    void breezeValuesMatchSpec() {
        Theme b = Themes.byId("breeze");
        assertEquals(0xB8A6FF, b.accent());
        assertEquals(0x7FF0C8, b.full());
        assertEquals(0xF2C46D, b.half());
        assertEquals(0xFF6A9A, b.low());
    }

    @Test
    void unknownIdFallsBackToWind() {
        assertEquals("wind", Themes.byId("nope").id());
        assertEquals("wind", Themes.byId(null).id());
    }
}
