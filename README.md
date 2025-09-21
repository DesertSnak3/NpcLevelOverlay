# NPC Level Overlay Plugin for RuneLite

A RuneLite plugin that displays NPC combat information above their heads, including combat levels, max hits, and weakness indicators.

## Background

After creating an ironman account, I found myself struggling to identify which NPCs posed the greatest threat during combat. While the vanilla game shows combat levels through right-click menus, there was no easy way to quickly assess:
- Which NPCs could hit hard enough to be dangerous
- What combat style or spell would be most effective against them
- Which monsters I should avoid based on my current combat level

This plugin was created to solve these problems by providing at-a-glance information about NPCs directly in the game world.

## Features

### 🎯 Combat Level Display
- Shows NPC combat levels above their heads
- Color-coded based on level difference:
  - **Green**: Lower level than you (safe to fight)
  - **Yellow**: Same level as you
  - **Orange**: 1-3 levels higher (use caution)
  - **Red**: 4+ levels higher (dangerous!)

### 💥 Max Hit Display
- Shows the maximum damage an NPC can deal in brackets (e.g., `[23]`)
- Simplified display shows only the max hit value
- Helps ironmen and hardcore players avoid potentially deadly encounters
- Data sourced from comprehensive monster database

### ⚔️ Weakness Icons
- Displays icons indicating what the NPC is weak to:
  - **Elemental Magic**: Fire 🔥, Water 💧, Earth 🌍, Air 💨 (shown when weak to specific elements)
  - **General Magic**: Magic Icon ✨ (shown when weak to magic but no specific element)
  - **Melee Styles**: Stab 🗡️, Slash ⚔️, Crush 🔨
  - **Ranged Weapons**: Arrow 🏹, Bolt 🎯, Dart 📍, General Ranged 🏹
- Smart weakness detection:
  - Analyzes NPC defense stats to determine primary weakness
  - Magic-using NPCs show elemental weaknesses
  - Defaults to slash icon when no specific weakness is known
- Icons are scaled to be visible but not intrusive

### ⚠️ Smart Aggression Indicators
- Shows an icon for NPCs that will actually attack you based on combat levels
- Uses OSRS aggression formula: NPCs stop attacking when your level > (2 × NPC level + 1)
- Example: Level 28 hobgoblins won't show aggression icon for level 57+ players
- Helps identify which monsters will actually engage you
- Particularly useful for planning training spots and safe areas

### 📋 NPC Name Display
- Optionally show NPC names alongside their level
- Format: `NPC Name (Level)`

## Configuration

Access the plugin settings through the RuneLite configuration panel:

| Setting | Description | Default |
|---------|-------------|---------|
| **Show level** | Display the NPC's combat level | On |
| **Show NPC name** | Display the NPC's name next to its level | Off |
| **Show Max Hit** | Display the NPC's maximum possible hit | On |
| **Show Weakness Icon** | Display an icon indicating what the NPC is weak to | On |
| **Show Aggression Icon** | Display an icon for NPCs that will attack you | On |
| **Icon Size** | Set the size of weakness and aggression icons (XS/S/M/LG) | M |
| **Minimum Level** | Only show overlays for NPCs at or above this level | 0 |
| **Only show in combat** | Only display overlays for NPCs you're fighting | Off |

### Advanced Features

#### Filtering Options
- **Minimum Level Filter**: Set a minimum NPC level to reduce clutter (e.g., set to 7 to hide rats, chickens)
- **Combat-Only Mode**: Only show overlays for NPCs you're actively fighting
- **Flexible Display**: Toggle level, name, and max hit independently

#### Icon Customization
- **Adjustable Icon Size**: Choose from Extra Small (50%), Small (75%), Medium (100%), or Large (125%)
- **Smart Icon Display**: Icons scale properly to remain visible but not intrusive

#### Color Customization
- Custom color configuration for each level range (hidden by default in settings)

## Usage

1. Install the plugin through the RuneLite Plugin Hub
2. Enable the plugin in your RuneLite settings
3. Configure your preferred display options
4. NPCs will now show their information above their heads as you encounter them

## Examples

- **Abyssal Demon**: `86 [8]` 🗡️ (Level 86, max hit 8, weak to stab)
- **Blue Dragon**: `111 [50]` 💧 (Level 111, max hit 50, weak to water spells)
- **Guard**: `21 [3]` ⚔️ (Level 21, max hit 3, weak to slash)
- **Hobgoblin (to level 56 player)**: `⚠️ 28 [4]` ⚔️ (Shows aggression icon)
- **Hobgoblin (to level 57+ player)**: `28 [4]` ⚔️ (No aggression icon - won't attack)

## Data Source

The plugin uses comprehensive NPC data from a detailed monster database, including:
- Combat levels from NPC definitions
- Maximum hit values from verified sources
- Weakness information based on defensive stats
- Aggression status for dangerous NPCs
- All data is stored locally in CSV format for quick access

## Technical Details

- Lightweight overlay rendering
- Efficient caching of weakness icons
- CSV-based data loading for easy updates
- Supports all NPCs in Old School RuneScape

## Contributing

This plugin is open source! Feel free to submit issues or pull requests on GitHub.

## Credits

- NPC data sourced from the OSRS Wiki community
- Icons adapted from in-game sprites
- Built for the RuneLite client

---

*Never get caught off-guard by a hard-hitting NPC again!*