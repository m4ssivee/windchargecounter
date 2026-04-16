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
    // Duel reset state tracking
    private boolean wasPlayerDead = false;
    private UUID lastWorldId = null;
    public static final int MAX_WIND_CHARGES = 128;

    private final Map<UUID, Integer> windChargesUsed = new HashMap<>();
    private final Set<Integer> processedEntities = new HashSet<>();
    private int localPlayerCount = 0;
    private int previousLocalPlayerCount = 0;
    private static final int REFILL_JUMP_THRESHOLD = 20; // If count jumps by 20+ in one tick, it's a new round

    public void updateLocalPlayerCount(PlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.WIND_CHARGE)) {
                count += stack.getCount();
            }
        }

        // Detect round reset: if wind charges reach max (128) from any lower count,
        // the server refilled inventory for a new round
        if (previousLocalPlayerCount > 0 && previousLocalPlayerCount < MAX_WIND_CHARGES && count == MAX_WIND_CHARGES) {
            WindChargeCounterMod.LOGGER.info("[WCC] Wind charge refill detected ({} -> {}), auto-resetting counters for new round!", previousLocalPlayerCount, count);
            clearAll();
        }

        previousLocalPlayerCount = localPlayerCount;
        localPlayerCount = count;
    }

    /**
     * Check for death/respawn to auto-reset counters after a duel ends.
     * @param enabled whether auto-reset is enabled in config
     */
    public void checkDuelReset(PlayerEntity localPlayer, ClientWorld world, boolean enabled) {
        if (localPlayer == null) return;

        // Detect world/server change → reset (always, regardless of toggle)
        UUID currentWorldId = null;
        if (world != null) {
            try {
                currentWorldId = UUID.nameUUIDFromBytes(
                    world.getRegistryKey().getValue().toString().getBytes());
            } catch (Exception e) {
                // fallback: use hashCode
                currentWorldId = new UUID(0, world.hashCode());
            }
        }
        if (lastWorldId != null && currentWorldId != null && !lastWorldId.equals(currentWorldId)) {
            WindChargeCounterMod.LOGGER.info("[WCC] World changed, resetting counters.");
            clearAll();
            wasPlayerDead = false;
        }
        lastWorldId = currentWorldId;

        // Detect death → respawn cycle for local player (only reset if enabled)
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
        
        // Also clear individual opponent counters if they die
        if (enabled && world != null) {
            for (net.minecraft.client.network.AbstractClientPlayerEntity player : world.getPlayers()) {
                if (player != localPlayer && (player.isDead() || player.getHealth() <= 0)) {
                    clearPlayer(player.getUuid());
                }
            }
        }
    }

    public void tick(ClientWorld world) {
        if (world == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        UUID localUuid = client.player.getUuid();

        Iterable<Entity> entities = world.getEntities();
        for (Entity entity : entities) {
            if (entity.getType() == net.minecraft.entity.EntityType.WIND_CHARGE || entity.getType() == net.minecraft.entity.EntityType.BREEZE_WIND_CHARGE) {
                int entityId = entity.getId();
                if (!processedEntities.contains(entityId)) {
                    processedEntities.add(entityId);

                    try {
                        Entity owner = null;
                        if (entity instanceof ProjectileEntity projectile) {
                            owner = projectile.getOwner();
                        }

                        // If owner is null (PvP servers hide this), find closest player that is NOT us
                        if (owner == null) {
                            double closestDist = Double.MAX_VALUE;
                            PlayerEntity closestPlayer = null;
                            for (PlayerEntity p : world.getPlayers()) {
                                if (p.getUuid().equals(localUuid)) continue; // SKIP SELF
                                double dist = p.squaredDistanceTo(entity);
                                if (dist < closestDist) {
                                    closestDist = dist;
                                    closestPlayer = p;
                                }
                            }
                            owner = closestPlayer;
                        }

                        // Record usage only for opponents
                        if (owner instanceof PlayerEntity player && !player.getUuid().equals(localUuid)) {
                            recordWindChargeUse(player);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        if (processedEntities.size() > 1000) {
            processedEntities.clear();
        }
    }

    public void recordWindChargeUse(PlayerEntity player) {
        UUID id = player.getUuid();
        int used = windChargesUsed.getOrDefault(id, 0) + 1;
        windChargesUsed.put(id, Math.min(used, MAX_WIND_CHARGES));
    }

    public boolean hasWindCharge() {
        return localPlayerCount > 0;
    }

    public boolean hasOpponentUsed(UUID playerId) {
        return windChargesUsed.containsKey(playerId) && windChargesUsed.get(playerId) > 0;
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
        processedEntities.clear();
        localPlayerCount = 0;
    }
}
