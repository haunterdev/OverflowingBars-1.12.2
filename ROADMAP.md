# Overflowing Bars — Roadmap

User suggestions and planned features for the 1.12.2 port.

Status: `[ ]` todo · `[~]` in progress · `[x]` done

## User suggestions

- [x] **1. Mount overflowing bars** — HIGH value, MEDIUM effort
  - Render overflowing bar rows for mount health (horse, donkey, pig, etc.) — same layered
    icon rendering as health/armor bars.
  - Likely needs a mount-specific `IconRowConfig` and a hook into `GuiIngameForge.renderHealthMount`.
  - Consider separate row-count overlay or reuse existing row-count logic gated by config.
  - Source: user suggestion (2026-06-13).
  - DONE (2026-06-15): `mount` config category + `MountBarRenderer` (compact single row,
    vanilla mount hearts as base, orange overflow layer, reused `RowCountRenderer` "Nx").
    Hooked `RenderGameOverlayEvent.Pre` HEALTHMOUNT in `GuiOverlayHandler`; cancels vanilla,
    reserves one row of `right_height`. Build verified; in-game visual check still pending.

- [x] **2. Quantum Things compat — "Flamer Bar" / Lava Charm & Lava Waders** — MEDIUM value, LOW effort
  - DONE (2026-06-15). Root cause confirmed by decompiling QuantumThings 1.2.2 (a repackage
    of lumien's Random Things): `RTEventHandler.renderLavaCharm` draws the lava charm bar on
    `RenderGameOverlayEvent.Post` + `ElementType.ARMOR`, reading `GuiIngameForge.left_height`.
    Forge renders each element as `if (pre(TYPE)) return; ...; post(TYPE);`, so our cancel of
    the ARMOR *Pre* (to draw our own armor bar) made Forge skip `post(ARMOR)` — the lava bar
    never fired.
  - Fix: `GuiOverlayHandler.firePostOverlay` re-fires the Post event for HEALTH, ARMOR, and
    HEALTHMOUNT after we cancel their Pre and advance the height counter. General fix — any
    add-on HUD bar keyed to those Post phases now keeps rendering and stacking. Build verified;
    in-game check against QuantumThings still pending.
  - Investigate: "Flamer Bar" (fire/lava immunity bar) not appearing when Lava Charm or Lava
    Waders from [Quantum Things](https://www.curseforge.com/minecraft/mc-mods/quantum-things)
    are equipped.
  - Likely a render-layer ordering issue or the Quantum Things bar rendering at a position
    that OverflowingBars' GUI overlay shift overwrites.
  - May need a compat layer or config toggle to adjust bar positions / disable specific overlays
    when Quantum Things is loaded.
  - Source: user suggestion (2026-06-13).
