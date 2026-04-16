package com.m4ssive.windchargecounter.mixin;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V", at = @At("TAIL"), require = 0)
    private void onWindChargeSpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        try {
            WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
            if (mod == null || mod.getTracker() == null) return;

            ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;
            if (handler.getWorld() == null) return;

            Entity entity = handler.getWorld().getEntityById(packet.getEntityId());
            if (entity == null) return;

            if (entity.getType() != EntityType.WIND_CHARGE && entity.getType() != EntityType.BREEZE_WIND_CHARGE) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return;

            Entity owner = null;
            if (entity instanceof ProjectileEntity projectile) {
                owner = projectile.getOwner();
            }

            if (owner == null) {
                double closestDist = Double.MAX_VALUE;
                PlayerEntity closestPlayer = null;
                for (PlayerEntity p : handler.getWorld().getPlayers()) {
                    if (p.getUuid().equals(client.player.getUuid())) continue;
                    double dist = p.squaredDistanceTo(entity);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestPlayer = p;
                    }
                }
                owner = closestPlayer;
            }

            if (owner instanceof PlayerEntity player && !player.getUuid().equals(client.player.getUuid())) {
                mod.getTracker().recordWindChargeUse(player);
            }
        } catch (Exception ignored) {}
    }
}
