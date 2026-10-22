package com.m4ssive.windchargecounter.config;

import com.google.gson.JsonObject;
import com.m4ssive.windchargecounter.theme.Themes;

/** Upgrades config files written by older versions. */
public final class ConfigMigration {
    private ConfigMigration() {}

    public static void migrate(JsonObject raw, ModConfig target) {
        // 1.2.0 stored background opacity as 0-255
        if (raw.has("backgroundOpacity") && !raw.has("panelOpacity")) {
            target.panelOpacity = Math.round(raw.get("backgroundOpacity").getAsInt() / 2.55f);
        }
        // files without theme colors: fill them from the selected (or default) preset
        if (!raw.has("accentColor")) {
            target.applyTheme(Themes.byId(target.themeId));
        }
    }
}
