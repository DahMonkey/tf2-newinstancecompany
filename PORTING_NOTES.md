# TF2 Stuff Reloaded: 1.12.2 → 1.19.2 port notes

Source: https://github.com/SmileycorpMC/tf2-stuff-reloaded (LGPL-2.1), plus assets from the 1.12.2-1.7.3a release jar.

## Build
- Forge 1.19.2-43.4.0, official mappings, Java 17, ForgeGradle 6, Gradle 8.8.
- GitHub Actions builds every push; read errors with `gh run view <id> --log`.
- `tools/remap.py` (outside the repo) did the first mechanical rename pass; everything after that is hand-ported.

## Conventions used by the port
- **Item metadata** → stored as the item's damage value (`ItemStack#getDamageValue`).
  Items keep their 1.12 single-item-with-subtypes design. Create stacks with
  `TF2Util.metaStack(item, count, meta)`. Loot tables use `rafradek_tf2_weapons:set_meta`.
- **Item models chosen at runtime** (weapons by NBT "Type", meta items by meta) go through
  `client.TF2ItemModels`, which wraps each item's baked model with ItemOverrides that call the
  item's mesh definition (`ItemMeshDefinition` is now our own interface in `client`).
- **Capabilities**: `getCapability(cap, null)` became `getCapability(cap).orElse(null)`.
- **Registry names** with capitals were renamed: `itemTF2` → `item_tf2`, `robotPart` → `robot_part`,
  potion effects `bonkEff` → `bonk_eff`, etc. (lower snake case).
- **Translations**: original `.lang` keys are kept as-is in `en_us.json`; server-side
  `I18n.translateToLocal` became `TF2Util.translate` (uses `Language.getInstance()`).

## Stage plan
1. Weapons core (items, attributes, capabilities, networking, projectiles, HUD, workbench).
2. Mercenaries and AI.
3. Engineer buildings.
4. Bosses, invasions, arenas, world generation, contracts, JEI.
