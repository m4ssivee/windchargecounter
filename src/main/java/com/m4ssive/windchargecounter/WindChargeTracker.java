package com.m4ssive.windchargecounter;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class WindChargeTracker {
    public static final int MAX_WIND_CHARGES = 128;

    // envanter tek tickte bu kadar artarsa ve 128'e ulaşırsa yeni raunt sayılır
    private static final int REFILL_JUMP_THRESHOLD = 16;
    // aynı mermi entity'si render mesafesinden çıkıp tekrar girerse ikinci kez sayılmasın
    private static final int MAX_SEEN_PROJECTILES = 512;

    /** HUD'da gösterim için rakip bilgisi. */
    public record Opponent(UUID uuid, String name, int remaining, long lastUse) {}

    // LinkedHashMap: rakipler ilk görüldükleri sırada listelenir, HUD satırları zıplamaz
    private final Map<UUID, Integer> windChargesUsed = new LinkedHashMap<>();
    private final Map<UUID, String> opponentNames = new HashMap<>();
    private final Map<UUID, Long> lastUseTime = new HashMap<>();
    private final Set<UUID> seenProjectiles = new LinkedHashSet<>();

    private int localPlayerCount = 0;
    private long lastLocalUse = 0;
    private boolean hasLocalSample = false;

    private boolean wasPlayerDead = false;
    private ClientWorld lastWorld = null;

    public void updateLocalPlayerCount(PlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.WIND_CHARGE)) {
                count += stack.getCount();
            }
        }

        // envanter 128'e geri dolduysa yeni raunt başlamış demektir (0'dan dolma dahil)
        if (hasLocalSample
                && count >= MAX_WIND_CHARGES
                && localPlayerCount < MAX_WIND_CHARGES
                && count - localPlayerCount >= REFILL_JUMP_THRESHOLD) {
            WindChargeCounterMod.LOGGER.info("[WCC] Wind charge refill detected ({} -> {}), resetting counters for new round.", localPlayerCount, count);
            clearAll();
        }

        if (hasLocalSample && count < localPlayerCount) {
            lastLocalUse = System.currentTimeMillis();
        }
        localPlayerCount = count;
        hasLocalSample = true;
    }

    /**
     * Dünya değişikliği, ölüm/respawn ve ölen rakipler için reset kontrolü.
     */
    public void checkDuelReset(PlayerEntity localPlayer, ClientWorld world, boolean resetOnDeath) {
        if (localPlayer == null) return;

        // yeni sunucu / yeni dünya / boyut değişimi -> her zaman yeni bir ClientWorld örneği oluşur
        if (world != lastWorld) {
            if (lastWorld != null) {
                WindChargeCounterMod.LOGGER.info("[WCC] World changed, resetting counters.");
            }
            clearAll();
            wasPlayerDead = false;
            lastWorld = world;
        }

        boolean isDead = localPlayer.isDead();
        if (isDead && !wasPlayerDead) {
            wasPlayerDead = true;
        } else if (!isDead && wasPlayerDead) {
            wasPlayerDead = false;
            if (resetOnDeath) {
                clearAll();
                WindChargeCounterMod.LOGGER.info("[WCC] Player respawned, counters reset.");
            }
        }

        // ölen rakiplerin sayacını temizle
        if (resetOnDeath && world != null && !windChargesUsed.isEmpty()) {
            for (AbstractClientPlayerEntity player : world.getPlayers()) {
                if (player != localPlayer && player.isDead()) {
                    clearPlayer(player.getUuid());
                }
            }
        }
    }

    /**
     * Bir rakibin attığı wind charge'ı kaydeder.
     *
     * @param projectileUuid mermi entity'sinin UUID'si; aynı mermi iki kez sayılmaz
     */
    public void recordWindChargeUse(PlayerEntity player, UUID projectileUuid) {
        if (projectileUuid != null) {
            if (!seenProjectiles.add(projectileUuid)) return;
            if (seenProjectiles.size() > MAX_SEEN_PROJECTILES) {
                Iterator<UUID> it = seenProjectiles.iterator();
                it.next();
                it.remove();
            }
        }

        UUID id = player.getUuid();
        int used = windChargesUsed.getOrDefault(id, 0) + 1;
        windChargesUsed.put(id, Math.min(used, MAX_WIND_CHARGES));
        opponentNames.put(id, player.getName().getString());
        lastUseTime.put(id, System.currentTimeMillis());
    }

    public boolean hasWindCharge() {
        return localPlayerCount > 0;
    }

    public boolean hasTrackedOpponents() {
        return !windChargesUsed.isEmpty();
    }

    public int getWindChargesUsed(UUID playerId) {
        return windChargesUsed.getOrDefault(playerId, 0);
    }

    public int getLocalPlayerCharges() {
        return localPlayerCount;
    }

    public int getOpponentRemainingCharges(UUID playerId) {
        return MAX_WIND_CHARGES - getWindChargesUsed(playerId);
    }

    /** Takip edilen rakipler, ilk görülme sırasına göre. Render mesafesinden çıkanlar da dahil. */
    public List<Opponent> getOpponents() {
        List<Opponent> list = new ArrayList<>(windChargesUsed.size());
        for (Map.Entry<UUID, Integer> e : windChargesUsed.entrySet()) {
            UUID id = e.getKey();
            list.add(new Opponent(id, opponentNames.getOrDefault(id, "?"),
                MAX_WIND_CHARGES - e.getValue(), lastUseTime.getOrDefault(id, 0L)));
        }
        return list;
    }

    public long getLastLocalUse() {
        return lastLocalUse;
    }

    public Map<UUID, Integer> getAllOpponentUsage() {
        return new HashMap<>(windChargesUsed);
    }

    public void clearPlayer(UUID playerId) {
        windChargesUsed.remove(playerId);
        opponentNames.remove(playerId);
        lastUseTime.remove(playerId);
    }

    /** Rakip sayaçlarını sıfırlar. Kendi sayımız envanterden okunduğu için dokunulmaz. */
    public void clearAll() {
        windChargesUsed.clear();
        opponentNames.clear();
        lastUseTime.clear();
        seenProjectiles.clear();
    }
}
