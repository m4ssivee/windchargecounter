package com.m4ssive.windchargecounter;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.gui.EditModeScreen;
import com.m4ssive.windchargecounter.gui.WindChargeHud;
import com.m4ssive.m4lib.M4Lib;
import com.m4ssive.m4lib.NametagRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindChargeCounterMod implements ClientModInitializer {
    public static final String MOD_ID = "windchargecounter";
    public static final String MOD_NAME = "WindChargeCounter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static WindChargeCounterMod instance;
    private ModConfig config;
    private WindChargeTracker tracker;
    private WindChargeHud hud;

    private static KeyBinding toggleEditModeKey;
    private boolean m4LibRegistered = false;
    private boolean m4LibRetryRegistered = false;

    @Override
    public void onInitializeClient() {
        instance = this;

        config = new ModConfig();
        config.load();

        tracker = new WindChargeTracker();
        hud = new WindChargeHud(tracker, config);

        registerNametagSuffix();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("wcc")
                .then(ClientCommandManager.literal("reset").executes(context -> {
                    if (tracker != null) {
                        tracker.clearAll();
                        context.getSource().sendFeedback(Text.literal("\u00a7a[WCC] Counters reset for the new round!"));
                    }
                    return 1;
                }))
            );
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.currentScreen == null) {
                hud.render(context, tickCounter.getTickDelta(false));
            }
        });

        registerKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                tracker.updateLocalPlayerCount(client.player);
                tracker.checkDuelReset(client.player, client.world, config.autoResetOnDeath);
                tracker.tick(client.world);
            }

            if (config.hudX == -9999 || config.hudY == -9999) {
                if (client.getWindow() != null && client.getWindow().getScaledWidth() > 0) {
                    config.hudX = client.getWindow().getScaledWidth() - 160;
                    config.hudY = 10;
                    config.save();
                }
            }

            if (toggleEditModeKey != null && toggleEditModeKey.wasPressed()) {
                // Use hud.isEditMode() as single source of truth
                if (!hud.isEditMode()) {
                    MinecraftClient.getInstance().setScreen(new EditModeScreen());
                } else {
                    hud.setEditMode(false);
                    if (MinecraftClient.getInstance().currentScreen instanceof EditModeScreen) {
                        MinecraftClient.getInstance().setScreen(null);
                    }
                    config.save();
                }
            }
        });

        LOGGER.info("WindChargeCounter loaded!");
    }

    private void registerNametagSuffix() {
        try {
            LOGGER.info("[WindChargeCounter] Registering nametag suffix provider with m4lib...");
            
            M4Lib m4Lib = M4Lib.getInstance();
            if (m4Lib == null) {
                if (!m4LibRetryRegistered) {
                    m4LibRetryRegistered = true;
                    ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (!m4LibRegistered) {
                            try {
                                M4Lib lib = M4Lib.getInstance();
                                if (lib != null) {
                                    NametagRenderer renderer = lib.getNametagRenderer();
                                    if (renderer != null) {
                                        doRegisterNametagSuffix(renderer);
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    });
                }
                return;
            }
            
            NametagRenderer nametagRenderer = m4Lib.getNametagRenderer();
            if (nametagRenderer == null) {
                if (!m4LibRetryRegistered) {
                    m4LibRetryRegistered = true;
                    ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (!m4LibRegistered) {
                            try {
                                M4Lib lib = M4Lib.getInstance();
                                if (lib != null) {
                                    NametagRenderer renderer = lib.getNametagRenderer();
                                    if (renderer != null) {
                                        doRegisterNametagSuffix(renderer);
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    });
                }
                return;
            }
            
            doRegisterNametagSuffix(nametagRenderer);
        } catch (Exception e) {
            LOGGER.error("Error registering nametag suffix", e);
        }
    }

    private void doRegisterNametagSuffix(NametagRenderer nametagRenderer) {
        if (m4LibRegistered || nametagRenderer == null) return;

        try {
            nametagRenderer.registerModSuffix("windchargecounter", (player) -> {
                try {
                    if (player == null || !player.isAlive()) return null;
                    if (!config.showInNametags) return null;

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client == null || client.player == null) return null;

                    boolean isSelf = player.getUuid().equals(client.player.getUuid());

                    // Kendi nametag'imizde gösterme (showSelf false ise)
                    if (isSelf && !config.showSelf) return null;

                    int remaining;
                    if (isSelf) {
                        remaining = tracker.getLocalPlayerCharges();
                        // Envanterde wind charge yoksa kendi nametag'ımızda gösterme
                        if (remaining <= 0) return null;
                    } else {
                        // Rakip hiç wind charge kullanmadıysa gösterme
                        if (!tracker.hasOpponentUsed(player.getUuid())) return null;
                        remaining = tracker.getOpponentRemainingCharges(player.getUuid());
                    }

                    MutableText suffix = Text.literal("");
                    suffix.append(Text.literal(" | ").styled(s -> s.withColor(net.minecraft.util.Formatting.GRAY)));
                    suffix.append(Text.literal("\u2B21").styled(s -> s.withColor(net.minecraft.util.Formatting.AQUA)));
                    
                    int color = WindChargeHud.getChargeColor(remaining);
                    String maxDisplay = isSelf ? "" : "/" + WindChargeTracker.MAX_WIND_CHARGES;
                    String countStr = remaining + maxDisplay;
                    suffix.append(Text.literal(countStr).styled(s -> s.withColor(TextColor.fromRgb(color & 0x00FFFFFF))));
                    return suffix;
                } catch (Exception e) {
                    LOGGER.error("[WindChargeCounter] Error rendering nametag suffix", e);
                    return null;
                }
            });
            m4LibRegistered = true;
            LOGGER.info("[WindChargeCounter] Nametag suffix registered with m4lib");
        } catch (Exception e) {
            LOGGER.error("Failed to register nametag suffix", e);
        }
    }

    private void registerKeybinds() {
        try {
            toggleEditModeKey = new KeyBinding("key.windchargecounter.toggleedit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.windchargecounter");
            KeyBindingHelper.registerKeyBinding(toggleEditModeKey);
        } catch (Throwable t) {
            try {
                for (java.lang.reflect.Constructor<?> constructor : KeyBinding.class.getConstructors()) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    if (paramTypes.length == 3 && paramTypes[0] == String.class && paramTypes[2] == String.class) {
                        toggleEditModeKey = (KeyBinding) constructor.newInstance("key.windchargecounter.toggleedit", GLFW.GLFW_KEY_K, "category.windchargecounter");
                        KeyBindingHelper.registerKeyBinding(toggleEditModeKey);
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to register keybinds", e);
            }
        }
    }


    public static WindChargeCounterMod getInstance() { return instance; }
    public ModConfig getConfig() { return config; }
    public WindChargeTracker getTracker() { return tracker; }
    public WindChargeHud getHud() { return hud; }
}
