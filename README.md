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
- Shows the maximum damage an NPC can deal in brackets (e.g., `[0-23]`)
- Helps ironmen and hardcore players avoid potentially deadly encounters
- Parses complex damage values including multi-hit attacks

### ⚔️ Weakness Icons
- Displays icons indicating what the NPC is weak to:
  - **Elemental Magic**: Fire 🔥, Water 💧, Earth 🌍, Air 💨 (shown when weak to specific elements)
  - **General Magic**: Magic Icon ✨ (shown when weak to magic but no specific element)
  - **Melee Styles**: Stab 🗡️, Slash ⚔️, Crush 🔨
  - **Ranged Weapons**: Arrow 🏹, Bolt 🎯, Dart 📍, General Ranged 🏹
- Smart magic weakness detection:
  - If an NPC is weak to both magic and a specific element, shows the elemental rune
  - If weak to magic without a specific element, shows magic icon
- Defaults to slash icon when no specific weakness is known
- Icons are scaled to be visible but not intrusive

### 📋 NPC Name Display
- Optionally show NPC names alongside their level
- Format: `NPC Name (Level)`

## Configuration

Access the plugin settings through the RuneLite configuration panel:

| Setting | Description | Default |
|---------|-------------|---------|
| **Show NPC name** | Display the NPC's name next to its level | Off |
| **Show Max Hit** | Display the NPC's maximum possible hit | On |
| **Show Weakness Icon** | Display an icon indicating what the NPC is weak to | On |

### Advanced Color Customization
The plugin also supports custom color configuration for each level range (hidden by default in settings).

## Usage

1. Install the plugin through the RuneLite Plugin Hub
2. Enable the plugin in your RuneLite settings
3. Configure your preferred display options
4. NPCs will now show their information above their heads as you encounter them

## Examples

- **Abyssal Demon**: `86 [0-8]` 🗡️ (Level 86, max hit 8, weak to stab)
- **Blue Dragon**: `111 [0-50]` 💧 (Level 111, max hit 50, weak to water spells)
- **Guard**: `21 [0-3]` ⚔️ (Level 21, max hit 3, weak to slash)

## Data Source

The plugin uses comprehensive NPC data from the OSRS wiki, including:
- Combat levels from NPC definitions
- Maximum hit values from community-sourced data
- Weakness information based on defensive stats

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