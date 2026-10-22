package com.m4ssive.windchargecounter.nametag;

/** Decides what the nametag suffix shows. Pure logic, no Minecraft classes. */
public final class SuffixFormat {
    /** separator ("" = none), icon shown, count text, low warning ("!"). */
    public record Segments(String separator, boolean icon, String count, boolean warning) {}

    private SuffixFormat() {}

    public static Segments segments(int remaining, int max, String separator, boolean icon, boolean showMax, int lowWarning) {
        String count = showMax ? remaining + "/" + max : String.valueOf(remaining);
        String sep = separator == null ? "" : separator;
        if (sep.equals("[]")) {
            sep = "[";
            count = count + "]";
        }
        boolean warning = lowWarning > 0 && remaining <= lowWarning;
        return new Segments(sep, icon, count, warning);
    }
}
