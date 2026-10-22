# WCC Editor & Theme Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the edit mode and config screen with one custom-drawn, themeable floating-toolbar editor, theme the HUD, and give nametags a pixel-icon suffix — identically on Minecraft 1.21–1.21.11.

**Architecture:** Pure-Java logic (themes, color math, snapping, popover placement, suffix formatting, config migration) lives in MC-free classes with JUnit tests. A small custom widget kit draws only with `fill` / `drawTextWithShadow` / `drawItem` and receives input through one internal model; the editor `Screen` is the only place with version-conditional input code (Stonecutter `//? if >=1.21.9`).

**Tech Stack:** Fabric Loader, Fabric API, Yarn, Stonecutter 0.7.11, Loom 1.14.10, Gradle 9.2.1, Java 21, JUnit 5, m4lib `registerModSuffix`.

**Spec:** `docs/superpowers/specs/2026-10-22-wcc-editor-design.md`

## Global Constraints

- Every UI string is English. Delete `lang/tr_tr.json`.
- Draw only with `DrawContext.fill`, `drawTextWithShadow`, `drawItem` and the matrix helpers (`push/pop/translate/scale`, Stonecutter-split at `>=1.21.6`).
- Every color passed to drawing is ARGB with alpha set (`0xFF……` unless deliberately translucent).
- Version-specific input (`Click`, `KeyInput`, `CharInput`) only inside `EditorScreen`, behind `//? if >=1.21.9`.
- No new player-facing dependency (no Cloth Config / YACL / owo).
- Counting logic (`WindChargeTracker`, `ClientPlayNetworkHandlerMixin`) is not changed.
- Scale range 0.5–3.0, step 0.1. Snap threshold 6 px. Snap-to-anchor margin 4 px. Toolbar gap below HUD 12 px. Low-warning default 16, range 0–64 (0 = off).
- Build check for every task that touches MC code: `MC_ONLY=1.21.11 ./gradlew --no-daemon :1.21.11:build :1.21.1:build` → `BUILD SUCCESSFUL`.
- Unit tests: `MC_ONLY=1.21.11 ./gradlew --no-daemon :1.21.11:test` → `BUILD SUCCESSFUL`, 0 failures.
- Never push. Commit locally only; the user tests in-game first.

## Review Focus

1. High GUI scale / small window (854×480): popovers and toolbar must stay fully on screen (flip above the HUD, clamp horizontally) → `PopoverPlacementTest` in Task 3.
2. Cancel must restore every setting changed in the session, including colors and nametag options → `ModConfigTest.copyIsDeepAndRestorable` in Task 2.
3. Hex input like `"#b8a6ff"`, `"B8A6FF"`, `"fff"`, `"zzzzzz"`, `""` → accept 6-digit with/without `#` in any case, reject the rest without changing the color → `ColorMathTest.parseHex*` in Task 1.
4. Old `windchargecounter.json` (has `backgroundOpacity: 160`, no theme fields) or a corrupt file → loads with migrated opacity 63 % and Wind defaults, never crashes → `ConfigMigrationTest` in Task 2.
5. Counts outside 0–128 (holding 140 charges, or a negative from a reset race) → colors clamp, suffix still renders, `/128` still appended → `ColorMathTest.countColorClamps` (Task 1) and `SuffixFormatTest.countAbove128` (Task 4).

---

### Task 1: Test setup, themes and color math

**Files:**
- Modify: `build.gradle.kts` (add JUnit)
- Create: `src/main/java/com/m4ssive/windchargecounter/theme/Theme.java`
- Create: `src/main/java/com/m4ssive/windchargecounter/theme/Themes.java`
- Create: `src/main/java/com/m4ssive/windchargecounter/theme/ColorMath.java`
- Test: `src/test/java/com/m4ssive/windchargecounter/theme/ColorMathTest.java`, `ThemesTest.java`

**Interfaces:**
- Produces: `record Theme(String id, String name, int accent, int text, int panel, int full, int half, int low)` (RGB, no alpha); `Themes.ALL: List<Theme>` in order Wind, Breeze, Night, Ember, Mono; `Themes.byId(String): Theme` (unknown → Wind); `Themes.DEFAULT_ID = "wind"`.
- Produces: `ColorMath.argb(int rgb, int alpha0to255): int`, `ColorMath.lerp(int rgbA, int rgbB, float t): int`, `ColorMath.countColor(int remaining, int max, int full, int half, int low): int` (RGB), `ColorMath.parseHex(String): OptionalInt`, `ColorMath.toHex(int rgb): String` (6 upper-case digits, no `#`).

- [ ] **Step 1:** In `build.gradle.kts` add `testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")`, `testRuntimeOnly("org.junit.platform:junit-platform-launcher")` and `tasks.test { useJUnitPlatform() }`.
- [ ] **Step 2: Write failing tests**

```java
// ThemesTest
assertEquals(List.of("wind","breeze","night","ember","mono"), Themes.ALL.stream().map(Theme::id).toList());
Theme b = Themes.byId("breeze");
assertEquals(0xB8A6FF, b.accent()); assertEquals(0x7FF0C8, b.full()); assertEquals(0xF2C46D, b.half()); assertEquals(0xFF6A9A, b.low());
assertEquals("wind", Themes.byId("nope").id());
// ColorMathTest
assertEquals(0xFF5FE36A, ColorMath.argb(0x5FE36A, 255));
assertEquals(0x5FE36A, ColorMath.countColor(128, 128, 0x5FE36A, 0xD9E04A, 0xFF6A4A));
assertEquals(0xD9E04A, ColorMath.countColor(64, 128, 0x5FE36A, 0xD9E04A, 0xFF6A4A));
assertEquals(0xFF6A4A, ColorMath.countColor(0, 128, 0x5FE36A, 0xD9E04A, 0xFF6A4A));
// countColorClamps
assertEquals(0x5FE36A, ColorMath.countColor(140, 128, 0x5FE36A, 0xD9E04A, 0xFF6A4A));
assertEquals(0xFF6A4A, ColorMath.countColor(-3, 128, 0x5FE36A, 0xD9E04A, 0xFF6A4A));
// parseHex*
assertEquals(OptionalInt.of(0xB8A6FF), ColorMath.parseHex("#b8a6ff"));
assertEquals(OptionalInt.of(0xB8A6FF), ColorMath.parseHex("B8A6FF"));
assertTrue(ColorMath.parseHex("fff").isEmpty());
assertTrue(ColorMath.parseHex("zzzzzz").isEmpty());
assertTrue(ColorMath.parseHex("").isEmpty());
assertEquals("0B0F17", ColorMath.toHex(0x0B0F17));
```

- [ ] **Step 3:** Run `MC_ONLY=1.21.11 ./gradlew --no-daemon :1.21.11:test` → FAIL (classes missing).
- [ ] **Step 4:** Implement. Theme values exactly as spec §8:

| id | name | accent | text | panel | full | half | low |
|---|---|---|---|---|---|---|---|
| wind | Wind | 8BE9FF | EAF2FF | 0B0F17 | 5FE36A | D9E04A | FF6A4A |
| breeze | Breeze | B8A6FF | EAF2FF | 0E0B1E | 7FF0C8 | F2C46D | FF6A9A |
| night | Night | 5B8CFF | EAF2FF | 0A0F24 | 8BE9FF | FFD25A | FF6A9A |
| ember | Ember | FF9A4A | FFF1E6 | 1A0F0A | FFE08A | FFB86A | E5484D |
| mono | Mono | D6DCE6 | EAF2FF | 101216 | EAEFF6 | B4BCC8 | FF6A6A |

`countColor`: ratio = clamp(remaining/max, 0, 1); ratio ≥ 0.5 → lerp(half, full, (ratio−0.5)·2); else lerp(low, half, ratio·2). No MC imports in this package.
- [ ] **Step 5:** Run tests → PASS.
- [ ] **Step 6:** Commit `feat: add themes and color math`.

---

### Task 2: Config v2 with migration and snapshot

**Files:**
- Modify: `src/main/java/com/m4ssive/windchargecounter/config/ModConfig.java`
- Create: `src/main/java/com/m4ssive/windchargecounter/config/ConfigMigration.java`
- Test: `src/test/java/com/m4ssive/windchargecounter/config/ModConfigTest.java`, `ConfigMigrationTest.java`

**Interfaces:**
- Consumes: `Themes`, `Theme` (Task 1).
- Produces on `ModConfig` (public fields): existing `hudX, hudY, scale, enableHud, showSelf, showHeader, showBars, flashOnThrow, showInNametags, autoResetOnDeath`; new `String themeId = "wind"`, `boolean customTheme = false`, `int accentColor, textColor, panelColor, fullColor, halfColor, lowColor` (RGB, Wind defaults), `boolean colorByCharges = true`, `int panelOpacity = 63` (percent), `boolean snapToEdges = true`, `boolean nametagIcon = true`, `boolean nametagShowMax = false`, `boolean nametagOwn = true`, `int lowWarning = 16`, `String nametagSeparator = "|"` (one of `"|"`, `"-"`, `"[]"`, `""`). Remove `backgroundOpacity` field.
- Produces methods: `ModConfig copy()`, `void copyFrom(ModConfig other)` (all fields), `void applyTheme(Theme t)` (sets themeId, six colors, `customTheme=false`), `void markCustom()`, `void sanitize()` (clamps scale 0.5–3.0, panelOpacity 0–100, lowWarning 0–64, unknown separator → `"|"`, unknown themeId → `"wind"`), `load()`, `save()`.
- Produces: `ConfigMigration.migrate(JsonObject raw, ModConfig target)`: if `raw` has `backgroundOpacity` and lacks `panelOpacity` → `panelOpacity = round(backgroundOpacity / 2.55)`; if `raw` lacks `accentColor` → `applyTheme(Themes.byId(target.themeId))`.

- [ ] **Step 1: Write failing tests**

```java
// ModConfigTest.copyIsDeepAndRestorable
ModConfig a = new ModConfig(); ModConfig snap = a.copy();
a.applyTheme(Themes.byId("ember")); a.scale = 2.0f; a.nametagSeparator = "-"; a.lowWarning = 5; a.hudX = 50;
a.copyFrom(snap);
assertEquals("wind", a.themeId); assertEquals(1.0f, a.scale); assertEquals("|", a.nametagSeparator); assertEquals(16, a.lowWarning); assertEquals(ModConfig.UNSET, a.hudX);
// sanitize
a.scale = 9f; a.panelOpacity = 300; a.lowWarning = -1; a.nametagSeparator = "?"; a.themeId = "x"; a.sanitize();
assertEquals(3.0f, a.scale); assertEquals(100, a.panelOpacity); assertEquals(0, a.lowWarning); assertEquals("|", a.nametagSeparator); assertEquals("wind", a.themeId);
// ConfigMigrationTest.oldFileMigrates
JsonObject raw = JsonParser.parseString("{\"backgroundOpacity\":160,\"scale\":1.5}").getAsJsonObject();
ModConfig c = new Gson().fromJson(raw, ModConfig.class); ConfigMigration.migrate(raw, c);
assertEquals(63, c.panelOpacity); assertEquals(1.5f, c.scale); assertEquals(0x8BE9FF, c.accentColor);
```

- [ ] **Step 2:** Run tests → FAIL.
- [ ] **Step 3:** Implement. Move `CONFIG_PATH` out of the static initializer into a private method called by `load()`/`save()` so `new ModConfig()` never touches `FabricLoader` (tests construct it). `load()`: parse to `JsonObject`; on any exception log and keep defaults; then `copyFrom(gson result)`, `ConfigMigration.migrate`, `sanitize`.
- [ ] **Step 4:** Run tests → PASS. Run the build check → `BUILD SUCCESSFUL` (fix callers of the removed `backgroundOpacity`: `WindChargeHud` uses `panelOpacity * 255 / 100`; `ConfigScreen` is deleted in Task 7, so for now replace its opacity button body with `panelOpacity` cycling 0/25/50/63/80/100).
- [ ] **Step 5:** Commit `feat: config v2 with themes, migration and snapshot`.

---

### Task 3: Snapping, anchors and popover placement

**Files:**
- Create: `src/main/java/com/m4ssive/windchargecounter/editor/layout/Snapping.java`
- Create: `src/main/java/com/m4ssive/windchargecounter/editor/layout/PopoverPlacement.java`
- Test: `src/test/java/com/m4ssive/windchargecounter/editor/layout/SnappingTest.java`, `PopoverPlacementTest.java`

**Interfaces:**
- Produces: `record SnapResult(int x, int y, Integer guideX, Integer guideY)`; `Snapping.snap(int x, int y, int w, int h, int screenW, int screenH, int threshold): SnapResult` — candidates per axis: left edge 0, right edge screen−w, center (screen−w)/2; nearest within `threshold` wins, guide = the screen line it aligned to (0, screenW, screenW/2); none → null guide. Result always clamped into the screen.
- Produces: `Snapping.anchor(int col, int row, int w, int h, int screenW, int screenH, int margin): int[]{x, y}` for col,row ∈ {0,1,2}.
- Produces: `record Placement(int x, int y, boolean above, int caretX)`; `PopoverPlacement.place(int anchorX, int anchorTop, int anchorBottom, int w, int h, int screenW, int screenH, int gap, int targetCenterX): Placement` — below if `anchorBottom+gap+h ≤ screenH−4`, else above (`y = anchorTop−gap−h`); x = clamp(anchorX, 4, screenW−w−4); caretX = clamp(targetCenterX, x+10, x+w−10).

- [ ] **Step 1: Write failing tests**

```java
SnapResult r = Snapping.snap(5, 300, 100, 60, 1280, 720, 6);
assertEquals(0, r.x()); assertEquals(0, r.guideX()); assertNull(r.guideY());
r = Snapping.snap(593, 3, 100, 60, 1280, 720, 6);           // center x = 590
assertEquals(590, r.x()); assertEquals(640, r.guideX()); assertEquals(0, r.y());
r = Snapping.snap(1300, 800, 100, 60, 1280, 720, 6);       // off-screen → clamped
assertEquals(1180, r.x()); assertEquals(660, r.y());
assertArrayEquals(new int[]{1176, 4}, Snapping.anchor(2, 0, 100, 60, 1280, 720, 4));
// PopoverPlacementTest: small screen flips above and clamps
Placement p = PopoverPlacement.place(700, 300, 400, 392, 260, 854, 480, 12, 820);
assertTrue(p.above()); assertEquals(28, p.y()); assertEquals(458, p.x()); assertEquals(820, p.caretX());
p = PopoverPlacement.place(100, 50, 150, 392, 200, 1280, 720, 12, 130);
assertFalse(p.above()); assertEquals(162, p.y()); assertEquals(100, p.x());
```

- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement (pure Java). **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat: snapping and popover placement`.

---

### Task 4: Nametag suffix with pixel icon

**Files:**
- Create: `src/main/java/com/m4ssive/windchargecounter/nametag/SuffixFormat.java` (pure)
- Create: `src/main/java/com/m4ssive/windchargecounter/nametag/NametagSuffix.java` (builds `Text`)
- Create: `src/main/resources/assets/windchargecounter/font/icons.json`
- Create: `src/main/resources/assets/windchargecounter/textures/font/wind_icon.png` (8×8, RGBA)
- Modify: `WindChargeCounterMod.java` (`buildNametagSuffix` delegates to `NametagSuffix.build`)
- Test: `src/test/java/com/m4ssive/windchargecounter/nametag/SuffixFormatTest.java`

**Interfaces:**
- Consumes: `ModConfig` fields `nametagSeparator, nametagIcon, nametagShowMax, colorByCharges, lowWarning, accentColor, full/half/lowColor` (Task 2); `ColorMath.countColor` (Task 1).
- Produces: `record Segments(String separator, boolean icon, String count, boolean warning)`; `SuffixFormat.segments(int remaining, int max, String separator, boolean icon, boolean showMax, int lowWarning): Segments` — separator `"[]"` renders as `"["` before and `"]"` after the count (encode as separator `"["` and append `"]"` to count); `""` → no separator; warning = lowWarning > 0 && remaining ≤ lowWarning.
- Produces: `NametagSuffix.build(int remaining, ModConfig cfg): Text` — `" " + sep(gray 0xAAAAAA) + " " + icon + " " + count(colored) + "!"(low color if warning)`. Icon char `` styled with font id `windchargecounter:icons`:

```java
//? if >=1.21.9 {
/*style.withFont(new net.minecraft.text.StyleSpriteSource.Font(Identifier.of("windchargecounter", "icons")))
*///?} else {
style.withFont(Identifier.of("windchargecounter", "icons"))
//?}
```

- [ ] **Step 1: Write failing tests**

```java
assertEquals(new Segments("|", true, "74", false), SuffixFormat.segments(74, 128, "|", true, false, 16));
assertEquals(new Segments("", false, "9", true), SuffixFormat.segments(9, 128, "", false, false, 16));
assertEquals(new Segments("-", true, "16/128", true), SuffixFormat.segments(16, 128, "-", true, true, 16));
assertEquals(new Segments("[", true, "74]", false), SuffixFormat.segments(74, 128, "[]", true, false, 16));
assertFalse(SuffixFormat.segments(0, 128, "|", true, false, 0).warning());
// countAbove128
assertEquals("140/128", SuffixFormat.segments(140, 128, "|", true, true, 16).count());
```

- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement `SuffixFormat`, then `NametagSuffix`.
- [ ] **Step 4:** Font provider `icons.json`: `{"providers":[{"type":"bitmap","file":"windchargecounter:font/wind_icon.png","ascent":7,"height":8,"chars":[""]}]}`. Draw `wind_icon.png` from the mockup's 8×8 pixel swirl (`Nametags.dc.html` SVG path) in `#8BE9FF` on transparent (generate with a short Python/PIL script outside the repo, commit only the PNG).
- [ ] **Step 5:** Run tests → PASS. Build check, plus `MC_ONLY=1.21.8,1.21.9 ./gradlew --no-daemon :1.21.8:build :1.21.9:build` → `BUILD SUCCESSFUL` (proves the `>=1.21.9` font boundary; if 1.21.9 fails to compile, move the boundary to the first version that compiles and record it in the commit message).
- [ ] **Step 6:** Commit `feat: nametag suffix with pixel wind icon`.

---

### Task 5: UI kit (drawing primitives and widgets)

**Files:**
- Create: `src/main/java/com/m4ssive/windchargecounter/ui/Draw.java` — move `push/pop/translate/scale` from `WindChargeHud` here (public static); add `rect(ctx,x,y,w,h,argb)`, `border(ctx,x,y,w,h,thickness,argb)`, `dashedV(ctx,x,y1,y2,argb)`, `dashedH(ctx,y,x1,x2,argb)`, `caretUp(ctx,cx,y,size,argb)` / `caretDown(...)` (stacked 1-px fills), `text(ctx,String,x,y,argb)`, `textWidth(String)`.
- Create: `ui/Widget.java` — abstract: `int x,y,w,h`; `render(DrawContext ctx, int mouseX, int mouseY, UiTheme t)`; input defaults returning false: `mouseClicked(double,double,int)`, `mouseDragged(double,double,int)`, `mouseReleased(double,double,int)`, `mouseScrolled(double,double,double)`, `keyPressed(int key, int mods)`, `charTyped(String s)`; `boolean contains(double,double)`; `boolean focused`.
- Create: `ui/UiTheme.java` — `record UiTheme(int accent, int text, int muted, int surface, int surfaceAlt, int line)`; `static UiTheme from(ModConfig c)`: accent = c.accentColor, text = c.textColor, muted 0x8FA3C0, surface 0x0D0E22, surfaceAlt 0x15172E, line 0x2A2E55 (all as ARGB 0xFF).
- Create widgets: `ui/Button.java` (label, primary/ghost, `Runnable onPress`), `ui/Toggle.java` (label + 40×20 switch, `BooleanSupplier get`, `Consumer<Boolean> set`), `ui/Slider.java` (label, value label `Function<Float,String>`, min,max,step, getter/setter; `static float valueFromMouse(double mouseX, int left, int width, float min, float max, float step)`), `ui/Swatch.java` (color getter, selected flag, onPress), `ui/HexField.java` (6-char input, Enter/blur commits via `ColorMath.parseHex`, invalid → red border, value unchanged), `ui/Segmented.java` (options list, selected index, onSelect).
- Test: `src/test/java/com/m4ssive/windchargecounter/ui/SliderMathTest.java`

**Interfaces:**
- Consumes: `ColorMath`, `ModConfig`.
- Produces: every class/method above; later tasks build popovers from these widgets only.

- [ ] **Step 1: Write failing test**

```java
assertEquals(0.5f, Slider.valueFromMouse(10, 10, 250, 0.5f, 3.0f, 0.1f), 1e-6);
assertEquals(3.0f, Slider.valueFromMouse(999, 10, 250, 0.5f, 3.0f, 0.1f), 1e-6);
assertEquals(1.8f, Slider.valueFromMouse(140, 10, 250, 0.5f, 3.0f, 0.1f), 1e-6);
assertEquals(16f, Slider.valueFromMouse(41, 10, 124, 0f, 64f, 1f), 1e-6);
```

- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement `valueFromMouse` (clamp, round to step) and the widgets. Visual metrics from `Toolbar.dc.html`/`Nametags.dc.html`: toggle 40×20 with 14×14 knob, slider track 4 px with 8×16 knob, swatch borders 2 px, buttons 30 px tall, text 9-px MC font.
- [ ] **Step 4:** Run test → PASS; build check → `BUILD SUCCESSFUL`.
- [ ] **Step 5:** Commit `feat: custom UI kit`.

---

### Task 6: Themed HUD

**Files:**
- Modify: `src/main/java/com/m4ssive/windchargecounter/gui/WindChargeHud.java`

**Interfaces:**
- Consumes: `Draw` (Task 5), `ColorMath`, `ModConfig` colors (Task 2).
- Produces: `WindChargeHud.render(DrawContext ctx, boolean editing)` (remove `editMode` state and all `handleMouse*`/`handleScroll` methods); `int[] bounds()` → `{x, y, w, h}` of the last drawn panel in screen pixels; `int[] measure(MinecraftClient c, boolean editing)` → `{w, h}` scaled size for the current rows (used by snapping before drawing). When `editing` and there is no real data, the rows are the preview `You 112`, `Enemy 74`, `FarAway 23`.

- [ ] **Step 1:** Replace hard-coded colors: header icon + title use accent (title at 85 % mix toward text), own name accent, opponent names text (out-of-range: text at 55 % alpha), numbers/stripe/bars `colorByCharges ? countColor(...) : accent`, bar background = panel mixed 20 % toward text, panel = `argb(panelColor, panelOpacity*255/100)`. Edit-state outline (2 px accent, offset 3) and four 8×8 handles when `editing`.
- [ ] **Step 2:** Update callers: the `HudRenderCallback` in `WindChargeCounterMod` calls `hud.render(context, false)`; the old `EditModeScreen` calls `render(ctx, true)` until Task 7 deletes it.
- [ ] **Step 3:** Build check → `BUILD SUCCESSFUL`.
- [ ] **Step 4:** Commit `feat: theme-driven HUD`.

---

### Task 7: Editor screen shell and entry points

**Files:**
- Create: `src/main/java/com/m4ssive/windchargecounter/editor/EditorScreen.java`
- Create: `src/main/java/com/m4ssive/windchargecounter/editor/TopBar.java`
- Delete: `gui/EditModeScreen.java`, `gui/ConfigScreen.java`, `lang/tr_tr.json`
- Modify: `WindChargeCounterMod.java`, `ModMenuIntegration.java`, `lang/en_us.json`

**Interfaces:**
- Consumes: `WindChargeHud.render/bounds/measure` (Task 6), `Snapping` (Task 3), `ModConfig.copy/copyFrom/save` (Task 2), `Widget` (Task 5).
- Produces: `new EditorScreen(Screen parent)`; `enum Tab { LAYOUT, COUNTER, NAMETAG, THEME }`; `void openTab(Tab t)` (Task 8/9 attach popovers); `TopBar(Consumer<Tab> onTab, Runnable onCancel, Runnable onDone)`.

- [ ] **Step 1:** `EditorScreen`: on construct take `snapshot = config.copy()`; `shouldPause() false`; background = `fill(0,0,w,h, 0x610A1020)`; render order: background → HUD `render(ctx, true)` → snap guides (dashed accent) while dragging → toolbar/popover (Task 8) → `TopBar` centered 18 px from top → hint bar bottom center `Click select · Drag move · Scroll resize · Arrows nudge 1px`.
- [ ] **Step 2:** Input adapters: override the version-specific `mouseClicked/mouseReleased/mouseDragged/keyPressed/charTyped` behind `//? if >=1.21.9 {` (`Click.x()/y()/button()`, `KeyInput.key()/modifiers()`, `CharInput.asString()`) `//?} else {` (`double,double,int`, `int,int,int`, `char,int`) and forward to private `onClick/onRelease/onDrag/onKey/onChar` using the internal model; `mouseScrolled(double,double,double,double)` is the same on all versions. Dispatch order: open popover → toolbar → top bar → HUD.
- [ ] **Step 3:** Behavior: click on HUD bounds starts drag; drag uses `Snapping.snap(..., 6)` unless Shift held or `!snapToEdges`; scroll over HUD changes scale ±0.1; arrows nudge 1 px (Shift 10 px); click on empty space closes the popover; `Esc`, the editor keybind, or Done → `config.save()` and `close()` to parent; Cancel → `config.copyFrom(snapshot)` then close.
- [ ] **Step 4:** Entry points: keybind label `"Open WCC Editor"` in `en_us.json`, category label unchanged; tick handler opens `new EditorScreen(null)` when no screen is open; `/wcc` with no args → `MinecraftClient.getInstance().send(() -> client.setScreen(new EditorScreen(null)))`; `ModMenuIntegration` returns `EditorScreen::new`. HUD render callback skips drawing while `currentScreen instanceof EditorScreen` (the editor draws it).
- [ ] **Step 5:** Build check → `BUILD SUCCESSFUL`. `grep -rn "EditModeScreen\|ConfigScreen" src` → no matches.
- [ ] **Step 6:** Commit `feat: editor screen shell, replaces edit mode and config screen`.

---

### Task 8: Toolbar, popover and Layout / Counter / Nametag tabs

**Files:**
- Create: `editor/Toolbar.java`, `editor/Popover.java`, `editor/tabs/LayoutTab.java`, `editor/tabs/CounterTab.java`, `editor/tabs/NametagTab.java`
- Modify: `editor/EditorScreen.java`

**Interfaces:**
- Consumes: widgets (Task 5), `PopoverPlacement` (Task 3), `Snapping.anchor` (Task 3), `EditorScreen.openTab` (Task 7), `WindChargeTracker.clearAll()`.
- Produces: `Toolbar.layout(int[] hudBounds, int screenW, int screenH)` (12 px below HUD, flips above when needed, clamps); `Popover(String title, List<Widget> rows, int width)` with `place(Placement)` and caret drawing; each tab: `static Popover create(ModConfig cfg, EditorScreen screen)`.

- [ ] **Step 1:** Toolbar (height 40, from `Toolbar.dc.html`): `−` / scale label / `+` │ theme chip (accent, full, half stripes + `customTheme ? "Custom" : theme name`) → `openTab(THEME)` │ `...` → opens the active tab.
- [ ] **Step 2:** Popover: width 392 (Theme) / 300 (others), surface `0xFA0D0E22`, 2 px border accent at 50 %, caret toward the opening button, placed with `PopoverPlacement.place(..., gap 12, ...)`.
- [ ] **Step 3:** Tab rows exactly as spec §7.3: Layout (Scale slider 0.5–3.0 step 0.1; Snap-to 3×3 grid via `Snapping.anchor(col,row,w,h,sw,sh,4)`; Snap to edges toggle; Reset position → `hudX = hudY = UNSET`). Counter (Show HUD, Header, Progress bars, Flash on throw, Show yourself, Reset on death toggles; Reset counters button). Nametag (Show on nametags, Wind icon, Show /128, Color by charges left, On my own nametag toggles; Low warning at slider 0–64 step 1; Separator segmented `|` `-` `[ ]` `none` → `"|","-","[]",""`).
- [ ] **Step 4:** Build check → `BUILD SUCCESSFUL`.
- [ ] **Step 5:** Commit `feat: floating toolbar and layout/counter/nametag popovers`.

---

### Task 9: Theme tab and color picker

**Files:**
- Create: `editor/tabs/ThemeTab.java`, `editor/ColorPicker.java`
- Modify: `editor/EditorScreen.java` (picker layering)

**Interfaces:**
- Consumes: `Themes`, `ModConfig.applyTheme/markCustom`, `Swatch`, `HexField`, `Toggle`, `Slider`, `Popover` (Tasks 1, 2, 5, 8).
- Produces: `ThemeTab.create(ModConfig, EditorScreen): Popover`; `ColorPicker(int initialRgb, IntConsumer onPick)` drawn above the popover, closed by click outside or Enter.

- [ ] **Step 1:** Theme popover rows (from `Toolbar.dc.html`): presets row of 5 cards (three stripes accent/full/low + name; selected = 2 px accent border; click → `applyTheme`); divider; "CUSTOM COLORS" Accent / Text / Panel swatch buttons with hex label → open `ColorPicker`, on pick set field + `markCustom()`; "COUNT COLORS" toggle `colorByCharges`, gradient bar Low→Half→Full (24 fills via `ColorMath.lerp`), Low / Half / Full swatches → picker; Panel opacity slider 0–100 %; Reset → `applyTheme(Themes.byId(themeId))`.
- [ ] **Step 2:** `ColorPicker`: 8×3 palette grid of curated colors (include all 30 preset colors from §8, deduplicated, plus white/black/greys) and a `HexField`; invalid hex keeps the old color.
- [ ] **Step 3:** Build check → `BUILD SUCCESSFUL`; run unit tests → PASS.
- [ ] **Step 4:** Commit `feat: theme popover and color picker`.

---

### Task 10: Build matrix and delivery

**Files:**
- Modify: `gradle.properties` (`mod_version=1.3.0`), `README.md` (badge, features list in English, editor screenshot placeholder removed).

- [ ] **Step 1:** `MC_ONLY=1.21.11 ./gradlew --no-daemon :1.21.11:build :1.21.1:build :1.21.11:test` → `BUILD SUCCESSFUL`.
- [ ] **Step 2:** Copy `WindChargeCounter-1.3.0+1.21.11.jar` and `…+1.21.1.jar` to `/mnt/user-data/outputs/jars/`, **wait for the copy to finish**, then commit them to the user's Desktop `WCC-jars/<version>/`; move older WindChargeCounter jars there into `WCC-jars/_eski/`; verify with `shasum` on the device that hashes match the local files.
- [ ] **Step 3:** Stop. Tell the user (in Turkish) what to test: `/wcc version` shows 1.3.0, K opens the editor, each tab, theme switching, custom colors and hex input, Cancel restores, nametag suffix states (full / half / ≤16). Do not push.
- [ ] **Step 4 (after the user approves):** build all 12 versions — `./gradlew --no-daemon build` with no `MC_ONLY` (m4lib must first `publishToMavenLocal` for all 12) — deliver each jar to `WCC-jars/<version>/` with hash check.
- [ ] **Step 5:** Commit `chore: release 1.3.0`.
