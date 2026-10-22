package com.m4ssive.windchargecounter;

import com.m4ssive.windchargecounter.editor.EditorScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    /** The editor edits the HUD in place, so it only opens inside a world. */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) return new EditorScreen(parent);
            return new NoticeScreen(() -> client.setScreen(parent),
                Text.literal("WindChargeCounter"),
                Text.literal("The editor opens in game. Join a world, then press K or type /wcc."));
        };
    }
}
