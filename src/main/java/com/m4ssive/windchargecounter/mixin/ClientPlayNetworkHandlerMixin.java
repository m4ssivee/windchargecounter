package com.m4ssive.windchargecounter.mixin;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    // wind charge spawn paketini yakala - entity'yi world'den aramaya gerek yok
    @Inject(method = "onEntitySpawn", at = @At("HEAD"), require = 0)
    private void onWindChargeSpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        try {
            EntityType<?> type = packet.getEntityType();
            if (type != EntityType.WIND_CHARGE && type != EntityType.BREEZE_WIND_CHARGE) return;

            WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
            if (mod == null || mod.getTracker() == null) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return;

            ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;
            if (handler.getWorld() == null) return;

            // spawn pozisyonunu paketten al
            double spawnX = packet.getX();
            double spawnY = packet.getY();
            double spawnZ = packet.getZ();

            // en yakın oyuncuyu bul (kendimiz dahil)
            double closestDist = Double.MAX_VALUE;
            PlayerEntity closestPlayer = null;
            for (PlayerEntity p : handler.getWorld().getPlayers()) {
                double dx = p.getX() - spawnX;
                double dy = p.getY() - spawnY;
                double dz = p.getZ() - spawnZ;
                double dist = dx * dx + dy * dy + dz * dz;
                if (dist < closestDist) {
                    closestDist = dist;
                    closestPlayer = p;
                }
            }

            // eğer en yakın oyuncu biz değilsek (rakipse) ve mesafe makul bir seviyedeyse
            if (closestPlayer != null && !closestPlayer.getUuid().equals(client.player.getUuid()) && closestDist < 16.0) {
                mod.getTracker().recordWindChargeUse(closestPlayer);
                WindChargeCounterMod.LOGGER.info("[WCC] Rakip mermi spawn tespit edildi: {} (mesafe: {})", closestPlayer.getName().getString(), Math.sqrt(closestDist));
            } else if (closestPlayer != null && closestPlayer.getUuid().equals(client.player.getUuid())) {
                WindChargeCounterMod.LOGGER.info("[WCC] Bizim mermi tespit edildi, ignore edildi.");
            }
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("[WCC] Error in spawn mixin", e);
        }
    }
}
