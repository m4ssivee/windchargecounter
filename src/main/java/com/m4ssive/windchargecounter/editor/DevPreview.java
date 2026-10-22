package com.m4ssive.windchargecounter.editor;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.minecraft.client.MinecraftClient;

/**
 * Development-only: with -Dwcc.dev.editorTabs=LAYOUT,THEME,... the editor opens after joining a world
 * (or on the title screen when the list starts with TITLE) and cycles through those tabs. Used to
 * screenshot the UI on a headless build machine. Does nothing when the property is not set.
 */
public final class DevPreview {
    private static final String PROP = "wcc.dev.editorTabs";
    private static final int START_TICK = 100, TICKS_PER_STEP = 80;
    private static int ticks;
    private static int step = -1;

    private DevPreview() {}

    public static void tick(MinecraftClient client) {
        String spec = System.getProperty(PROP);
        if (spec == null) return;
        String[] steps = spec.split(",");
        boolean title = steps[0].trim().equals("TITLE");
        if (!title && client.player == null) return;
        ticks++;
        if (ticks < START_TICK || (ticks - START_TICK) % TICKS_PER_STEP != 0) return;
        step++;
        if (step == 0) {
            client.setScreen(new EditorScreen(client.currentScreen));
            WindChargeCounterMod.LOGGER.info("[WCC-DEV] step 0: editor opened");
            return;
        }
        int idx = step - 1 + (title ? 1 : 0);
        if (idx >= steps.length) return;
        if (client.currentScreen instanceof EditorScreen ed) {
            String name = steps[idx].trim();
            try {
                if (name.equals("NONE")) ed.closeOverlay();
                else ed.openTab(EditorScreen.Tab.valueOf(name));
            } catch (IllegalArgumentException e) {
                WindChargeCounterMod.LOGGER.warn("[WCC-DEV] unknown step {}", name);
            }
            WindChargeCounterMod.LOGGER.info("[WCC-DEV] step {}: {}", step, name);
        }
    }
}
