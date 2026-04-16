package com.m4ssive.windchargecounter.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class TextHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("WindChargeCounter");
    private static Method drawTextShadowFlagMethod = null;
    private static Method drawTextWithShadowMethod = null;
    private static Method drawCenteredTextWithShadowMethod = null;
    private static boolean initialized = false;

    private static void init(DrawContext ctx) {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> textRendererClass = TextRenderer.class;
            Class<?> textClass = Text.class;
            for (Method m : ctx.getClass().getMethods()) {
                Class<?>[] p = m.getParameterTypes();
                String name = m.getName();
                if (drawTextShadowFlagMethod == null && p.length == 6
                        && p[0] == textRendererClass && p[1] == textClass
                        && p[2] == int.class && p[3] == int.class && p[4] == int.class
                        && p[5] == boolean.class) {
                    drawTextShadowFlagMethod = m;
                }
                if (drawTextWithShadowMethod == null && p.length == 5
                        && p[0] == textRendererClass && p[1] == textClass
                        && p[2] == int.class && p[3] == int.class && p[4] == int.class
                        && (name.contains("Shadow") || name.contains("shadow"))
                        && !(name.contains("Centered") || name.contains("centered"))) {
                    drawTextWithShadowMethod = m;
                }
                if (drawCenteredTextWithShadowMethod == null && p.length == 5
                        && p[0] == textRendererClass && p[1] == textClass
                        && p[2] == int.class && p[3] == int.class && p[4] == int.class
                        && (name.contains("Centered") || name.contains("centered"))) {
                    drawCenteredTextWithShadowMethod = m;
                }
            }
        } catch (Throwable e) {
            LOGGER.error("[TextHelper] init failed", e);
        }
    }

    public static void drawTextWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        init(context);
        if (drawTextShadowFlagMethod != null) {
            try { drawTextShadowFlagMethod.invoke(context, textRenderer, text, x, y, color, true); return; }
            catch (Throwable e) {}
        }
        if (drawTextWithShadowMethod != null) {
            try { drawTextWithShadowMethod.invoke(context, textRenderer, text, x, y, color); return; }
            catch (Throwable e) {}
        }
    }

    public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        init(context);
        if (drawTextShadowFlagMethod != null) {
            try { drawTextShadowFlagMethod.invoke(context, textRenderer, text, x, y, color, false); return; }
            catch (Throwable e) {}
        }
        if (drawTextWithShadowMethod != null) {
            try { drawTextWithShadowMethod.invoke(context, textRenderer, text, x, y, color); return; }
            catch (Throwable e) {}
        }
    }

    public static void drawCenteredTextWithShadow(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        init(context);
        if (drawCenteredTextWithShadowMethod != null) {
            try { drawCenteredTextWithShadowMethod.invoke(context, textRenderer, text, centerX, y, color); return; }
            catch (Throwable e) {}
        }
        int textWidth = textRenderer.getWidth(text);
        drawTextWithShadow(context, textRenderer, text, centerX - textWidth / 2, y, color);
    }
}
