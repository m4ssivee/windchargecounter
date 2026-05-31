package com.m4ssive.windchargecounter.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.WindChargeTracker;
import com.m4ssive.windchargecounter.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text windchargecounter$modifyDisplayName(Text original) {
        try {
            PlayerEntity self = (PlayerEntity) (Object) this;
            if (self == null) return original;

            WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
            if (mod == null) return original;

            ModConfig config = mod.getConfig();
            if (config == null || !config.showInNametags) return original;

            WindChargeTracker tracker = mod.getTracker();
            if (tracker == null) return original;

            if (!self.isAlive()) return original;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return original;

            boolean isSelf = self.getUuid().equals(client.player.getUuid());
            int remaining;

            if (isSelf) {
                remaining = tracker.getLocalPlayerCharges();
                if (remaining <= 0) return original;
            } else {
                int used = tracker.getWindChargesUsed(self.getUuid());
                remaining = WindChargeTracker.MAX_WIND_CHARGES - used;
            }

            // Exactly replicating TotemCounter style text formatting
            MutableText suffixText = Text.literal(" | ")
                    .setStyle(net.minecraft.text.Style.EMPTY.withColor(net.minecraft.util.Formatting.GRAY));

            String display = remaining + " WC";
            
            float ratio = (float) remaining / WindChargeTracker.MAX_WIND_CHARGES;
            int color;
            if (ratio > 0.75f) color = 0x55DDFF;
            else if (ratio > 0.5f) color = 0x00CED1;
            else if (ratio > 0.25f) color = 0xFFFF55;
            else color = 0xFF5555;

            MutableText counter = Text.literal(display)
                    .setStyle(net.minecraft.text.Style.EMPTY.withColor(TextColor.fromRgb(color)));

            MutableText updated = original.copy().append(suffixText).append(counter);
            return updated;
        } catch (Exception e) {
            WindChargeCounterMod.LOGGER.error("[PlayerEntityMixin] Error modifying displayName", e);
            return original;
        }
    }
}
