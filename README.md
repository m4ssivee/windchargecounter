# WindChargeCounter

<div align="center">

![Version](https://img.shields.io/badge/version-1.3.1-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21%E2%80%931.21.11-green.svg)
![Fabric](https://img.shields.io/badge/mod%20loader-fabric-1976d2?logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJMMTMuMDkgOC4yNkwyMCA5TDEzLjA5IDE1Ljc0TDEyIDIyTDEwLjkxIDE1Ljc0TDQgOUwxMC45MSA4LjI2TDEyIDJaIiBmaWxsPSIjMTk3NkQyIi8+Cjwvc3ZnPg==)
![Java](https://img.shields.io/badge/java-21-orange.svg)

A highly customizable, client-side utility mod designed for Mace PvP that tracks and displays remaining Wind Charges for both you and your opponents.

</div>

## Overview

**WindChargeCounter** is a lightweight Fabric mod created specifically for the competitive PvP community. It provides a real-time count of how many Wind Charges (out of the standard 128 maximum) you and your opponent have left during a duel. 

This mod helps players maintain better situational awareness without having to manually keep track of the opponent's item usage.

## Features

- **Themed HUD:** A compact panel with a row per player, a progress bar and a flash when someone throws. Opponents stay listed (dimmed) when they leave render distance.
- **WCC Editor:** Press `K` (rebindable), type `/wcc`, or use Mod Menu to open one editor for everything. Drag the HUD with edge/center snapping, scroll to resize, nudge with the arrow keys, and change every setting from the Layout / Counter / Nametag / Theme tabs. `Esc`/`Done` saves, `Cancel` reverts.
- **Themes:** Five presets (Wind, Breeze, Night, Ember, Mono) plus custom Accent / Text / Panel colors, Low / Half / Full count colors (palette or hex) and panel opacity.
- **Nametag Integration:** `Enemy | ◉ 74` suffix with a pixel wind-charge icon. Counts turn red with a `!` when an opponent is low (threshold configurable). Separator, icon, `/128` and own-nametag display are configurable.
- **Automatic Match Tracking:** The mod automatically detects when a new round starts (e.g., when an inventory refill occurs) and resets the counters accordingly.
- **Commands:** `/wcc` opens the editor, `/wcc reset` clears the counters, `/wcc version` prints the installed version.
- **Every 1.21 version:** Separate builds for Minecraft 1.21 through 1.21.11.

## Important Information

> **Note:** This is a **client-side only** modification. It works by observing item usage (throwing wind charges) in your render distance to calculate the remaining amount. It does not require any server-side plugins to function.

## Installation Requirements

1. Download the latest release.
2. Install [Fabric Loader](https://fabricmc.net/use/) (Minecraft 1.21 – 1.21.11; pick the jar matching your game version) and the [Fabric API](https://modrinth.com/mod/fabric-api).
3. **Required Dependency:** You must install [m4lib](https://github.com/m4ssivee/m4lib) for the mod to work.
4. Place the `.jar` files into your `.minecraft/mods` folder.
5. *(Optional but recommended)* Install [Mod Menu](https://modrinth.com/mod/modmenu) as another way to open the editor.

## Building from source

Requirements:
- Java 21+

```bash
git clone https://github.com/m4ssivee/windchargecounter.git
cd windchargecounter
# m4lib must be in mavenLocal first (clone m4lib, then: ./gradlew publishToMavenLocal)
./gradlew build                                   # all versions
MC_ONLY=1.21.11 ./gradlew :1.21.11:build          # one version
```
Each version's jar is in `versions/<minecraft version>/build/libs/`.

---

**License:** MIT  
**Author:** m4ssivee  
