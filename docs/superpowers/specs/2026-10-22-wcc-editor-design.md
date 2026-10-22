# WindChargeCounter — Editor & Theme Redesign

Status: draft for approval · 2026-10-22
Visual spec: the "WCC Ayar Ekranı Taslakları" canvas (claude.ai artifact). Artboards
**D · Floating Toolbar** (`Toolbar.dc.html`) and **Nametag · Suffix** (`Nametags.dc.html`)
are the reference; the other artboards are rejected alternatives.

## 1. Intent

The user found the current HUD editing experience bad: the edit mode (K) and the
config screen are two disconnected screens, the config screen is a plain grid of vanilla
buttons, and it is only reachable through Mod Menu. They want **one main place where
everything can be edited**, looking good, with harmonious colors and a wind-charge theme.

Explicit wishes from the user:
- Floating-toolbar editor (concept D) — chosen over sidebar, hotbar dock and inventory concepts.
- Good color options (themes + custom colors).
- Minecraft font look; **all UI text in English** (global audience).
- Nametag in the **Suffix** style, polished.
- HUD keeps the **Panel** style only.
- Must look different from TotemCounterV2, so the GUI lives in WindChargeCounter, **not in m4lib**.
- The user tests every build in-game (GUI, HUD, everything) before anything is pushed.

## 2. Goals

1. A single in-game editor ("WCC Editor") that replaces both `EditModeScreen` and `ConfigScreen`.
2. Opened from: the keybind (default K, rebindable), `/wcc` with no arguments, and Mod Menu's config button.
3. Every setting is editable from the editor with live preview on the real game screen.
4. Theme system: 5 presets plus individually editable colors.
5. Nametag suffix with a custom pixel wind-charge icon and a low-charge warning.
6. Identical look and behavior on every supported version, 1.21 through 1.21.11.

## 3. Non-goals

- Other HUD styles (Pill, Tower, Minimal) — not now.
- A shared GUI in m4lib.
- Sounds/alerts, keybind editing inside the editor (keybinds stay in vanilla Controls).
- Changes to the counting logic (spawn-packet tracking stays as it is).

## 4. Anti-goals (would be a failure even if it "works")

- Anything that looks like vanilla buttons/sliders.
- Visible differences between Minecraft versions.
- A new runtime dependency for players (no Cloth Config, YACL, owo-lib).
- Any regression in counting accuracy.
- Turkish (or any non-English) strings in the UI.

## 5. Constraints

- Fabric, Yarn mappings, Stonecutter multi-version build (1.21 … 1.21.11), Java 21.
- Draw only with primitives that are stable across 1.21.x: `DrawContext.fill`,
  `drawTextWithShadow`, `drawItem`, plus the existing matrix helpers in `WindChargeHud`
  (push/pop/translate/scale with the 1.21.6 `Matrix3x2fStack` split).
- All colors are ARGB with explicit alpha (1.21.6+ drops alpha-0 text).
- Input APIs differ at 1.21.9 (`Click`, `KeyInput`, `CharInput`); keep those differences
  inside the screen class behind Stonecutter conditionals, translate them into one internal
  input model so widgets are version-agnostic.
- In-game text uses Minecraft's own font. (Mockups use Monocraft, an OFL look-alike, so widths differ slightly.)
- Everything is laid out in GUI-scaled pixels and must fit at GUI scale "Auto" on a 1280×720 window
  and still work at 854×480.

## 6. Approaches considered

| Approach | Trade-off | Decision |
|---|---|---|
| **1. Fully custom-drawn editor** (own small widget set: toggle, slider, swatch, segmented tabs, popover, toolbar) | Most code, but full control of the look and identical across all 12 versions | **Chosen** (user: "önerin gibi yap") |
| 2. Restyled vanilla widgets | Less code, but widget rendering/API changed in 1.21.6 and 1.21.9, and it drifts back to a vanilla look | Rejected |
| 3. Config library (Cloth Config / YACL) | Fast list-style screens, but cannot do a floating toolbar anchored to the HUD and adds a player dependency | Rejected |

## 7. The editor

### 7.1 Screen

- Game stays visible, dimmed with `#0A1020` at ~38% alpha. Game does not pause.
- The HUD is drawn in edit state: 2px accent outline offset 3px, four 8×8 corner handles.
  Real tracker data if available, otherwise preview rows (`You 112`, `Enemy 74`, `FarAway 23`).
- **Top tab strip** (centered, 18px from top): `WCC` logo mark │ `Layout` `Counter` `Nametag` `Theme` │ `Cancel` `Done`.
  The active tab is filled with the accent color.
- **Floating toolbar** attached 12px below the HUD, left-aligned with it. It flips above the HUD
  when there is not enough room below and is clamped to the screen horizontally.
  Contents: `−` scale `+` │ theme chip (three color stripes + theme name) │ `…`.
  The theme chip opens the Theme popover; `…` opens the popover of the active tab.
- **Popover**: anchored under the toolbar with a small caret pointing at the button that opened it.
  Clicking a tab in the top strip opens that tab's popover. Clicking empty screen space closes it.
- **Hint bar** bottom center: `Click select · Drag move · Scroll resize · Arrows nudge 1px`.

### 7.2 Interaction

- Drag the HUD to move it. Snaps to screen edges and center lines within 6px; a dashed accent guide
  line shows the active snap. Hold Shift to move freely.
- Scroll over the HUD: scale ±0.1 (range 0.5–3.0). Corner handles are visual only in v1.
- Arrow keys nudge 1px, Shift+arrow 10px.
- `Esc` or `Done`: save and close. `Cancel`: restore the config snapshot taken when the editor opened, then close.
  All changes preview live while editing.

### 7.3 Popover contents

**Layout**
- Scale slider (0.5–3.0, step 0.1) with value label.
- "Snap to" 3×3 grid: moves the HUD to that anchor with a 4px margin.
- Snap to edges (toggle, default on).
- Reset position.

**Counter**
- Show HUD, Header, Progress bars, Flash on throw, Show yourself, Reset on death (toggles).
  *(Show HUD is my call: the old config had `enableHud` and the mockup forgot it.)*
- Reset counters (button).

**Nametag** (matches `Nametags.dc.html`)
- Show on nametags, Wind icon, Show /128, Color by charges left, On my own nametag (toggles).
- Low warning at: slider 0–64, default 16.
- Separator: `|` · `-` · `[ ]` · none.

**Theme** (matches `Toolbar.dc.html`)
- Presets row: Wind, Breeze, Night, Ember, Mono.
- Custom colors: Accent, Text, Panel (swatch + hex label). Clicking a swatch opens a small color
  picker: a palette grid of curated colors plus a 6-digit hex field.
- Count colors: Low / Half / Full swatches with a gradient preview bar, and a toggle
  "Color by charges left" (off → numbers use the accent color).
- Panel opacity slider (0–100%).
- Reset (back to the selected preset's values).

## 8. Themes

Selecting a preset copies its colors into the config's color fields. Editing any color afterwards
marks the theme as "Custom" (the chip shows "Custom"). Starting values — tune in-game if needed:

| Preset | Accent | Text | Panel | Full | Half | Low |
|---|---|---|---|---|---|---|
| Wind (default) | 8BE9FF | EAF2FF | 0B0F17 | 5FE36A | D9E04A | FF6A4A |
| Breeze | B8A6FF | EAF2FF | 0E0B1E | 7FF0C8 | F2C46D | FF6A9A |
| Night | 5B8CFF | EAF2FF | 0A0F24 | 8BE9FF | FFD25A | FF6A9A |
| Ember | FF9A4A | FFF1E6 | 1A0F0A | FFE08A | FFB86A | E5484D |
| Mono | D6DCE6 | EAF2FF | 101216 | EAEFF6 | B4BCC8 | FF6A6A |

Count color for a value is interpolated Full → Half → Low by the remaining/128 ratio.
The editor's own chrome (tabs, toolbar, popovers) also follows the accent color.

## 9. HUD (Panel style)

Same layout as the current 1.2.0 HUD, driven by the theme: header icon and title tinted
from the accent, own name in the accent color, numbers and left stripe in count colors (or accent),
bars in count colors, panel background = Panel color × opacity.

## 10. Nametag suffix

`<name> <sep> <icon> <count>[!]`
- `<sep>`: chosen separator in gray; omitted for "none".
- `<icon>`: a custom 8×8 pixel wind-charge glyph from a bitmap font provider shipped in the mod
  (`assets/windchargecounter/font/…` + texture), applied with a text style font id. If a version
  cannot apply a custom font in a nametag, fall back to no icon (never to `⬡`).
- `<count>`: remaining charges, `/128` appended when enabled, colored by count colors or accent.
- `!` appended in the Low color when count ≤ low-warning threshold (threshold 0 disables it).
- Provided through m4lib's existing `registerModSuffix` API; no m4lib changes.

## 11. Config

Existing `windchargecounter.json` keeps working: unknown fields are ignored, missing fields
take defaults. New fields: theme id, accent/text/panel/full/half/low colors, colorByCharges,
panelOpacity (replaces backgroundOpacity, migrate the old value), snapToEdges, nametag options
(icon, showMax, ownNametag, lowWarning, separator).

## 12. Entry points

- Keybind (default K) opens the editor; the binding's name becomes "Open WCC Editor".
- `/wcc` with no arguments opens the editor (open on the next client tick so the closing chat screen does not override it). `/wcc reset` and `/wcc version` stay.
- Mod Menu config button opens the editor with Mod Menu as parent screen.
- `EditModeScreen` and `ConfigScreen` are removed.

## 13. Testing & delivery

- Build 1.21.11 and 1.21.1 first (both sides of every Stonecutter condition), deliver jars to the
  user's Desktop `WCC-jars/` with the version in the file name, verify the delivered file's hash.
- The user tests in-game; **nothing is pushed until the user approves**.
- After approval of those two, build and deliver all 12 versions.
- Pure logic (theme interpolation, config migration, snap math) gets JUnit tests.

## 14. Calls made by Claude (not discussed with the user — check these)

- The editor opens with the **Layout** tab active and **no popover open**, so the first thing
  you see is the HUD and the toolbar.
- **Show HUD** toggle added to the Counter tab (it existed in the old config).
- The low-charge **"!" appears only on nametags**; on the HUD the number just turns the Low color.
- Corner handles are visual only in v1; resizing is scroll / slider / −+.
- The editor chrome follows the theme's accent color.
- Theme preset colors in section 8 are starting values, tuned after the first in-game test.

## 15. Left to the builder

Exact pixel metrics and spacing (follow the mockups), widget class structure, the curated
palette for the color picker, popover animation (if any), how the hex field handles input on
each version, and the internal input model.
