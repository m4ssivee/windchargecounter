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

    // fırlatanın kim olduğu paketten anlaşılamazsa en yakın oyuncuya bakılır (blok cinsinden)
    private static final double FALLBACK_MAX_DISTANCE = 3.0;

    @Inject(method = "onEntitySpawn", at = @At("HEAD"))
    private void windchargecounter$onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        try {
            // sadece oyuncuların attığı wind charge; breeze mermileri sayılmaz
            if (packet.getEntityType() != EntityType.WIND_CHARGE) return;

            WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
            if (mod == null || mod.getTracker() == null) return;

            MinecraftClient client = MinecraftClient.getInstance();
            // onEntitySpawn önce netty thread'inde çağrılır, sonra forceMainThread ile ana thread'de
            // tekrar çağrılır. HEAD'de olduğumuz için sadece ana thread çağrısını sayıyoruz
            // (eski 2x sayma hatasının asıl sebebi buydu).
            if (!client.isOnThread()) return;
            if (client.player == null) return;

            ClientWorld world = ((ClientPlayNetworkHandler) (Object) this).getWorld();
            if (world == null) return;

            PlayerEntity shooter = null;

            // projectile spawn paketinde entityData = fırlatanın entity id'si (yoksa 0)
            int ownerId = packet.getEntityData();
            if (ownerId != 0) {
                Entity owner = world.getEntityById(ownerId);
                if (owner instanceof PlayerEntity p) shooter = p;
                else if (owner != null) return; // oyuncu olmayan bir şey attı
            }

            if (shooter == null) {
                shooter = findClosestPlayer(world, packet.getX(), packet.getY(), packet.getZ());
            }

            if (shooter == null) return;
            if (shooter.getUuid().equals(client.player.getUuid())) return; // kendimizi envanterden sayıyoruz

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
            // wind charge oyuncunun göz hizasından doğar
            double d = p.getEyePos().squaredDistanceTo(x, y, z);
            if (d < best) {
                best = d;
                closest = p;
            }
        }
        return closest;
    }
}
