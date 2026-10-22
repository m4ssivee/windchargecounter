package com.m4ssive.windchargecounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("windchargecounter.json");

    /** HUD konumu henüz ayarlanmadı (ilk açılışta sağ üste yerleşir). */
    public static final int UNSET = -9999;

    // konum & boyut
    public int hudX = UNSET;
    public int hudY = UNSET;
    public float scale = 1.0f;

    // görünüm
    public boolean enableHud = true;
    public boolean showSelf = true;
    public boolean showHeader = true;
    public boolean showBars = true;
    public boolean flashOnThrow = true;
    public int backgroundOpacity = 160; // 0-255

    // davranış
    public boolean showInNametags = true;
    public boolean autoResetOnDeath = true;

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            // eski config'teki bilinmeyen alanlar yok sayılır, eksik alanlar varsayılanda kalır
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) copyFrom(loaded);
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("Config load error, using defaults", e);
        }
        sanitize();
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("Config save error", e);
        }
    }

    private void sanitize() {
        if (Float.isNaN(scale)) scale = 1.0f;
        scale = Math.max(0.5f, Math.min(3.0f, scale));
        backgroundOpacity = Math.max(0, Math.min(255, backgroundOpacity));
    }

    private void copyFrom(ModConfig o) {
        hudX = o.hudX;
        hudY = o.hudY;
        scale = o.scale;
        enableHud = o.enableHud;
        showSelf = o.showSelf;
        showHeader = o.showHeader;
        showBars = o.showBars;
        flashOnThrow = o.flashOnThrow;
        backgroundOpacity = o.backgroundOpacity;
        showInNametags = o.showInNametags;
        autoResetOnDeath = o.autoResetOnDeath;
    }
}
