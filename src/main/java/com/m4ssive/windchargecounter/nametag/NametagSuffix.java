package com.m4ssive.windchargecounter.nametag;

import com.m4ssive.windchargecounter.WindChargeTracker;
import com.m4ssive.windchargecounter.config.ModConfig;
import com.m4ssive.windchargecounter.theme.ColorMath;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

/** Builds the " | ◉ 74!" text appended to player nametags. */
public final class NametagSuffix {
    /** Private-use char mapped to the 8×8 wind icon in assets/windchargecounter/font/icons.json. */
    public static final String ICON_CHAR = "";
    private static final Identifier ICON_FONT = Identifier.of("windchargecounter", "icons");
    private static final int SEPARATOR_COLOR = 0xAAAAAA;

    private NametagSuffix() {}

    public static Text build(int remaining, ModConfig cfg) {
        SuffixFormat.Segments s = SuffixFormat.segments(remaining, WindChargeTracker.MAX_WIND_CHARGES,
            cfg.nametagSeparator, cfg.nametagIcon, cfg.nametagShowMax, cfg.lowWarning);
        int countColor = cfg.colorByCharges
            ? ColorMath.countColor(remaining, WindChargeTracker.MAX_WIND_CHARGES, cfg.fullColor, cfg.halfColor, cfg.lowColor)
            : cfg.accentColor;

        MutableText out = Text.literal(" ");
        if (!s.separator().isEmpty()) {
            out.append(colored(s.separator(), SEPARATOR_COLOR)).append(" ");
        }
        if (s.icon()) {
            out.append(Text.literal(ICON_CHAR).setStyle(iconStyle())).append(" ");
        }
        out.append(colored(s.count(), countColor));
        if (s.warning()) {
            out.append(colored("!", cfg.lowColor));
        }
        return out;
    }

    private static MutableText colored(String text, int rgb) {
        return Text.literal(text).styled(st -> st.withColor(TextColor.fromRgb(rgb & 0xFFFFFF)));
    }

    // the font id moved behind StyleSpriteSource in 1.21.9
    private static Style iconStyle() {
        //? if >=1.21.9 {
        /*return Style.EMPTY.withFont(new net.minecraft.text.StyleSpriteSource.Font(ICON_FONT));
        *///?} else {
        return Style.EMPTY.withFont(ICON_FONT);
        //?}
    }
}
