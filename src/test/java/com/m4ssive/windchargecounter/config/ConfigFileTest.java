package com.m4ssive.windchargecounter.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFileTest {
    @Test
    void unreadableFileIsBackedUpBeforeDefaultsAreUsed(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("windchargecounter.json");
        Files.writeString(file, "{\"scale\":\"x\",\"hudX\":40}");
        ModConfig c = new ModConfig();
        c.loadFrom(file);
        assertEquals(1.0f, c.scale);
        Path bak = dir.resolve("windchargecounter.json.bak");
        assertTrue(Files.exists(bak), "backup missing");
        assertEquals("{\"scale\":\"x\",\"hudX\":40}", Files.readString(bak));
    }

    @Test
    void goodFileLoadsAndNoBackupIsMade(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("windchargecounter.json");
        Files.writeString(file, "{\"scale\":2.0,\"themeId\":\"ember\",\"accentColor\":16751178}");
        ModConfig c = new ModConfig();
        c.loadFrom(file);
        assertEquals(2.0f, c.scale);
        assertEquals("ember", c.themeId);
        assertFalse(Files.exists(dir.resolve("windchargecounter.json.bak")));
    }

    @Test
    void saveToWritesAReadableFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("sub/windchargecounter.json");
        ModConfig c = new ModConfig();
        c.scale = 1.7f;
        c.saveTo(file);
        ModConfig d = new ModConfig();
        d.loadFrom(file);
        assertEquals(1.7f, d.scale);
    }
}
