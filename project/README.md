# Fallen God Testament - Complete Setup

## 🎮 Overview
Epic Minecraft plugin where players collect 7 rare fragments from each of 6 fallen gods to unlock ultimate rewards and divine powers.

## 📦 What's Included
- **Plugin JAR**: Core game mechanics, fragment spawning, altar interactions
- **Datapack**: Natural altar generation in world generation
- **Complete Documentation**: Installation and usage guides

## ⚡ Quick Start

### 1. Install Plugin
```bash
# Copy the compiled JAR to your server
cp target/testament-1.7.0.jar /path/to/server/plugins/
```

### 2. Install Datapack
```bash
# Copy datapack to each world
cp -r datapack/ /path/to/world/datapacks/fallengod_testament/
```

### 3. Create Altar Structures
1. Build each altar design in creative mode
2. Save as .nbt files using structure blocks
3. Place in `datapack/data/fallengod/structures/`

### 4. Start Server
```bash
# Restart server or reload
/reload
```

## 🏛️ The Six Gods & Their Rewards

### 1. **Fallen God** - Ultimate Defense
- **Theme**: Death, undeath, ultimate protection
- **Reward**: Netherite armor set + **Heart of the Fallen God**
- **Power**: 25 total hearts + permanent effects
- **Biomes**: Swamps, Dark Forests, Deep Dark

### 2. **Banishment God** - Ultimate Offense  
- **Theme**: Fire, exile, destruction
- **Reward**: Ultimate weapon and tool set
- **Power**: Overpowered enchanted weapons
- **Biomes**: Deserts, Badlands, Savannas

### 3. **Abyssal God** - Master of Depths
- **Theme**: Ocean depths, water mastery
- **Reward**: Trident of the Endless Deep + water abilities
- **Power**: Permanent water breathing, dolphins grace
- **Biomes**: Oceans, Rivers, Beaches

### 4. **Sylvan God** - Nature's Guardian
- **Theme**: Forests, nature, growth
- **Reward**: Bow of the Ancient Forest + nature powers
- **Power**: Permanent regeneration, saturation
- **Biomes**: Forests, Jungles, Taigas

### 5. **Tempest God** - Storm Lord
- **Theme**: Sky, storms, lightning, flight
- **Reward**: Wings of the Storm Lord + storm powers
- **Power**: Enhanced elytra, speed, jump boost
- **Biomes**: Mountains, Hills, Peaks

### 6. **Veil God** - Master of Dimensions
- **Theme**: Reality manipulation, void magic
- **Reward**: **Veil of Nullification** + dimension powers
- **Power**: Counters Heart of Fallen God, night vision
- **Biomes**: Any (mysterious and rare)

## ⚔️ Strategic PvP Balance

### Heart vs Veil System
- **Heart of Fallen God**: Ultimate power (25 hearts + strength)
- **Veil of Nullification**: Counters Heart within 16 blocks
- **Result**: Strategic positioning and risk/reward gameplay

## 🎯 Fragment Collection

### Rarity System
- **Chest Spawning**: 2% chance in opened chests
- **Mob Drops**: 0.1% base chance from specific mobs
- **Requirements**: Must open 50+ chests before fragments spawn
- **Cooldowns**: 2 hours between chest fragments, 1 hour between mob drops

### Fragment Distribution
- Fragments 1-3: More common (30%, 20%, 15%)
- Fragments 4-5: Uncommon (13%, 10%)
- Fragments 6-7: Rare (7%, 5%)

## 🏛️ Altar System

### Natural Generation
- Altars generate naturally during world generation
- Biome-specific placement for thematic consistency
- Balanced spacing (32 chunk spacing, 8 chunk separation)
- Uses Minecraft's native structure system

### Interaction
- Right-click altar center block with all 7 fragments
- Fragments are consumed and rewards granted
- Epic server-wide announcements
- Permanent effects applied

## 📋 Commands

### Player Commands
- `/testament status` - View fragment progress
- `/testament reunite <god>` - Reunite fragments at altar

### Admin Commands
- `/fragment spawn <god> <number> <chest|here>` - Spawn fragments
- `/fragment heart` - Give Heart of Fallen God
- `/fragment veil` - Give Veil of Nullification
- `/fragment stats` - View spawning statistics
- `/fragment giveall <god>` - Give all 7 fragments
- `/datapack scan [radius]` - Scan for altars
- `/datapack locate <god>` - Find specific altar

## 🔧 Configuration

### Fragment Spawning
```yaml
testament:
  fragments:
    chest_spawn_chance: 0.02      # 2% chance
    mob_drop_chance: 0.001        # 0.1% chance
    min_chests_for_fragments: 50  # Exploration requirement
```

### Heart of Fallen God
```yaml
heart_of_fallen_god:
  enabled: true
  extra_hearts: 15              # +15 hearts (25 total)
  strength_level: 1             # Strength I
  regeneration_level: 2         # Regeneration II
```

### Veil Nullification
```yaml
nullification:
  enabled: true
  range: 16.0                   # 16 block range
  veil_effects:
    slow_falling: true
    night_vision: true
```

## 🏗️ Building Altar Structures

### Required Structures
Each altar needs a 7x7 platform with themed center block:

1. **Fallen Altar**: Blackstone platform, crying obsidian center, soul fire
2. **Banishment Altar**: Nether brick platform, magma block center, fire
3. **Abyssal Altar**: Prismarine platform, dark prismarine center, sea lantern
4. **Sylvan Altar**: Moss block platform, oak log center, leaves
5. **Tempest Altar**: Quartz platform, lightning rod center, clouds
6. **Veil Altar**: End stone platform, end portal frame center, ender chest

### Structure Creation Process
1. Build altar in creative world
2. Use structure block to save as `<god>_altar/base`
3. Copy .nbt file to `datapack/data/fallengod/structures/`
4. Add marker entity with tag `<god>_altar` at center

## 🎮 Gameplay Flow

### 1. Exploration Phase
- Players explore and open chests
- Kill mobs in dangerous areas
- Rare fragments spawn based on activity

### 2. Collection Phase
- Gather all 7 unique fragments for chosen god
- Track progress with `/testament status`
- Plan which god to complete first

### 3. Reunification Phase
- Locate appropriate altar using `/datapack locate`
- Bring all 7 fragments to altar
- Right-click to complete testament

### 4. Power Phase
- Receive epic rewards and permanent effects
- Gain strategic advantages in PvP
- Work toward completing other testaments

## 🌟 End Game Content

### Testament Completion Rewards
- **Fallen God**: Ultimate survivability + Heart power
- **Banishment God**: Ultimate damage output
- **Abyssal God**: Water world domination
- **Sylvan God**: Nature harmony and archery mastery
- **Tempest God**: Sky mobility and storm power
- **Veil God**: Reality manipulation and Heart counter

### Strategic Considerations
- Heart wielders become powerful but vulnerable to Veil
- Veil wielders can neutralize Heart power tactically
- Other testaments provide specialized advantages
- Encourages diverse playstyles and team compositions

## 🔒 Permissions

### Player Permissions
- `fallengod.use` - Basic plugin usage (default: true)

### Admin Permissions
- `fallengod.admin.*` - All admin commands
- `fallengod.admin.spawn` - Spawn fragments
- `fallengod.admin.stats` - View statistics
- `fallengod.admin.locate` - Locate altars

## 📊 Performance

### Optimizations
- Async fragment spawning operations
- Efficient caching systems
- Cleanup of old tracking data
- Thread-safe implementations

### Resource Usage
- Minimal server impact
- Efficient data storage
- Balanced spawn rates prevent spam
- Smart cooldown systems

## 🐛 Troubleshooting

### Common Issues
1. **Altars not generating**: Ensure datapack is installed and `/reload` run
2. **Fragments not spawning**: Check exploration requirements (50+ chests)
3. **Altar not responding**: Verify all 7 unique fragments in inventory
4. **Effects not working**: Check for conflicting plugins

### Debug Commands
- `/fragment stats` - Check spawning statistics
- `/datapack scan` - Find nearby altars
- `/fragment progress <player>` - Check detailed progress

## 📈 Estimated Playtime

### Per Testament
- **Casual Players**: 20-40 hours
- **Dedicated Players**: 10-20 hours
- **Hardcore Grinders**: 5-10 hours

### All Six Testaments
- **Complete Collection**: 100-200+ hours
- **Provides long-term server engagement**
- **Encourages exploration and dangerous activities**

## 🎯 Design Philosophy

### Core Principles
1. **Rarity Creates Value**: Low drop rates make fragments precious
2. **Exploration Rewards**: Dangerous areas have better chances
3. **Strategic Depth**: Heart vs Veil creates tactical gameplay
4. **Long-term Engagement**: Multiple testaments provide goals
5. **Server Community**: Epic announcements create shared experiences

This system transforms fragment collection from simple grinding into an epic quest worthy of the divine powers it grants!