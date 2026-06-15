# Overflowing Bars

A 1.12.2 backport of [Overflowing Bars](https://www.curseforge.com/minecraft/mc-mods/overflowing-bars) by Fuzs.

Stacks health, absorption, armor, and armor toughness into compact colored rows with a row counter instead of pushing them off-screen. Uses vanilla icons as the base layer with a single highlight color for stacked rows.

Requires 1.12.2, Forge 14.23.5.2860+, and [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 9.1+. Client-side only.

## Features

- **Health & absorption** — stack beyond ten hearts in place, with an optional "Nx" row count.
- **Armor** — the same compact stacking for high armor values.
- **Armor toughness** — adds a dedicated toughness bar (diamond/netherite), which vanilla never shows.
- **Mount health** — stacks a ridden mob's hearts (horse, donkey, pig, etc.) into one compact row with overflow coloring and an optional "Nx" row count, instead of vanilla's up-to-30-heart wrap.
- **Chat offset** — keeps chat messages clear of the taller bars.
- **Configurable** — row count color/style, per-bar layers, toughness side, and more in `config/overflowingbars.cfg`.

## Mod compatibility

- **Scaling Health** — handled automatically. Defaults to letting Scaling Health draw the hearts; disable its `Custom Heart Rendering` to use these stacked hearts instead.
- **AttributeFix** — works alongside it. Needed if you want armor/toughness to actually exceed the vanilla caps.
- **Simple Difficulty / thirst HUDs** — the toughness bar sits above the hunger bar. Set `leftSide=true` in the `toughness` config to move it, or `armorToughnessBar=false` to hide it.
- **Quantum Things / Random Things** — the lava charm bar renders correctly alongside the armor bar.

## License

Code is [MPL-2.0](LICENSE.md), matching the original. Textures remain © Fuzs — see [LICENSE-ASSETS.md](LICENSE-ASSETS.md).
