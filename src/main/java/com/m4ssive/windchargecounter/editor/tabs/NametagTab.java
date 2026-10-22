package com.m4ssive.windchargecounter.editor.tabs;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.editor.EditorScreen;
import com.m4ssive.windchargecounter.editor.Popover;
import com.m4ssive.windchargecounter.ui.Label;
import com.m4ssive.windchargecounter.ui.Segmented;
import com.m4ssive.windchargecounter.ui.Slider;
import com.m4ssive.windchargecounter.ui.Toggle;

import java.util.List;

/** Nametag suffix options (spec §7.3 Nametag). */
public final class NametagTab {
    private static final List<String> SEP_VALUES = List.of("|", "-", "[]", "");
    private static final List<String> SEP_LABELS = List.of("|", "-", "[ ]", "none");

    private NametagTab() {}

    public static Popover create(ModConfig cfg, EditorScreen screen) {
        Slider low = new Slider(0, "Low warning at", v -> v < 0.5f ? "Off" : String.valueOf(Math.round(v)),
            0f, 64f, 1f, () -> (float) cfg.lowWarning, v -> cfg.lowWarning = Math.round(v), 0xFFFF5555);
        Segmented sep = new Segmented(0, SEP_LABELS,
            () -> Math.max(0, SEP_VALUES.indexOf(cfg.nametagSeparator)),
            i -> cfg.nametagSeparator = SEP_VALUES.get(i));
        return new Popover("Nametag", List.of(
            new Toggle(0, "Show on nametags", () -> cfg.showInNametags, v -> cfg.showInNametags = v),
            new Toggle(0, "Wind icon", () -> cfg.nametagIcon, v -> cfg.nametagIcon = v),
            new Toggle(0, "Show /128", () -> cfg.nametagShowMax, v -> cfg.nametagShowMax = v),
            new Toggle(0, "Color by charges", () -> cfg.colorByCharges, v -> cfg.colorByCharges = v),
            new Toggle(0, "On my own nametag", () -> cfg.nametagOwn, v -> cfg.nametagOwn = v),
            low,
            new Label("SEPARATOR"),
            sep), LayoutTab.WIDTH, null);
    }
}
