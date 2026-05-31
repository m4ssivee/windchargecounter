# WindChargeCounter

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green.svg)
![Fabric](https://img.shields.io/badge/mod%20loader-fabric-1976d2?logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJMMTMuMDkgOC4yNkwyMCA5TDEzLjA5IDE1Ljc0TDEyIDIyTDEwLjkxIDE1Ljc0TDQgOUwxMC45MSA4LjI2TDEyIDJaIiBmaWxsPSIjMTk3NkQyIi8+Cjwvc3ZnPg==)
![Java](https://img.shields.io/badge/java-21-orange.svg)

A highly customizable, client-side utility mod designed for Mace PvP that tracks and displays remaining Wind Charges for both you and your opponents.

</div>

## Overview

**WindChargeCounter** is a lightweight Fabric mod created specifically for the competitive PvP community. It provides a real-time count of how many Wind Charges (out of the standard 128 maximum) you and your opponent have left during a duel. 

This mod helps players maintain better situational awareness without having to manually keep track of the opponent's item usage.

## Features

- **Interactive HUD:** Displays a sleek, customizable tracker on your screen showing the remaining wind charges.
- **Edit Mode:** Press `K` in-game to enter Edit Mode. You can freely drag the HUD anywhere on your screen and use the corner handles to resize it to your preference.
- **Nametag Integration:** The wind charge count is seamlessly integrated into player nametags, so you can easily see an opponent's status during fast-paced combat.
- **Automatic Match Tracking:** The mod automatically detects when a new round starts (e.g., when an inventory refill occurs) and resets the counters accordingly.
- **Manual Reset Command:** You can use `/wcc reset` to clear and reset the tracking data manually at any time.
- **Extensive Customization:** Full support for `ModMenu`, allowing you to tweak visual preferences easily from the configuration menu.

## Important Information

> **Note:** This is a **client-side only** modification. It works by observing item usage (throwing wind charges) in your render distance to calculate the remaining amount. It does not require any server-side plugins to function.

## Installation Requirements

1. Download the latest release.
2. Install [Fabric Loader](https://fabricmc.net/use/) (for Minecraft 1.21+) and the [Fabric API](https://modrinth.com/mod/fabric-api).
3. **Required Dependency:** You must install [m4lib](https://github.com/m4ssivee/m4lib) for the mod to work.
4. Place the `.jar` files into your `.minecraft/mods` folder.
5. *(Optional but recommended)* Install [Mod Menu](https://modrinth.com/mod/modmenu) to access the configuration screen.

## Building from source

Requirements:
- Java 21+

```bash
git clone https://github.com/m4ssivee/windchargecounter.git
cd windchargecounter
./gradlew build
```
The compiled jar will be located in `build/libs/`.

---

**License:** MIT  
**Author:** m4ssivee  
