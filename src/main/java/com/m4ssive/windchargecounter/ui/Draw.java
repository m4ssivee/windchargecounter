package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Drawing primitives that behave the same on 1.21–1.21.11.
 * Only fill / drawTextWithShadow are used; the matrix stack is the one version split (1.21.6).
 */
public final class Draw {
    private Draw() {}

    public static TextRenderer font() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static void rect(DrawContext ctx, int x, int y, int w, int h, int argb) {
        if (w > 0 && h > 0) ctx.fill(x, y, x + w, y + h, argb);
    }

    public static void border(DrawContext ctx, int x, int y, int w, int h, int t, int argb) {
        rect(ctx, x, y, w, t, argb);
        rect(ctx, x, y + h - t, w, t, argb);
        rect(ctx, x, y + t, t, h - 2 * t, argb);
        rect(ctx, x + w - t, y + t, t, h - 2 * t, argb);
    }

    public static void dashedV(DrawContext ctx, int x, int y1, int y2, int argb) {
        for (int y = y1; y < y2; y += 6) rect(ctx, x, y, 1, Math.min(4, y2 - y), argb);
    }

    public static void dashedH(DrawContext ctx, int y, int x1, int x2, int argb) {
        for (int x = x1; x < x2; x += 6) rect(ctx, x, y, Math.min(4, x2 - x), 1, argb);
    }

    /** Triangle pointing up, tip at (cx, y). */
    public static void caretUp(DrawContext ctx, int cx, int y, int size, int argb) {
        for (int i = 0; i < size; i++) rect(ctx, cx - i, y + i, 2 * i + 1, 1, argb);
    }

    /** Triangle pointing down, tip at (cx, y). */
    public static void caretDown(DrawContext ctx, int cx, int y, int size, int argb) {
        for (int i = 0; i < size; i++) rect(ctx, cx - i, y - i, 2 * i + 1, 1, argb);
    }

    /** Triangle pointing left, tip at (x, cy). */
    public static void caretLeft(DrawContext ctx, int x, int cy, int size, int argb) {
        for (int i = 0; i < size; i++) rect(ctx, x + i, cy - i, 1, 2 * i + 1, argb);
    }

    /** Triangle pointing right, tip at (x, cy). */
    public static void caretRight(DrawContext ctx, int x, int cy, int size, int argb) {
        for (int i = 0; i < size; i++) rect(ctx, x - i, cy - i, 1, 2 * i + 1, argb);
    }

    public static void text(DrawContext ctx, String s, int x, int y, int argb) {
        ctx.drawTextWithShadow(font(), s, x, y, argb);
    }

    /** Flat text without shadow, for dark text on light (accent) fills. */
    public static void textFlat(DrawContext ctx, String s, int x, int y, int argb) {
        ctx.drawText(font(), s, x, y, argb, false);
    }

    public static void textFlatCentered(DrawContext ctx, String s, int cx, int y, int argb) {
        textFlat(ctx, s, cx - textWidth(s) / 2, y, argb);
    }

    public static void textCentered(DrawContext ctx, String s, int cx, int y, int argb) {
        text(ctx, s, cx - textWidth(s) / 2, y, argb);
    }

    public static int textWidth(String s) {
        return font().getWidth(s);
    }

    /**
     * Ends the current layer so later fills cover earlier text. Before 1.21.6 text is batched and drawn
     * last, so without this a popover would show the text underneath it; 1.21.6+ keeps draw order itself.
     */
    public static void flush(DrawContext ctx) {
        //? if <1.21.6 {
        ctx.draw();
        //?}
    }

    // matrix stack: MatrixStack before 1.21.6, Matrix3x2fStack from 1.21.6
    public static void push(DrawContext ctx) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().pushMatrix();
        *///?} else {
        ctx.getMatrices().push();
        //?}
    }

    public static void pop(DrawContext ctx) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().popMatrix();
        *///?} else {
        ctx.getMatrices().pop();
        //?}
    }

    public static void translate(DrawContext ctx, float x, float y) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().translate(x, y);
        *///?} else {
        ctx.getMatrices().translate(x, y, 0f);
        //?}
    }

    public static void scale(DrawContext ctx, float s) {
        //? if >=1.21.6 {
        /*ctx.getMatrices().scale(s, s);
        *///?} else {
        ctx.getMatrices().scale(s, s, 1f);
        //?}
    }
}
