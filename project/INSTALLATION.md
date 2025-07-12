# Installation Guide - Fallen God Testament

## 🚀 Quick Installation

### Prerequisites
- Minecraft Server 1.21.5
- Java 21+
- Paper/Spigot server

### Step 1: Download & Install Plugin
```bash
# Download the plugin JAR (or compile from source)
# Copy to your server's plugins folder
cp testament-1.7.0.jar /path/to/server/plugins/

# Restart server or reload plugins
/reload confirm
```

### Step 2: Install Datapack
```bash
# Copy datapack to EACH world that should have altars
cp -r datapack/ /path/to/world/datapacks/fallengod_testament/
cp -r datapack/ /path/to/world_nether/datapacks/fallengod_testament/
cp -r datapack/ /path/to/world_the_end/datapacks/fallengod_testament/

# Reload datapacks
/reload
```

### Step 3: Create Altar Structures
The datapack includes placeholder .nbt files. You need to create the actual altar structures:

#### Build Each Altar Design:

**1. Fallen God Altar:**
```
Platform: 7x7 Blackstone
Center: Crying Obsidian
Top: Soul Fire
Corners: Blackstone pillars (3 high) with Soul Fire on top
```

**2. Banishment God Altar:**
```
Platform: 7x7 Nether Bricks  
Center: Magma Block
Top: Fire
Corners: Lava pools at cardinal directions
```

**3. Abyssal God Altar:**
```
Platform: 7x7 Prismarine
Center: Dark Prismarine
Top: Sea Lantern
Corners: Water pools
```

**4. Sylvan God Altar:**
```
Platform: 7x7 Moss Blocks
Center: Oak Log
Top: Oak Leaves
Corners: Different saplings (Oak, Birch, Spruce, Jungle)
```

**5. Tempest God Altar:**
```
Platform: 7x7 Quartz Blocks
Center: Lightning Rod
Decorations: White wool "clouds" around
```

**6. Veil God Altar:**
```
Platform: 7x7 End Stone
Center: End Portal Frame
Top: Ender Chest
Corners: End Rods
```

#### Save Structures:
1. Build each altar in creative mode
2. Use structure blocks to save each design
3. Save as: `fallen_altar/base`, `banishment_altar/base`, etc.
4. Copy the generated .nbt files to replace the placeholders in:
   `datapack/data/fallengod/structures/`

#### Add Marker Entities:
For each altar, place a marker entity at the center with the appropriate tag:
- Fallen: `fallen_altar`
- Banishment: `banishment_altar`  
- Abyssal: `abyssal_altar`
- Sylvan: `sylvan_altar`
- Tempest: `tempest_altar`
- Veil: `veil_altar`

### Step 4: Test Installation
```bash
# Check if plugin loaded
/plugins

# Scan for altars (may need to generate new chunks first)
/datapack scan 10

# Check fragment spawning stats
/fragment stats

# Test fragment giving
/fragment giveall fallen
/testament status
```

## 🔧 Configuration

### Plugin Configuration
Edit `plugins/FallenGodTestament/config.yml`:

```yaml
# Adjust fragment spawn rates
testament:
  fragments:
    chest_spawn_chance: 0.02    # 2% chance (adjust as needed)
    mob_drop_chance: 0.001      # 0.1% chance (adjust as needed)

# Configure Heart of Fallen God
heart_of_fallen_god:
  enabled: true
  extra_hearts: 15              # +15 hearts (25 total)
  
# Configure Veil nullification
nullification:
  enabled: true
  range: 16.0                   # 16 block nullification range
```

### Datapack Configuration
The datapack uses Minecraft's structure generation system. To adjust:

1. **Generation Frequency**: Edit `datapack/data/fallengod/worldgen/structure_set/fallen_god_altars.json`
   ```json
   "placement": {
     "spacing": 32,    # Chunks between attempts (higher = rarer)
     "separation": 8   # Minimum chunks between structures
   }
   ```

2. **Biome Lists**: Edit files in `datapack/data/minecraft/tags/worldgen/biome/has_structure/`

## 🎮 First Time Setup

### For Server Admins:
1. **Test in Creative**: Use `/fragment giveall <god>` to test the complete flow
2. **Set Permissions**: Configure who can use admin commands
3. **Announce to Players**: Explain the fragment collection system
4. **Monitor Performance**: Check `/fragment stats` regularly

### For Players:
1. **Start Exploring**: Open chests and kill mobs to find fragments
2. **Check Progress**: Use `/testament status` to track fragments
3. **Find Altars**: Use `/datapack locate <god>` (if admin allows)
4. **Plan Strategy**: Decide which god to complete first

## 🐛 Troubleshooting

### Common Issues:

**"No altars found"**
- Generate new chunks (altars only appear in newly generated areas)
- Verify datapack is installed: `/datapack list`
- Check if structures are enabled: `/gamerule doStructureGeneration true`

**"Fragments not spawning"**
- Check exploration requirement: Must open 50+ chests first
- Verify spawn rates in config.yml
- Check player cooldowns: 2 hours between chest fragments

**"Altar not responding"**
- Ensure you have all 7 UNIQUE fragments for that god
- Right-click the center block of the altar
- Check if altar is properly registered: `/datapack scan`

**"Effects not working"**
- Check for plugin conflicts
- Verify permissions
- Restart server if needed

### Debug Commands:
```bash
/fragment stats          # Check spawning statistics
/fragment progress <player>  # Detailed player progress
/datapack scan 20        # Scan large area for altars
/datapack reload         # Reload altar registrations
```

## 📊 Performance Monitoring

### Key Metrics to Watch:
- Fragment spawn rate (should be very low)
- Player chest open counts
- Altar interaction frequency
- Server TPS during fragment operations

### Optimization Tips:
- Adjust spawn rates if too many/few fragments
- Monitor player cooldowns
- Clean caches periodically: `/fragment clear`

## 🔄 Updates

### Updating Plugin:
1. Stop server
2. Replace JAR file
3. Start server (config auto-updates)

### Updating Datapack:
1. Replace datapack files
2. Run `/reload`
3. New chunks will use updated generation

## 🎯 Success Metrics

Your installation is successful when:
- ✅ Plugin loads without errors
- ✅ Datapack shows in `/datapack list`
- ✅ New chunks generate with altars
- ✅ Fragments spawn rarely in chests/from mobs
- ✅ Altar interactions work and grant rewards
- ✅ Heart/Veil effects function properly

## 🆘 Getting Help

If you encounter issues:
1. Check server logs for errors
2. Verify all installation steps
3. Test with admin commands first
4. Check permissions and configuration
5. Monitor performance with debug commands

The system is designed to be rare and epic - if fragments are spawning frequently, something is misconfigured!