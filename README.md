# Kulka Better Cooldowns

A Fabric client-side mod for Minecraft **1.21.11** that displays item cooldowns directly on your HUD — so you always know when your ender pearl, chorus fruit, or other items are ready to use again.

## Features

- Shows active cooldowns for all items in your inventory on the HUD
- Displays cooldown as **percentage** or **seconds** (configurable)
- Fully **customizable HUD position** (X/Y)
- Adjustable **scale**
- Can be toggled on/off without restarting
- Supports **ModMenu** for in-game config screen

## Preview

> Item icon + remaining cooldown displayed in a clean overlay on screen.

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.18.4 |
| Fabric API | any |
| [YACL](https://modrinth.com/mod/yacl) | 3.8.2+1.21.11-fabric |
| [ModMenu](https://modrinth.com/mod/modmenu) *(optional)* | 17.0.0 |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download and place **Fabric API** and **YACL** into your `mods/` folder
3. Drop the mod `.jar` into your `mods/` folder
4. Launch Minecraft 1.21.11

## Configuration

Open **Mod Menu → Kulka Better Cooldowns** to configure:

- **Enable Mod** — toggle the HUD on/off
- **HUD X / HUD Y** — position of the cooldown display on screen
- **Scale** — size of the display (0.5× to 3×)
- **Use Seconds** — show remaining time in seconds instead of percentage

Config is saved to `.minecraft/config/kulka-bettercooldowns.json`.

## License

MIT — see [LICENSE.txt](LICENSE.txt)
