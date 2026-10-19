# Flux Networks for Minecraft 26.1.2 (vibe coded)

> Wanted to use this with ATM11, completely ported over with Claude.

This is an **unofficial, AI-assisted ("vibe coded")** port of [Flux Networks](https://github.com/SonarSonic/Flux-Networks) by
Sonar Sonic and BloCamLimb to **Minecraft 26.1.2 / NeoForge 26.1.2.x**. It is based on the upstream
`1.21` branch (version 8.0/8.1). All credit for the mod goes to the original authors. Flux Networks is
MIT-licensed; see [LICENSE](LICENSE).

> Not affiliated with or endorsed by the original authors. Please don't report issues with this
> port to the upstream issue tracker.

## Status

- ✅ Compiles against Minecraft 26.1.2 + NeoForge 26.1.2.122 (Java 25)
- ✅ Dedicated server boots; blocks place, save and reload correctly
- ⚠️ Client: an early startup crash was fixed; loading in a full modpack is not yet confirmed
- ⚠️ Energy transfer between plugs/points and other mods' machines is not yet confirmed in-game

## What changed from upstream

- **Energy:** uses NeoForge's new transactional `EnergyHandler` transfer API (1.21.9+) via
  `FNEnergyHandler` and a rewritten `ForgeEnergyConnector`.
- **Saving:** block entities use the new `ValueInput`/`ValueOutput` save system through a small
  shim in `FluxUtils` (`readRoot`/`writeRoot`) so the on-disk keys are unchanged.
- **GUI:** ported to the 26.1 `GuiGraphicsExtractor` API. `GuiDraw` replaces the old global
  shader colour/texture state, and `GuiFocusable` bridges the new input event objects to the
  original `(x, y, button)` handlers.
- **Rendering:** the Flux Storage energy display uses the new render-state block entity renderer;
  block/item colours use tint sources and item model definitions (`assets/fluxnetworks/items`).
- **Recipes, registries, networking, commands, permissions** updated to the 26.1 APIs.

### Removed for now

Optional integrations that have no 26.1 builds to compile against: JEI, The One Probe, Curios,
Mekanism, GregTech CEu and the Modern UI design. The datagen classes were removed too; the
generated resources are kept in `src/generated`. Flux Storage items no longer show the energy fill
bar in the inventory (placed blocks still do).

## Building

Requires **JDK 25**.

```sh
./gradlew build
```

The jar is written to `build/libs/`.
