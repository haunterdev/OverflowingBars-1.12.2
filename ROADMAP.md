# Overflowing Bars — Roadmap

User suggestions and planned features for the 1.12.2 port.

Status: `[ ]` todo · `[~]` in progress · `[x]` done

## User suggestions

- [ ] **1. Mount overflowing bars** — HIGH value, MEDIUM effort
  - Render overflowing bar rows for mount health (horse, donkey, pig, etc.) — same layered
    icon rendering as health/armor bars.
  - Likely needs a mount-specific `IconRowConfig` and a hook into `GuiIngameForge.renderHealthMount`.
  - Consider separate row-count overlay or reuse existing row-count logic gated by config.
  - Source: user suggestion (2026-06-13).

- [ ] **2. Quantum Things compat — "Flamer Bar" / Lava Charm & Lava Waders** — MEDIUM value, LOW effort
  - Investigate: "Flamer Bar" (fire/lava immunity bar) not appearing when Lava Charm or Lava
    Waders from [Quantum Things](https://www.curseforge.com/minecraft/mc-mods/quantum-things)
    are equipped.
  - Likely a render-layer ordering issue or the Quantum Things bar rendering at a position
    that OverflowingBars' GUI overlay shift overwrites.
  - May need a compat layer or config toggle to adjust bar positions / disable specific overlays
    when Quantum Things is loaded.
  - Source: user suggestion (2026-06-13).
