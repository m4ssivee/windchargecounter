package com.m4ssive.windchargecounter.editor.tabs;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.editor.EditorScreen;
import com.m4ssive.windchargecounter.editor.Popover;
import com.m4ssive.windchargecounter.ui.Button;
import com.m4ssive.windchargecounter.ui.Toggle;

import java.util.List;

/** HUD content toggles and the counter reset (spec §7.3 Counter). */
public final class CounterTab {
    private CounterTab() {}

    public static Popover create(ModConfig cfg, EditorScreen screen) {
        String[] resetLabel = {"Reset counters"};
        Button reset = new Button(0, 16, () -> resetLabel[0], false, () -> {
            WindChargeCounterMod.getInstance().getTracker().clearAll();
            resetLabel[0] = "Counters reset";
        });
        return new Popover("Counter", List.of(
            new Toggle(0, "Show HUD", () -> cfg.enableHud, v -> cfg.enableHud = v),
            new Toggle(0, "Header", () -> cfg.showHeader, v -> cfg.showHeader = v),
            new Toggle(0, "Progress bars", () -> cfg.showBars, v -> cfg.showBars = v),
            new Toggle(0, "Flash on throw", () -> cfg.flashOnThrow, v -> cfg.flashOnThrow = v),
            new Toggle(0, "Show yourself", () -> cfg.showSelf, v -> cfg.showSelf = v),
            new Toggle(0, "Reset on death", () -> cfg.autoResetOnDeath, v -> cfg.autoResetOnDeath = v),
            reset), LayoutTab.WIDTH, null);
    }
}
