package com.m4ssive.windchargecounter;

import com.m4ssive.m4lib.M4Lib;
import com.m4ssive.m4lib.NametagRenderer;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.editor.EditorScreen;
import com.m4ssive.windchargecounter.gui.WindChargeHud;
import com.m4ssive.windchargecounter.nametag.NametagSuffix;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindChargeCounterMod implements ClientModInitializer {
    public static final String MOD_ID = "windchargecounter";
    public static final String MOD_NAME = "WindChargeCounter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static WindChargeCounterMod instance;
    private static KeyBinding toggleEditModeKey;

    private ModConfig config;
    private WindChargeTracker tracker;
    private WindChargeHud hud;

    private boolean m4LibRegistered = false;

    @Override
    public void onInitializeClient() {
        instance = this;

        config = new ModConfig();
        config.load();

        tracker = new WindChargeTracker();
        hud = new WindChargeHud(tracker, config);

        //? if >=1.21.9 {
        /*toggleEditModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.windchargecounter.toggleedit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
            KeyBinding.Category.create(net.minecraft.util.Identifier.of(MOD_ID, "main"))));
        *///?} else {
        toggleEditModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.windchargecounter.toggleedit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.windchargecounter"));
        //?}

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("wcc")
                .executes(context -> {
                    // open on the next tick, after the chat screen has closed
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.send(() -> mc.setScreen(new EditorScreen(null)));
                    return 1;
                })
                .then(ClientCommandManager.literal("version").executes(context -> {
                    String v = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(MOD_ID)
                        .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
                    context.getSource().sendFeedback(Text.literal("[WCC] WindChargeCounter v" + v).formatted(Formatting.AQUA));
                    return 1;
                }))
                .then(ClientCommandManager.literal("reset").executes(context -> {
                    tracker.clearAll();
                    context.getSource().sendFeedback(Text.literal("[WCC] Counters reset for the new round!").formatted(Formatting.GREEN));
                    return 1;
                }))
            )
        );

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen == null && !client.options.hudHidden) {
                hud.render(context, false);
            }
        });

        // m4lib may initialize after us; retry every tick until it is ready
        tryRegisterNametagSuffix();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!m4LibRegistered) tryRegisterNametagSuffix();
            com.m4ssive.windchargecounter.editor.DevPreview.tick(client);

            if (client.player != null) {
                tracker.checkDuelReset(client.player, client.world, config.autoResetOnDeath);
                tracker.updateLocalPlayerCount(client.player);
            }

            while (toggleEditModeKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new EditorScreen(null));
                }
            }
        });

        LOGGER.info("WindChargeCounter loaded!");
    }

    private void tryRegisterNametagSuffix() {
        try {
            M4Lib lib = M4Lib.getInstance();
            if (lib == null) return;
            NametagRenderer renderer = lib.getNametagRenderer();
            if (renderer == null) return;

            renderer.registerModSuffix(MOD_ID, this::buildNametagSuffix);
            m4LibRegistered = true;
            LOGGER.info("[WCC] Nametag suffix registered with m4lib");
        } catch (Throwable t) {
            m4LibRegistered = true; // tekrar tekrar log basmasın
            LOGGER.error("[WCC] Failed to register nametag suffix with m4lib", t);
        }
    }

    private Text buildNametagSuffix(PlayerEntity player) {
        try {
            if (player == null || !config.showInNametags || !player.isAlive()) return null;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return null;

            boolean isSelf = player.getUuid().equals(client.player.getUuid());
            if (isSelf && !config.nametagOwn) return null;
            int remaining = isSelf
                ? tracker.getLocalPlayerCharges()
                : tracker.getOpponentRemainingCharges(player.getUuid());
            if (isSelf && remaining <= 0) return null;

            return NametagSuffix.build(remaining, config);
        } catch (Exception e) {
            LOGGER.error("[WCC] Error creating nametag suffix", e);
            return null;
        }
    }

    public static KeyBinding getToggleEditModeKey() { return toggleEditModeKey; }
    public static WindChargeCounterMod getInstance() { return instance; }
    public ModConfig getConfig() { return config; }
    public WindChargeTracker getTracker() { return tracker; }
    public WindChargeHud getHud() { return hud; }
}
