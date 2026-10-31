# Mining / drop configuration

Each NetherOre has a `Mining.Ores.<Ore>` section in
`config/powercrystals/netherores/common.cfg` (the CoFH per-mod config):

```
mining {
    ores {
        coal {
            I:DropCount=2
            I:DropSpread=0
            I:Exp=0
            I:ExpSpread=5
            B:RequireSilkTouch=true
        }
    }
}
```

| Key | Meaning |
| --- | --- |
| `DropCount` | Base items dropped by one block, before Fortune. `0` with `DropSpread=0` disables item drops. |
| `DropSpread` | Adds a uniform random `0..(DropSpread-1)` to `DropCount` (vanilla-style ore spread). |
| `Exp` | Minimum experience dropped per block (`0` = none). |
| `ExpSpread` | Adds a uniform random `0..(ExpSpread-1)` to `Exp`. |
| `RequireSilkTouch` | `true` (default): Silk Touch returns the ore block, normal mining returns the configured drops. `false`: Silk Touch is treated as a normal break and also returns the configured drops. |

## Drop defaults

The defaults reproduce the pre-config hardcoded output exactly, so existing worlds
and packs do not change just by installing a build with these options:

| Ore | DropCount | DropSpread | Resulting range |
| --- | --- | --- | --- |
| Coal, Diamond, Gold, Iron, Copper, Tin, Emerald, Silver, Lead, Uranium, Platinum, Nickel, Steel, Iridium, Osmium, Sulfur, Titanium, Mithril, Adamantium, Rutile, Tungsten, Amber, Tennantite, Salt, Saltpeter, Magnesium | 2 | 0 | always 2 |
| Lapis | 8 | 9 | 8-16 |
| Redstone, Nikolite | 8 | 3 | 8-10 |

If no raw/vanilla item can be resolved for an ore (no mod provides an OreDictionary
entry), the block drops one of itself; downstream processing provides the 2x value.

## Experience defaults

Vanilla ore XP ranges are doubled (`Exp` = 2x the lower bound, `ExpSpread` covers the
doubled range). Metal-style ores follow modern iron/gold/copper ore, which drop no
experience; dust-style ores follow redstone and gem-style ores follow emerald.

| Ore class | Vanilla reference | Vanilla XP | Doubled default (`Exp` + `ExpSpread`) |
| --- | --- | --- | --- |
| Coal | coal ore | 0-2 | `0` + `5` -> 0-4 |
| Diamond, Emerald, Ruby, Peridot, Sapphire, Amber | diamond/emerald ore | 3-7 | `6` + `9` -> 6-14 |
| Lapis | lapis ore | 2-5 | `4` + `7` -> 4-10 |
| Redstone, Nikolite, Sulfur, Saltpeter, Salt | redstone ore | 1-5 | `2` + `9` -> 2-10 |
| Iron, Gold, Copper, Tin, Silver, Lead, Uranium, Platinum, Nickel, Steel, Iridium, Osmium, Titanium, Mithril, Adamantium, Rutile, Tungsten, Tennantite, Magnesium | modern iron/gold/copper ore | 0 | `0` + `0` |

Fortune is added on top of the configured item range. Large configured counts are split
into legal stacks (max stack size of the dropped item) instead of creating oversized
`ItemStack`s.

`DropCount`, `Exp` and `RequireSilkTouch` use the same key names as the historical
NetherOres config, so values already present in an upgraded
`config/powercrystals/netherores/common.cfg` are picked up; `DropSpread` and
`ExpSpread` are new and are appended with their defaults.

## Regression coverage

- `DropMathTest` - pure math: base count, spread bounds, Fortune bonus, XP, disabled
  drops.
- `OreMiningDefaultsTest` - every ore's drop and XP defaults, including the old
  Lapis/Redstone ranges and the silk-touch default.
- `MiningConfigLoaderTest` - loads a real Forge `Configuration` from a legacy-style
  file, proves the keys reach `Ores`, and round-trips save/reload.
- `ConfigWiringTest` - guards the glue that a plain JVM cannot instantiate: the config
  keys are actually read in `Ores` and actually used by `BlockNetherOres`' drop/XP path
  (the original bug was a config that existed but was never read).
