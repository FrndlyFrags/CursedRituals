# ItemRituals Plugin

A Minecraft Bukkit/Paper plugin that adds mystical ritual events to your server. Players can select powerful items from a pool, start rituals that summon a reward item, and compete to collect it before time runs out.

## Features

### Core Features
- **Ritual System**: Start rituals with items from a configurable pool
- **Dynamic BossBar**: Shows remaining time and ritual details to nearby players
- **Atmospheric Effects**: Lightning, particles, and ambient sounds enhance the ritual experience
- **Item Protection**: Ritual items are protected from pick-up, explosions, fire, and despawning

### Special Features
- **Obsidian Cage**: A 30×30×33 obsidian cage forms around the ritual in the final minutes, creating a fully enclosed area
- **Customizable GUI**: Player-friendly interface for managing the ritual item pool
- **Announcements**: Server-wide or local announcements for ritual events
- **Location Randomization**: Find safe, appropriate locations for each ritual

## Obsidian Cage

The obsidian cage is a 3D structure that forms around the ritual location in the final minutes. It is:
- **30 blocks wide × 30 blocks deep × 33 blocks tall** (3 blocks below ritual level)
- **Breakable**: Players can mine the obsidian blocks
- **Auto-repairing**: Broken blocks are automatically replaced
- **Auto-removal**: The cage disappears when the ritual completes or is cancelled

### Configuration

Edit `plugins/ItemRituals/config.yml` to customize the plugin:

#### Obsidian Cage Settings
```yaml
obsidian-cage:
  enabled: true          # Enable the obsidian cage feature
  size: 30               # Cage size (30x30 blocks)
  warning-time: 120      # Time before ritual end when cage appears (seconds)
  material: OBSIDIAN     # Block material for the cage
  announce: true         # Send announcement when cage forms
  announce-msg:
    - "&e✦ &c☠ &6Obsidian Cage Formed! &c☠ &e✦"
    - "&7The ritual is now sealed within an obsidian cage!"
```

## Installation

1. Download the latest `.jar` file from the releases
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin using `plugins/ItemRituals/config.yml`

## Building from Source

This project uses Maven. To build:

```bash
mvn clean package
```

The compiled JAR will be available at `target/ItemRituals-1.0.0.jar`.

## Requirements

- Java 17 or higher
- PaperMC 1.20.4+ server

## Commands

| Command          | Description                                  |
|------------------|----------------------------------------------|
| `/ritual`       | Open the ritual item management GUI         |
| `/ritual start` | Manually start a ritual                        |
| `/ritual stop`  | Stop the current ritual                        |
| `/ritual reload`| Reload the plugin configuration                |

## Usage

1. Add items to the ritual pool using the GUI
2. Players can start rituals by clicking the 'Start Ritual' button
3. A reward item will appear at the ritual location
4. Players must collect the item before the ritual completes
5. The obsidian cage will form in the final minutes, creating a fully enclosed area

## Configuration Guide

### Ritual Settings
```yaml
ritual:
  duration: 600          # Duration in seconds (10 minutes)
  radius: 25             # Ritual area radius
  phase-interval: 120    # Interval between phase changes (seconds)
  warning-count: 3       # Number of warnings before completion
```

### Location Settings
```yaml
location:
  random: true                   # Randomize ritual location
  min-distance-from-spawn: 1000  # Minimum distance from spawn
  max-distance-from-spawn: 5000  # Maximum distance from spawn
  min-distance-from-players: 300 # Minimum distance from players
  allowed-worlds: []             # Leave empty for all worlds
  max-attempts: 50               # Max attempts to find valid location
  restrictions:
    avoid-water: true
    avoid-lava: true
    avoid-void: true
    avoid-regions: true
    min-y: 50
    max-y: 120
```

## Notes

- Ritual items are stored in `plugins/ItemRituals/pool.json`
- Use the GUI to add/remove items from the ritual pool
- The obsidian cage forms automatically in the final minutes of each ritual
- Players can mine the obsidian blocks to break the cage
- The plugin supports both Bukkit and PaperMC servers