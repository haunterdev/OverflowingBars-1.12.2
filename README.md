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

- **Scaling Health** — automatic. When Scaling Health is installed with its custom heart rendering enabled (its default), Overflowing Bars yields the health bar to it and keeps rendering the armor and toughness bars. Disable Scaling Health's `Custom Heart Rendering` to use this mod's stacked hearts instead; the switch is picked up live, including through `/scalinghealth config_reload`. Running both heart renderers at once is what the compat prevents — it double-draws and can crash the client when taking damage.
- **AttributeFix** — not required, no conflict. This mod only renders attribute values; AttributeFix raising the vanilla caps (armor 30, toughness 20) is what makes overflow rows past those values possible at all.
- **Thirst/stamina HUD mods** (Simple Difficulty and similar) — the toughness bar sits above the hunger bar by default. Set `leftSide=true` in the `toughness` config category to move it to the left stack, or `armorToughnessBar=false` to hide it.
- **Quantum Things / Random Things** (lava charm "Flamer Bar") — automatic. That bar draws on the *Post* phase of the armor element; because this mod cancels the armor element's *Pre* phase to draw its own bar, Forge would otherwise skip the Post entirely and the lava charm bar would disappear. This mod now re-fires the Post phase for the health, armor, and mount elements it replaces, so any add-on HUD bar keyed to them keeps rendering and stacking correctly.

## License

Code is [MPL-2.0](LICENSE.md), matching the original. Textures remain © Fuzs — see [LICENSE-ASSETS.md](LICENSE-ASSETS.md).
