package com.m4ssive.windchargecounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("windchargecounter.json");

    public int hudX = -9999;
    public int hudY = -9999;
    public float scale = 1.0f;
    public boolean enableHud = true;
    public boolean showSelf = true;
    public boolean showInNametags = true;
    public boolean autoResetOnDeath = true;
    public int textColor = 0xFFFFFFFF;
    public int countHighColor = 0xFF55DDFF;
    public int countMidColor = 0xFFFFFF55;
    public int countLowColor = 0xFFFF5555;
    public int backgroundColor = 0x60001020;

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                copyFrom(loaded);
            }
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("Config load error", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("Config save error", e);
        }
    }

    private void copyFrom(ModConfig other) {
        this.hudX = other.hudX;
        this.hudY = other.hudY;
        this.scale = other.scale;
        this.enableHud = other.enableHud;
        this.showSelf = other.showSelf;
        this.showInNametags = other.showInNametags;
        this.autoResetOnDeath = other.autoResetOnDeath;
        this.textColor = other.textColor;
        this.countHighColor = other.countHighColor;
        this.countMidColor = other.countMidColor;
        this.countLowColor = other.countLowColor;
        this.backgroundColor = other.backgroundColor;
    }
}
