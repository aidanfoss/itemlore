
# ItemLore

[![License](https://img.shields.io/github/license/aidanfoss/itemlore.svg)](http://www.gnu.org/licenses/lgpl-3.0.html) [![Issues](https://img.shields.io/github/issues/aidanfoss/itemlore.svg)](https://github.com/aidanfoss/itemlore/issues) [![MC Versions](http://cf.way2muchnoise.eu/versions/For%20MC_itemlore_all.svg)](https://curseforge.com/minecraft/mc-mods/itemlore) [![CurseForge](https://cf.way2muchnoise.eu/full_itemlore_downloads.svg)](https://curseforge.com/minecraft/mc-mods/itemlore) [![Modrinth Downloads](https://img.shields.io/modrinth/dt/itemlore?logo=modrinth)](https://modrinth.com/mod/itemlore)

## Overview
ItemLore is a **Fabric** mod that adds customizable lore and stat‑tracking capabilities to anvilled (and potentially any held, when the command is called) items. Players can enable the feature and apply dynamic information directly into items, which track usage statistics, and players can view these statistics via commands.

## Features
- **Lore** – Add dynamic information to item tooltips
- **Stat Tracking** – Record per‑tool statistics
- **Commands** – `/stats ___`, with tab‑completion.
- **Configurable** – JSON configuration for default lore templates and stat behaviours.
- **Safe** – No data loss, no data corruption, fully vanilla-safe and can be added and removed without any issues.

## Installation
1. Download the latest `itemlore-*.jar` from **CurseForge** or **Modrinth**.
2. Place the JAR into your `mods/` folder.
3. Launch Minecraft with the Fabric loader.

## Usage
- **Toggle ItemLore**:
  ```
  /toggleItemLore
  ```
- **Give an item lore**:
  ```
  /applylore
  ```
- **View stats**:
  ```
  /stats all   # Show global stats
  /stats blocks  # Show block stats
  /stats mobs  # Show mob stats
  ```

## Building from Source
```bash
# Clone the repository
git clone https://github.com/aidanfoss/itemlore.git
cd itemlore

# Build the mod
./gradlew build
```
The compiled JAR will appear in `build/libs/`.

## Contributing
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request.

## License
This project is licensed under the **LGPL‑3.0**. See the [LICENSE](http://www.gnu.org/licenses/lgpl-3.0.html) file for details.

