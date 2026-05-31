package com.m4ssive.windchargecounter.mixin;

import com.m4ssive.windchargecounter.WindChargeCounterMod;
import com.m4ssive.windchargecounter.WindChargeTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow public abstract TextRenderer getTextRenderer();

    /**
     * Injects at HEAD of renderLabelIfPresent.
     * We do NOT include tickDelta to stay compatible with Minecraft 1.21 (no tickDelta param)
     * AND 1.21.1 (tickDelta present). Mixin will match whichever signature exists at runtime.
     */
    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    protected void onRenderLabelIfPresent(T entity, Text text, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        try {
            if (!(entity instanceof AbstractClientPlayerEntity player)) return;

            WindChargeCounterMod mod = WindChargeCounterMod.getInstance();
            if (mod == null || !mod.getConfig().showInNametags) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            // Skip self
            if (player.getUuid().equals(client.player.getUuid())) return;
            if (!player.isAlive()) return;

            WindChargeTracker tracker = mod.getTracker();
            int used = tracker.getWindChargesUsed(player.getUuid());
            int remaining = WindChargeTracker.MAX_WIND_CHARGES - used;

            String displayStr = "\u2B21 " + remaining + "/" + WindChargeTracker.MAX_WIND_CHARGES;
            MutableText displayText = Text.literal(displayStr);

            float ratio = (float) remaining / WindChargeTracker.MAX_WIND_CHARGES;
            int color;
            if (ratio > 0.75f) color = 0x55DDFF;
            else if (ratio > 0.5f) color = 0x00CED1;
            else if (ratio > 0.25f) color = 0xFFFF55;
            else color = 0xFF5555;
            displayText.setStyle(displayText.getStyle().withColor(TextColor.fromRgb(color)));

            // Render ABOVE where vanilla renders the nametag.
            // renderLabelIfPresent translates by getNameLabelHeight() which is ~height+0.5.
            // We add an extra 0.45 to appear above the existing nametag.
            matrices.push();
            matrices.translate(0.0F, 0.45F, 0.0F);
            matrices.scale(1.0F, 1.0F, 1.0F); // no extra scale needed; already in label space

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            TextRenderer textRenderer = this.getTextRenderer();
            float xOffset = -textRenderer.getWidth(displayText) / 2f;

            float bgOpacity = client.options.getTextBackgroundOpacity(0.25F);
            int background = (int)(bgOpacity * 255.0F) << 24;

            // Draw see-through version (visible through walls like vanilla nametag)
            textRenderer.draw(displayText, xOffset, 0.0F, 0x20FFFFFF, false,
                    matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, background, light);
            // Draw opaque version on top
            textRenderer.draw(displayText, xOffset, 0.0F, 0xFFFFFFFF, false,
                    matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();

        } catch (Exception e) {
            // Silently ignore render errors to avoid crashes
        }
    }
}
