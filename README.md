# Overflowing Bars

A 1.12.2 backport of [Overflowing Bars](https://www.curseforge.com/minecraft/mc-mods/overflowing-bars) by Fuzs.

Stacks health, absorption, armor, and armor toughness into compact colored rows with a row counter instead of pushing them off-screen. Uses vanilla icons as the base layer with a single highlight color for stacked rows — no confusing rainbow.

Requires Minecraft 1.12.2, Forge 14.23.5.2860+, and [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 9.1+. Client-side only.

## Features

- **Health & absorption** — stack beyond ten hearts in place, with an optional "Nx" row count.
- **Armor** — the same compact stacking for high armor values.
- **Armor toughness** — adds a dedicated toughness bar (diamond/netherite), which vanilla never shows.
- **Chat offset** — keeps chat messages clear of the taller bars.
- **Configurable** — row count color/style, per-bar layers, toughness side, and more in `config/overflowingbars.cfg`.

## Testing without modded armor

The `debug` config category has `debugHealthPoints`, `debugArmorPoints`, and `debugToughnessPoints`. Set any above `0` to force that bar's value so you can preview the overflow rows and row counts without commands or modded gear. `0` = off (use real values).

## License

Code is [MPL-2.0](LICENSE.md), matching the original. Textures remain © Fuzs — see [LICENSE-ASSETS.md](LICENSE-ASSETS.md).
