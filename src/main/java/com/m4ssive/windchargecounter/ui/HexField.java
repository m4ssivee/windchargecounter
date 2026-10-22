package com.m4ssive.windchargecounter.ui;

import com.m4ssive.windchargecounter.theme.ColorMath;
import net.minecraft.client.gui.DrawContext;

import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** 6-digit hex input. Enter or clicking away commits; invalid text keeps the old color and shows red. */
public class HexField extends Widget {
    private static final int KEY_ENTER = 257, KEY_KP_ENTER = 335, KEY_BACKSPACE = 259, KEY_ESCAPE = 256;
    private final IntSupplier get;
    private final IntConsumer onCommit;
    private String text = "";
    private boolean invalid;

    public HexField(int w, IntSupplier get, IntConsumer onCommit) {
        super(w, 14);
        this.get = get;
        this.onCommit = onCommit;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        if (!focused) text = ColorMath.toHex(get.getAsInt());
        Draw.rect(ctx, x, y, w, h, t.surfaceAlt());
        Draw.border(ctx, x, y, w, h, 1, invalid ? 0xFFFF5555 : focused ? t.accent() : t.line());
        Draw.text(ctx, "#", x + 3, y + 3, t.muted());
        String shown = text + (focused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        Draw.text(ctx, shown, x + 10, y + 3, t.text());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean inside = contains(mx, my);
        if (inside && !focused) {
            focused = true;
            text = ColorMath.toHex(get.getAsInt());
            invalid = false;
        } else if (!inside && focused) {
            commit();
        }
        return inside;
    }

    @Override
    public boolean keyPressed(int key, int modifiers) {
        if (!focused) return false;
        switch (key) {
            case KEY_ENTER, KEY_KP_ENTER -> commit();
            case KEY_BACKSPACE -> {
                if (!text.isEmpty()) text = text.substring(0, text.length() - 1);
            }
            case KEY_ESCAPE -> {
                focused = false;
                invalid = false;
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public boolean charTyped(String chars) {
        if (!focused) return false;
        for (char c : chars.toCharArray()) {
            if (c == '#') continue;
            if (Character.digit(c, 16) >= 0 && text.length() < 6) text += Character.toUpperCase(c);
        }
        return true;
    }

    private void commit() {
        OptionalInt v = ColorMath.parseHex(text);
        if (v.isPresent()) {
            onCommit.accept(v.getAsInt());
            invalid = false;
            focused = false;
        } else {
            invalid = true;
        }
    }
}
