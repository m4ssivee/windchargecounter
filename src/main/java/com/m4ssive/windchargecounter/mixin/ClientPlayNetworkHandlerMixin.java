package com.m4ssive.windchargecounter.mixin;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    // if the packet doesn't tell who threw it, use the nearest player within this many blocks
    private static final double FALLBACK_MAX_DISTANCE = 3.0;

    @Inject(method = "onEntitySpawn", at = @At("HEAD"))
    private void windchargecounter$onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        try {
            // only player wind charges; breeze charges are not counted
            if (packet.getEntityType() != EntityType.WIND_CHARGE) return;

            WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
            if (mod == null || mod.getTracker() == null) return;

            MinecraftClient client = MinecraftClient.getInstance();
            // onEntitySpawn runs first on the netty thread, then again on the main thread via
            // forceMainThread. We inject at HEAD, so only the main-thread call is counted
            // (this was the real cause of the old double-counting bug).
            if (!client.isOnThread()) return;
            if (client.player == null) return;

            ClientWorld world = ((ClientPlayNetworkHandler) (Object) this).getWorld();
            if (world == null) return;

            PlayerEntity shooter = null;

            // for projectiles the spawn packet's entityData is the owner's entity id (0 if none)
            int ownerId = packet.getEntityData();
            if (ownerId != 0) {
                Entity owner = world.getEntityById(ownerId);
                if (owner instanceof PlayerEntity p) shooter = p;
                else if (owner != null) return; // thrown by something that isn't a player
            }

            if (shooter == null) {
                shooter = findClosestPlayer(world, packet.getX(), packet.getY(), packet.getZ());
            }

            if (shooter == null) return;
            if (shooter.getUuid().equals(client.player.getUuid())) return; // our own count comes from the inventory

            mod.getTracker().recordWindChargeUse(shooter, packet.getUuid());
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("[WCC] Error handling wind charge spawn", e);
        }
    }

    private static PlayerEntity findClosestPlayer(ClientWorld world, double x, double y, double z) {
        double best = FALLBACK_MAX_DISTANCE * FALLBACK_MAX_DISTANCE;
        PlayerEntity closest = null;
        for (PlayerEntity p : world.getPlayers()) {
            if (!p.isAlive()) continue;
            // wind charges spawn at the thrower's eye height
            double d = p.getEyePos().squaredDistanceTo(x, y, z);
            if (d < best) {
                best = d;
                closest = p;
            }
        }
        return closest;
    }
}
