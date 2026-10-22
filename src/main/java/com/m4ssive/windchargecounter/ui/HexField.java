package com.m4ssive.windchargecounter.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * 6-digit hex input. Clicking selects the current value (typing replaces it), Ctrl/Cmd+V pastes,
 * Enter or clicking away commits; invalid text keeps the old color and shows red.
 */
public class HexField extends Widget {
    private static final int KEY_ENTER = 257, KEY_KP_ENTER = 335, KEY_BACKSPACE = 259, KEY_ESCAPE = 256, KEY_V = 86;
    private static final int MOD_CONTROL = 0x0002, MOD_SUPER = 0x0008;
    private final IntSupplier get;
    private final IntConsumer onCommit;
    private final HexInput input = new HexInput();
    private boolean invalid;

    public HexField(int w, IntSupplier get, IntConsumer onCommit) {
        super(w, 14);
        this.get = get;
        this.onCommit = onCommit;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, UiTheme t) {
        String shown = focused ? input.text() : com.m4ssive.windchargecounter.theme.ColorMath.toHex(get.getAsInt());
        Draw.rect(ctx, x, y, w, h, t.surfaceAlt());
        Draw.border(ctx, x, y, w, h, 1, invalid ? 0xFFFF5555 : focused ? t.accent() : t.line());
        Draw.text(ctx, "#", x + 3, y + 3, t.muted());
        if (focused && input.isSelected()) {
            Draw.rect(ctx, x + 9, y + 2, Draw.textWidth(shown) + 2, 10, UiTheme.alpha(t.accent(), 110));
        }
        boolean caret = focused && !input.isSelected() && (System.currentTimeMillis() / 500) % 2 == 0;
        Draw.text(ctx, shown + (caret ? "_" : ""), x + 10, y + 3, t.text());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean inside = contains(mx, my);
        if (inside && !focused) {
            focused = true;
            input.focus(get.getAsInt());
            invalid = false;
        } else if (!inside && focused) {
            commit();
            if (invalid) cancel(); // leaving with bad text: drop it instead of staying red
        }
        return inside;
    }

    @Override
    public boolean keyPressed(int key, int modifiers) {
        if (!focused) return false;
        if (key == KEY_V && (modifiers & (MOD_CONTROL | MOD_SUPER)) != 0) {
            input.paste(MinecraftClient.getInstance().keyboard.getClipboard());
            invalid = false;
            return true;
        }
        switch (key) {
            case KEY_ENTER, KEY_KP_ENTER -> commit();
            case KEY_BACKSPACE -> input.backspace();
            case KEY_ESCAPE -> cancel();
            default -> { return true; } // swallow other keys while typing
        }
        return true;
    }

    @Override
    public boolean charTyped(String chars) {
        if (!focused) return false;
        input.type(chars);
        invalid = false;
        return true;
    }

    /** Leaves the field without applying anything. */
    public void cancel() {
        focused = false;
        invalid = false;
    }

    private void commit() {
        OptionalInt v = input.value();
        if (v.isPresent()) {
            onCommit.accept(v.getAsInt());
            invalid = false;
            focused = false;
        } else {
            invalid = true;
        }
    }
}
