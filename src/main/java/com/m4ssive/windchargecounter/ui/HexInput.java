package com.m4ssive.windchargecounter.ui;

import com.m4ssive.windchargecounter.theme.ColorMath;

import java.util.OptionalInt;

/**
 * Text state of the hex field. On focus the current value is shown "selected":
 * the first typed character, backspace or paste replaces it. Pure logic, unit-tested.
 */
public final class HexInput {
    private String text = "";
    private boolean selected;

    public void focus(int rgb) {
        text = ColorMath.toHex(rgb);
        selected = true;
    }

    public String text() {
        return text;
    }

    public boolean isSelected() {
        return selected;
    }

    public void type(String chars) {
        for (char c : chars.toCharArray()) {
            if (Character.digit(c, 16) < 0) continue;
            if (selected) {
                text = "";
                selected = false;
            }
            if (text.length() < 6) text += Character.toUpperCase(c);
        }
    }

    public void backspace() {
        if (selected) {
            text = "";
            selected = false;
        } else if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
        }
    }

    /** Pasted text replaces everything; "#", spaces and other non-hex characters are dropped. */
    public void paste(String clip) {
        if (clip == null) return;
        text = "";
        selected = false;
        type(clip.trim());
    }

    public OptionalInt value() {
        return ColorMath.parseHex(text);
    }
}
