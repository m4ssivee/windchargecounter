package com.m4ssive.windchargecounter.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigMigrationTest {
    @Test
    void oldFileMigrates() {
        JsonObject raw = JsonParser.parseString("{\"backgroundOpacity\":160,\"scale\":1.5}").getAsJsonObject();
        ModConfig c = new Gson().fromJson(raw, ModConfig.class);
        ConfigMigration.migrate(raw, c);
        assertEquals(63, c.panelOpacity);
        assertEquals(1.5f, c.scale);
        assertEquals(0x8BE9FF, c.accentColor);
    }

    @Test
    void newFileKeepsItsValues() {
        JsonObject raw = JsonParser.parseString(
            "{\"panelOpacity\":40,\"themeId\":\"night\",\"accentColor\":1193046,\"backgroundOpacity\":255}").getAsJsonObject();
        ModConfig c = new Gson().fromJson(raw, ModConfig.class);
        ConfigMigration.migrate(raw, c);
        assertEquals(40, c.panelOpacity);
        assertEquals(1193046, c.accentColor);
    }
}
