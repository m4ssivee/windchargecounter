package com.m4ssive.windchargecounter.editor.tabs;

import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.editor.EditorScreen;
import com.m4ssive.windchargecounter.editor.Popover;
import com.m4ssive.windchargecounter.theme.ColorMath;
import com.m4ssive.windchargecounter.theme.Theme;
import com.m4ssive.windchargecounter.theme.Themes;
import com.m4ssive.windchargecounter.ui.Button;
import com.m4ssive.windchargecounter.ui.Divider;
import com.m4ssive.windchargecounter.ui.Draw;
import com.m4ssive.windchargecounter.ui.Label;
import com.m4ssive.windchargecounter.ui.Slider;
import com.m4ssive.windchargecounter.ui.Toggle;
import com.m4ssive.windchargecounter.ui.UiTheme;
import com.m4ssive.windchargecounter.ui.Widget;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Presets, custom colors, count colors and panel opacity (spec §7.3 Theme, §8). */
public final class ThemeTab {
    public static final int WIDTH = 212;

    private ThemeTab() {}

    public static Popover create(ModConfig cfg, EditorScreen screen) {
        Button reset = new Button(Draw.textWidth("Reset") + 10, 12, () -> "Reset", false,
            () -> cfg.applyTheme(Themes.byId(cfg.themeId)));
        Slider opacity = new Slider(0, "Panel opacity", v -> Math.round(v) + "%", 0f, 100f, 1f,
            () -> (float) cfg.panelOpacity, v -> cfg.panelOpacity = Math.round(v), 0);
        return new Popover("Theme", List.of(
            new PresetRow(cfg),
            new Divider(),
            new Label("CUSTOM COLORS"),
            new ColorRow(screen, cfg, new String[]{"Accent", "Text", "Panel"},
                new IntSupplier[]{() -> cfg.accentColor, () -> cfg.textColor, () -> cfg.panelColor},
                new IntConsumer[]{v -> cfg.accentColor = v, v -> cfg.textColor = v, v -> cfg.panelColor = v}),
            new Label("COUNT COLORS"),
            new Toggle(0, "Color by charges left", () -> cfg.colorByCharges, v -> cfg.colorByCharges = v),
            new GradientBar(cfg),
            new ColorRow(screen, cfg, new String[]{"Low", "Half", "Full"},
                new IntSupplier[]{() -> cfg.lowColor, () -> cfg.halfColor, () -> cfg.fullColor},
                new IntConsumer[]{v -> cfg.lowColor = v, v -> cfg.halfColor = v, v -> cfg.fullColor = v}),
            opacity), WIDTH, reset);
    }

    /** Five preset cards: three color stripes and the name. */
    static final class PresetRow extends Widget {
        private static final int GAP = 3;
        private final ModConfig cfg;

        PresetRow(ModConfig cfg) {
            super(0, 26);
            this.cfg = cfg;
        }

        private int cardW() {
            return (w - GAP * (Themes.ALL.size() - 1)) / Themes.ALL.size();
        }

        @Override
        public void render(DrawContext ctx, int mx, int my, UiTheme t) {
            int cw = cardW();
            for (int i = 0; i < Themes.ALL.size(); i++) {
                Theme th = Themes.ALL.get(i);
                int cx = x + i * (cw + GAP);
                boolean sel = !cfg.customTheme && th.id().equals(cfg.themeId);
                boolean hover = mx >= cx && mx < cx + cw && my >= y && my < y + h;
                Draw.rect(ctx, cx, y, cw, h, ColorMath.argb(ColorMath.lerp(th.panel(), th.accent(), 0.12f), 255));
                Draw.border(ctx, cx, y, cw, h, 1, sel ? t.accent() : hover ? t.text() : t.line());
                int sx = cx + (cw - 15) / 2;
                int[] stripes = {th.accent(), th.full(), th.low()};
                for (int s = 0; s < 3; s++) Draw.rect(ctx, sx + s * 5, y + 4, 5, 7, ColorMath.argb(stripes[s], 255));
                Draw.textCentered(ctx, th.name(), cx + cw / 2, y + 15, ColorMath.argb(th.text(), 255));
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || !contains(mx, my)) return false;
            int i = (int) ((mx - x) / (cardW() + GAP));
            cfg.applyTheme(Themes.ALL.get(Math.max(0, Math.min(Themes.ALL.size() - 1, i))));
            return true;
        }
    }

    /** Three "swatch + name + hex" buttons; clicking opens the color picker. */
    static final class ColorRow extends Widget {
        private static final int GAP = 3;
        private final EditorScreen screen;
        private final ModConfig cfg;
        private final String[] names;
        private final IntSupplier[] getters;
        private final IntConsumer[] setters;

        ColorRow(EditorScreen screen, ModConfig cfg, String[] names, IntSupplier[] getters, IntConsumer[] setters) {
            super(0, 20);
            this.screen = screen;
            this.cfg = cfg;
            this.names = names;
            this.getters = getters;
            this.setters = setters;
        }

        private int cellW() {
            return (w - GAP * (names.length - 1)) / names.length;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my, UiTheme t) {
            int cw = cellW();
            for (int i = 0; i < names.length; i++) {
                int cx = x + i * (cw + GAP);
                boolean hover = mx >= cx && mx < cx + cw && my >= y && my < y + h;
                Draw.rect(ctx, cx, y, cw, h, t.surfaceAlt());
                Draw.border(ctx, cx, y, cw, h, 1, hover ? t.accent() : t.line());
                Draw.rect(ctx, cx + 3, y + 5, 10, 10, ColorMath.argb(getters[i].getAsInt(), 255));
                Draw.border(ctx, cx + 3, y + 5, 10, 10, 1, 0xFF3A3F6A);
                Draw.text(ctx, names[i], cx + 16, y + 6, t.text());
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || !contains(mx, my)) return false;
            int cw = cellW();
            int i = Math.max(0, Math.min(names.length - 1, (int) ((mx - x) / (cw + GAP))));
            IntConsumer set = setters[i];
            screen.openPicker(this, i * (cw + GAP), h + 2, getters[i], v -> {
                set.accept(v);
                cfg.markCustom();
            });
            return true;
        }
    }

    /** Low → Half → Full preview. */
    static final class GradientBar extends Widget {
        private static final int SEGMENTS = 24;
        private final ModConfig cfg;

        GradientBar(ModConfig cfg) {
            super(0, 5);
            this.cfg = cfg;
        }

        @Override
        public void render(DrawContext ctx, int mx, int my, UiTheme t) {
            for (int s = 0; s < SEGMENTS; s++) {
                int x1 = x + s * w / SEGMENTS, x2 = x + (s + 1) * w / SEGMENTS;
                int remaining = Math.round(128f * s / (SEGMENTS - 1));
                int c = ColorMath.countColor(remaining, 128, cfg.fullColor, cfg.halfColor, cfg.lowColor);
                Draw.rect(ctx, x1, y, x2 - x1, h, ColorMath.argb(c, 255));
            }
        }
    }
}
