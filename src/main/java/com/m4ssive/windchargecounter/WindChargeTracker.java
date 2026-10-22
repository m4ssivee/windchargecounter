package com.m4ssive.windchargecounter;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class WindChargeTracker {
    public static final int MAX_WIND_CHARGES = 128;

    // a jump this big in one tick that reaches 128 means a new round (kit refill)
    private static final int REFILL_JUMP_THRESHOLD = 16;
    // a projectile that leaves render distance and comes back must not be counted twice
    private static final int MAX_SEEN_PROJECTILES = 512;

    /** Opponent data shown on the HUD. */
    public record Opponent(UUID uuid, String name, int remaining, long lastUse) {}

    // LinkedHashMap: opponents stay in first-seen order so HUD rows don't jump around
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

        // inventory refilled to 128 → a new round started (also when refilling from 0)
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
     * Resets on world change, on death/respawn, and for opponents that died.
     */
    public void checkDuelReset(PlayerEntity localPlayer, ClientWorld world, boolean resetOnDeath) {
        if (localPlayer == null) return;

        // new server / new world / dimension change always creates a new ClientWorld instance
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

        // clear the counters of opponents that died
        if (resetOnDeath && world != null && !windChargesUsed.isEmpty()) {
            for (AbstractClientPlayerEntity player : world.getPlayers()) {
                if (player != localPlayer && player.isDead()) {
                    clearPlayer(player.getUuid());
                }
            }
        }
    }

    /**
     * Records a wind charge thrown by an opponent.
     *
     * @param projectileUuid UUID of the projectile entity; the same projectile is never counted twice
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

    /** Tracked opponents in first-seen order, including those out of render distance. */
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

    /** Resets opponent counters. Our own count is read from the inventory, so it is not touched. */
    public void clearAll() {
        windChargesUsed.clear();
        opponentNames.clear();
        lastUseTime.clear();
        seenProjectiles.clear();
    }
}
