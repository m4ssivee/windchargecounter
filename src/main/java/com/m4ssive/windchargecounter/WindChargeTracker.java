package com.m4ssive.windchargecounter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;

import java.util.*;

public class WindChargeTracker {
    // duel arası reset için
    private boolean wasPlayerDead = false;
    private UUID lastWorldId = null;
    public static final int MAX_WIND_CHARGES = 128;

    private final Map<UUID, Integer> windChargesUsed = new HashMap<>();
    private final Map<UUID, Long> lastWindChargeTime = new HashMap<>();
    private int localPlayerCount = 0;
    private int previousLocalPlayerCount = 0;
    private static final int REFILL_JUMP_THRESHOLD = 20; // artık kullanılmıyor ama bıraktım

    public void updateLocalPlayerCount(PlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.WIND_CHARGE)) {
                count += stack.getCount();
            }
        }

        // envanter 128'e geri dolduysa yeni raunt başlamış demektir
        if (previousLocalPlayerCount > 0 && previousLocalPlayerCount < MAX_WIND_CHARGES && count == MAX_WIND_CHARGES) {
            WindChargeCounterMod.LOGGER.info("[WCC] Wind charge refill detected ({} -> {}), auto-resetting counters for new round!", previousLocalPlayerCount, count);
            clearAll();
        }

        previousLocalPlayerCount = localPlayerCount;
        localPlayerCount = count;
    }

    /**
     * ölüm/respawn ve dünya değişikliği kontrolü
     */
    public void checkDuelReset(PlayerEntity localPlayer, ClientWorld world, boolean enabled) {
        if (localPlayer == null) return;

        // dünya değiştiyse her türlü sıfırla
        UUID currentWorldId = null;
        if (world != null) {
            try {
                currentWorldId = UUID.nameUUIDFromBytes(
                    world.getRegistryKey().getValue().toString().getBytes());
            } catch (Exception e) {
                // olmazsa hashCode ile idare et
                currentWorldId = new UUID(0, world.hashCode());
            }
        }
        if (lastWorldId != null && currentWorldId != null && !lastWorldId.equals(currentWorldId)) {
            WindChargeCounterMod.LOGGER.info("[WCC] World changed, resetting counters.");
            clearAll();
            wasPlayerDead = false;
        }
        lastWorldId = currentWorldId;

        // oyuncu öldüyse respawn'da sıfırla
        boolean isDead = localPlayer.isDead() || localPlayer.getHealth() <= 0;

        if (isDead && !wasPlayerDead) {
            wasPlayerDead = true;
            if (enabled) {
                WindChargeCounterMod.LOGGER.info("[WCC] Player died, counters will reset on respawn.");
            }
        } else if (!isDead && wasPlayerDead) {
            wasPlayerDead = false;
            if (enabled) {
                clearAll();
                WindChargeCounterMod.LOGGER.info("[WCC] Player respawned, counters reset for new duel!");
            }
        }
        
        // rakipler öldüyse onların sayacını da temizle
        if (enabled && world != null) {
            for (net.minecraft.client.network.AbstractClientPlayerEntity player : world.getPlayers()) {
                if (player != localPlayer && (player.isDead() || player.getHealth() <= 0)) {
                    clearPlayer(player.getUuid());
                }
            }
        }
    }

    // tick metodu tamamen silindi çünkü Mixin üzerinden çok daha tutarlı takip ediyoruz ve burada çifte sayma (çakışma) oluşuyordu.

    public void recordWindChargeUse(PlayerEntity player) {
        UUID id = player.getUuid();
        
        long currentTime = System.currentTimeMillis();
        if (lastWindChargeTime.containsKey(id) && (currentTime - lastWindChargeTime.get(id)) < 500) {
            // Anti-double count: prevent multiple usages from the same player within 500ms
            return;
        }
        lastWindChargeTime.put(id, currentTime);

        int used = windChargesUsed.getOrDefault(id, 0) + 1;
        windChargesUsed.put(id, Math.min(used, MAX_WIND_CHARGES));
    }

    public boolean hasWindCharge() {
        return localPlayerCount > 0;
    }

    public boolean hasOpponentUsed(UUID playerId) {
        return windChargesUsed.containsKey(playerId) && windChargesUsed.get(playerId) > 0;
    }

    public int getWindChargesUsed(UUID playerId) {
        return windChargesUsed.getOrDefault(playerId, 0);
    }

    public int getLocalPlayerCharges() {
        return localPlayerCount;
    }

    public int getOpponentRemainingCharges(UUID playerId) {
        int used = windChargesUsed.getOrDefault(playerId, 0);
        return MAX_WIND_CHARGES - used;
    }

    public Map<UUID, Integer> getAllOpponentUsage() {
        return new HashMap<>(windChargesUsed);
    }

    public void clearPlayer(UUID playerId) {
        windChargesUsed.remove(playerId);
    }

    public void clearAll() {
        windChargesUsed.clear();
        lastWindChargeTime.clear();
        localPlayerCount = 0;
    }
}
