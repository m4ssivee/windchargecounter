package com.m4ssive.windchargecounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.theme.Theme;
import com.m4ssive.windchargecounter.theme.Themes;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> SEPARATORS = Set.of("|", "-", "[]", "");

    /** HUD position not set yet (placed top-right on first render). */
    public static final int UNSET = -9999;

    // position & size
    public int hudX = UNSET;
    public int hudY = UNSET;
    public float scale = 1.0f;
    public boolean snapToEdges = true;

    // HUD content
    public boolean enableHud = true;
    public boolean showSelf = true;
    public boolean showHeader = true;
    public boolean showBars = true;
    public boolean flashOnThrow = true;
    public boolean autoResetOnDeath = true;

    // theme (RGB, no alpha)
    public String themeId = Themes.DEFAULT_ID;
    public boolean customTheme = false;
    public int accentColor;
    public int textColor;
    public int panelColor;
    public int fullColor;
    public int halfColor;
    public int lowColor;
    public boolean colorByCharges = true;
    public int panelOpacity = 63; // percent

    // nametag
    public boolean showInNametags = true;
    public boolean nametagIcon = true;
    public boolean nametagShowMax = false;
    public boolean nametagOwn = true;
    public int lowWarning = 16;
    public String nametagSeparator = "|";

    public ModConfig() {
        applyTheme(Themes.byId(Themes.DEFAULT_ID));
    }

    /** Copies the preset's colors and selects it. */
    public void applyTheme(Theme t) {
        themeId = t.id();
        customTheme = false;
        accentColor = t.accent();
        textColor = t.text();
        panelColor = t.panel();
        fullColor = t.full();
        halfColor = t.half();
        lowColor = t.low();
    }

    /** Called after any single color was edited by hand. */
    public void markCustom() {
        customTheme = true;
    }

    public ModConfig copy() {
        ModConfig c = new ModConfig();
        c.copyFrom(this);
        return c;
    }

    public void copyFrom(ModConfig o) {
        hudX = o.hudX;
        hudY = o.hudY;
        scale = o.scale;
        snapToEdges = o.snapToEdges;
        enableHud = o.enableHud;
        showSelf = o.showSelf;
        showHeader = o.showHeader;
        showBars = o.showBars;
        flashOnThrow = o.flashOnThrow;
        autoResetOnDeath = o.autoResetOnDeath;
        themeId = o.themeId;
        customTheme = o.customTheme;
        accentColor = o.accentColor;
        textColor = o.textColor;
        panelColor = o.panelColor;
        fullColor = o.fullColor;
        halfColor = o.halfColor;
        lowColor = o.lowColor;
        colorByCharges = o.colorByCharges;
        panelOpacity = o.panelOpacity;
        showInNametags = o.showInNametags;
        nametagIcon = o.nametagIcon;
        nametagShowMax = o.nametagShowMax;
        nametagOwn = o.nametagOwn;
        lowWarning = o.lowWarning;
        nametagSeparator = o.nametagSeparator;
    }

    public void sanitize() {
        if (Float.isNaN(scale)) scale = 1.0f;
        scale = Math.round(Math.max(0.5f, Math.min(3.0f, scale)) * 10f) / 10f;
        panelOpacity = Math.max(0, Math.min(100, panelOpacity));
        lowWarning = Math.max(0, Math.min(64, lowWarning));
        if (nametagSeparator == null || !SEPARATORS.contains(nametagSeparator)) nametagSeparator = "|";
        if (themeId == null || !Themes.byId(themeId).id().equals(themeId)) themeId = Themes.DEFAULT_ID;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("windchargecounter.json");
    }

    public void load() {
        Path path = path();
        if (!Files.exists(path)) {
            save();
            return;
        }
        loadFrom(path);
    }

    /** Loads a config file. An unreadable file is copied to *.bak and defaults are kept. */
    public void loadFrom(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject raw = JsonParser.parseReader(reader).getAsJsonObject();
            ModConfig loaded = GSON.fromJson(raw, ModConfig.class);
            if (loaded != null) {
                ConfigMigration.migrate(raw, loaded);
                copyFrom(loaded);
            }
        } catch (Exception e) {
            log("Config file is unreadable, using defaults (old file kept as .bak)", e);
            try {
                Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception copyError) {
                log("Could not back up the unreadable config", copyError);
            }
        }
        sanitize();
    }

    public void save() {
        saveTo(path());
    }

    public void saveTo(Path path) {
        sanitize();
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            log("Config save error", e);
        }
    }

    // the mod's logger is not initialised in unit tests
    private static void log(String msg, Exception e) {
        try {
            WindChargeCounterMod.LOGGER.error(msg, e);
        } catch (Throwable ignored) {
            System.err.println("[WCC] " + msg + ": " + e);
        }
    }
}
