# HeadDrops Plugin

A Folia-compatible Minecraft plugin for Minecraft 1.21.8 that drops player heads upon death with custom lore including timestamp and killer information.

## Features

- Drops player heads on death with persistent custom lore
- Configurable drop chance and serial numbers to prevent duping
- Serial numbers in random or incremental modes, visible or invisible
- Folia region-aware for performance
- Customizable lore templates

## Building and Releasing

To build locally:
```bash
./gradlew build
```

The plugin JAR will be in `build/libs/`.


## Configuration

Edit `config.yml` to customize:

- Drop requirements (PvP only or all deaths)
- Drop chance percentage
- Serial number settings (enabled, mode, visibility)
- Lore templates and formatting

## License

Created by @BaconCat1  
Copyright (C) 2025 BaconCat1  
Licensed under the GNU General Public License v3.0  
See [LICENSE](LICENSE) for details.