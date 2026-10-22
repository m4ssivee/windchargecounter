package com.m4ssive.windchargecounter.theme;

import java.util.OptionalInt;

/** Color helpers. No Minecraft classes, so they are unit-testable. */
public final class ColorMath {
    private ColorMath() {}

    /** RGB + alpha (0-255) → ARGB. */
    public static int argb(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    /** Linear blend of two RGB colors, t in [0,1]. */
    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** Count color: Full at max, Half at max/2, Low at 0, interpolated; out-of-range clamps. RGB. */
    public static int countColor(int remaining, int max, int full, int half, int low) {
        float ratio = max <= 0 ? 0f : Math.max(0f, Math.min(1f, remaining / (float) max));
        return ratio >= 0.5f ? lerp(half, full, (ratio - 0.5f) * 2f) : lerp(low, half, ratio * 2f);
    }

    /** Parses "RRGGBB" or "#RRGGBB" (any case). Anything else → empty. */
    public static OptionalInt parseHex(String s) {
        if (s == null) return OptionalInt.empty();
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() != 6) return OptionalInt.empty();
        for (int i = 0; i < 6; i++) if (Character.digit(s.charAt(i), 16) < 0) return OptionalInt.empty();
        return OptionalInt.of(Integer.parseInt(s, 16));
    }

    /** RGB → six upper-case hex digits, alpha ignored. */
    public static String toHex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }
}
