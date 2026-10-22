package com.m4ssive.windchargecounter.theme;

import java.util.List;

/** Built-in theme presets (spec §8). */
public final class Themes {
    public static final String DEFAULT_ID = "wind";

    public static final List<Theme> ALL = List.of(
        new Theme("wind", "Wind", 0x8BE9FF, 0xEAF2FF, 0x0B0F17, 0x5FE36A, 0xD9E04A, 0xFF6A4A),
        new Theme("breeze", "Breeze", 0xB8A6FF, 0xEAF2FF, 0x0E0B1E, 0x7FF0C8, 0xF2C46D, 0xFF6A9A),
        new Theme("night", "Night", 0x5B8CFF, 0xEAF2FF, 0x0A0F24, 0x8BE9FF, 0xFFD25A, 0xFF6A9A),
        new Theme("ember", "Ember", 0xFF9A4A, 0xFFF1E6, 0x1A0F0A, 0xFFE08A, 0xFFB86A, 0xE5484D),
        new Theme("mono", "Mono", 0xD6DCE6, 0xEAF2FF, 0x101216, 0xEAEFF6, 0xB4BCC8, 0xFF6A6A)
    );

    private Themes() {}

    /** The preset with this id, or Wind when unknown. */
    public static Theme byId(String id) {
        for (Theme t : ALL) if (t.id().equals(id)) return t;
        return ALL.get(0);
    }
}
