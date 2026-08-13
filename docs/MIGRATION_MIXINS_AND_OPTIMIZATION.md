# NetherOres-Legacy: Mixin Migration & Efficiency Findings

> Analysis date: 2026-08-13
> Scope: Replace the Access Transformer and all reflection usage with Mixins, and
> optimize the raw-ore drop path.
>
> **Status:** Sections 1 and 3 are **complete** (migrated AT + reflection to Mixins,
> and optimized the raw-ore drop path). Section 2 is analytical background. See
> Section 4 for the remaining cleanup.

---

## 1. Inventory: where the mod reaches into vanilla private internals

There are exactly **3** places the mod needs non-public access to vanilla classes,
spanning one Access Transformer file plus one reflection call. Nothing else in the
mod (`src/main/java`) uses reflection or access transformers.

| # | Target | Member | Used by | Current mechanism |
|---|--------|--------|---------|-------------------|
| 1 | `EntityPigZombie` | `becomeAngryAt(Entity)` (obf `func_70835_a`) | `BlockNetherOres.angerPigmen` → `o.becomeAngryAt(var0)` (`BlockNetherOres.java:439`) | **AT** (`netherores_at.cfg`) |
| 2 | `EntitySilverfish` | field `allySummonCooldown` (obf `field_70843_d`) | `EntityHellfish.updateEntityActionState` reads/writes `super.allySummonCooldown` (`EntityHellfish.java:38-44`) | **AT** (`netherores_at.cfg`) |
| 3 | `ItemBlock` | field `field_150939_a` (the `ItemBlock.blockInstance`) | `BlockNetherOverrideOre` constructor via `ObfuscationReflectionHelper.setPrivateValue(...)` (`BlockNetherOverrideOre.java:43-47`) | **Reflection** |

### Current AT file (`src/main/resources/META-INF/netherores_at.cfg`)

```properties
# Access Transformer for NetherOres (Minecraft 1.7.10)
# Make PigZombie.becomeAngryAt(...) public so mods can call it directly.
public net.minecraft.entity.monster.EntityPigZombie func_70835_a(Lnet/minecraft/entity/Entity;)V
public net.minecraft.entity.monster.EntityPigZombie becomeAngryAt(Lnet/minecraft/entity/Entity;)V
public net.minecraft.entity.monster.EntitySilverfish allySummonCooldown
public net.minecraft.entity.monster.EntityPigZombie func_70835_c(Lnet/minecraft/entity/Entity;)V
# Silverfish allySummonCooldown field (SRG name observed in srg_patched sources)
public net.minecraft.entity.monster.EntitySilverfish field_70843_d
public net.minecraft.entity.monster.EntitySilverfish allySummonCooldown
```

Also referenced in `gradle.properties`:
```properties
accessTransformersFile = netherores_at.cfg
```
---

## 2. Q1 — Can every AT + reflection be replaced with Mixins?

**Yes.** All three were converted to Mixins. The entire `netherores_at.cfg` file and
the `accessTransformersFile` line in `gradle.properties`, plus the single
`ObfuscationReflectionHelper` call, are now removed.

### Conversion mapping

> **Implemented.** One deviation from the original plan (see note below).

| # | Mixin approach |
|---|----------------|
| 1 | `@Mixin(EntityPigZombie.class)` interface, `EntityPigZombieMixin`, with `@Invoker("becomeAngryAt") void becomeAngryAt(Entity e)`. `BlockNetherOres.angerPigmen` casts the `EntityPigZombie` to it and calls the method. |
| 2 | `@Mixin(EntitySilverfish.class)` interface, `EntitySilverfishMixin`, with `@Accessor("allySummonCooldown")` getter/setter. `EntityHellfish` casts `this` to the accessor interface and uses the getter/setter (no more `super.allySummonCooldown`). |
| 3 | `@Mixin(ItemBlock.class)` interface, `ItemBlockMixin`, with `@Accessor("field_150939_a") void setBlockInstance(Block)`. `BlockNetherOverrideOre` calls it instead of `ObfuscationReflectionHelper`. |

> **How the mixins were registered:** all three are declared in
> `Mixins.MINECRAFT` as EARLY common mixins (`addCommonMixins(...)`), since they all
> target vanilla classes loaded early.

### Note: interface-mixin validator rule (found during implementation)

The Mixin annotation processor enforces a strict rule for **interface** mixins
targeting a **class**: the interface may only contain `@Accessor` / `@Invoker`
methods — not `@Shadow`. (Sponge's `TargetValidator.validateInterfaceMixin`
rejects any other method with "Targetted type ... is not an interface".)
Consequently `EntityPigZombieMixin` uses `@Invoker("becomeAngryAt")` — the method
analog of `@Accessor` — rather than `@Shadow`.

### Caveats / prerequisites (important)

1. **Mixin support is currently disabled.** This was true before implementation; it is
   now **resolved** — `gradle.properties` has `usesMixins = true` and the UniMixins
   boilerplate is wired up.
2. **`run/config/mixingasm/...` files are leftovers.** They belong to a different,
   older 1.7.10 mixin coremod (Mixingasm) and are **not** connected to the current
   build. They should be ignored/cleaned up, not relied on.
3. **The `net/` copies at repo root are reference decompiles only.**
   `net/minecraft/entity/monster/EntityPigZombie.java` and
   `EntitySilverfish.java` are RFG srg-named decompiled sources for reference; they
   are *not* compiled by the build (`src/main/java` is the only Java source root).
   Mixin classes in `src/main/java` will target the real vanilla classes.
4. **Obfuscation is the real risk.** The AT mixes SRG and MCP names
   (`func_70835_a`/`becomeAngryAt`, `field_70843_d`/`allySummonCooldown`), which is
   the "one resolves in dev, one in obf" split to be careful with. In a 1.7.10 mixin,
   define the members using the **MCP name**. **This was validated during
   implementation** — the generated refmap maps `becomeAngryAt → func_70835_c(...)`,
   `allySummonCooldown → field_70843_d`, and `field_150939_a → field_150939_a`.

### Resulting mixin interfaces (sketch)

```java
// EntityPigZombieMixin.java
@Mixin(EntityPigZombie.class)
public interface EntityPigZombieMixin {
    @Invoker("becomeAngryAt")
    void becomeAngryAt(Entity entity);
}

// EntitySilverfishMixin.java
@Mixin(EntitySilverfish.class)
public interface EntitySilverfishMixin {
    @Accessor("allySummonCooldown")
    int getAllySummonCooldown();
    @Accessor("allySummonCooldown")
    void setAllySummonCooldown(int value);
}

// ItemBlockMixin.java
@Mixin(ItemBlock.class)
public interface ItemBlockMixin {
    @Accessor("field_150939_a")
    void setBlockInstance(Block block);
}
```
---

## 3. Q2 — Efficiency opportunities in the raw-ore mining path

All in `BlockNetherOres.java`. Ordered roughly by impact.

> **Applied:** Items 1–3 below were fixed in `BlockNetherOres.java` +
> `NetherOresCore.java`, along with the `Ores.values()` caching (item 3 of the old
> plan). Item 4 (drop-logic consolidation) is the only one reviewed but **not**
> applied (separate Forge vs vanilla call paths; deferred). Items 5–6 were
> intentionally left as-is (low impact / micro).

### 1. The "negative cache" is never retained — biggest issue

`findRawOreStack` (`BlockNetherOres.java:214`) uses
`rawCache.compute(..., existing -> existing.isPresent() ? existing : re-resolve)`.
Any ore that resolves to `Optional.empty()` (no matching Ore-Dictionary entry yet)
triggers a **full re-resolution on every single block break**:
`OreDictionary.getOres(...)`, config parsing, string allocation, and registry
lookups.

Additionally, the cache is prefilled in `NetherOresCore.preInit`
(`NetherOresCore.java:142`), **before other mods register their OreDictionary
entries** — so at prefill time most raw items resolve empty, and the "refresh
empties" policy throws away the prefill's benefit. Until a key is populated, the
hot mining path re-runs all resolving logic per hit.

**Fix:** once a key is resolved to anything (even `Optional.empty()`), treat it as
final — cache the empty result (e.g. `computeIfAbsent`/`putIfAbsent`). Optionally
also move the prefill to `postInit` (`NetherOresCore.java:161`), after other mods
register ores, so the cache fills with real hits instead of empty placeholders that
get recomputed.

### 2. Config preferred-mod order is parsed on every miss

Inside `resolveRawForOre` (`BlockNetherOres.java:124-137`) the preferred-mod array
is built and the comma-delimited `preferredModOrder` config string is
fetched/split each time it resolves. And `choosePreferredFromOreDict`
(`BlockNetherOres.java:41-64`) calls `.toLowerCase()` and `.split(":")` **per
ItemStack, per call**. Combined with issue #1, all of this repeats on the hot path.

**Fix:** parse and precompute the preferred-mod `String[]` once at config load into
a static cached field (plus a pre-lowercased copy). The ore-dict matcher then only
compares against precomputed arrays — no per-call splitting/lowercasing.

### 3. `Ores.values()` is called repeatedly

Invoked in `getDrops`, `getItemDropped`, `findRawOreStack`, `resolveRawForOre`,
`prefillRawCache`, and `ItemBlockNetherOre` (multiple times each).
`Enum.values()` allocates a fresh array copy on every call.

**Fix:** hoist a `private static final Ores[] ALL = Ores.values();` and reuse it
everywhere. Minor but free.

### 4. Drop logic is copy-pasted between methods

`getItemDropped` + `quantityDropped`/`quantityDroppedWithBonus` and the `getDrops`
override each independently redo the "special-vanilla-or-raw" lookup and the
base/fortune math (`BlockNetherOres.java:266-338`). They are separate runtime paths
(Forge's block break uses `getDrops`), so not strictly double computation, but it is
duplicated, easy to drift, and each path re-hits the cache independently.

**Fix:** converge on a single static helper (resolved `ItemStack` + quantity math)
so the cache is hit once per lookup and the code stays consistent.

### 5. Minor redundancy: `resolveRawForOre` Iridium special-case

The special-cased `GameRegistry.findItemStack("IC2","itemShardIridium")` lookup
(`BlockNetherOres.java:73-81`) runs *and* the generic `rawIridium`/fallback keys
also run when it fails. Fine if intentional, but the block is worth consolidating
into the same cached path so it also benefits from fix #1.

### 6. Micro: cache keying / boxing

`rawCache` is `ConcurrentMap<Integer, Optional<ItemStack>>` (autoboxed int keys,
`Optional` wrappers). `ConcurrentHashMap` is correct for the threading here, but a
size-fixed primitive-keyed lookup (keys are a small, bounded set from `Ores`) is an
option. The `Optional`/boxing overhead is negligible versus the above; lowest
priority.
---

## 4. Implementation status

1. **Enable UniMixins**: set `usesMixins = true` in `gradle.properties` and add the
   mixin config/boilerplate. — **DONE** (`usesMixins = true`, mixin loaders + JSON
   configs in `src/main/resources/`).
2. **Migrate AT + reflection to mixins**: convert the 3 sites to `@Accessor`/`@Invoker`
   interfaces; delete `netherores_at.cfg` and the `accessTransformersFile` line. —
   **DONE** (see Section 2).
3. **Optimize `BlockNetherOres`**: retain negative-cache results (`computeIfAbsent`),
   move `prefillRawCache()` to `postInit`, precompute the preferred-mod order once. —
   **DONE** (see Section 3, all sub-items executed).
4. **Cleanup**: cache `Ores.values()`, consolidate the drop logic. —
   **PARTIALLY DONE**: `Ores.values()` is now cached (`ALL`) on the hot path.
   **REMAINING**: consolidate the copy-pasted drop logic between `getDrops` and
   `getItemDropped`/`quantityDropped*` into a shared helper (see Section 3, item 4).

---

## 5. Files touched / referenced

| Path | Role |
|------|------|
| `src/main/resources/META-INF/netherores_at.cfg` | **DELETED** (was the NetherOres access transformer) |
| `gradle.properties` | `usesMixins = true`; `accessTransformersFile` commented out |
| `src/main/java/powercrystals/netherores/mixins/Mixins.java` | Registers `EntityPigZombieMixin`, `EntitySilverfishMixin`, `ItemBlockMixin` (EARLY/common) |
| `src/main/java/powercrystals/netherores/mixins/early/EntityPigZombieMixin.java` | `@Invoker("becomeAngryAt")` (new) |
| `src/main/java/powercrystals/netherores/mixins/early/EntitySilverfishMixin.java` | `@Accessor("allySummonCooldown")` (new) |
| `src/main/java/powercrystals/netherores/mixins/early/ItemBlockMixin.java` | `@Accessor("field_150939_a")` (new) |
| `src/main/java/powercrystals/netherores/ores/BlockNetherOres.java` | `angerPigmen` caller (`@Invoker`), drop/raw-cache optimization |
| `src/main/java/powercrystals/netherores/ores/BlockNetherOverrideOre.java` | `ItemBlockMixin.setBlockInstance` (was `ObfuscationReflectionHelper`) |
| `src/main/java/powercrystals/netherores/entity/EntityHellfish.java` | `EntitySilverfishMixin` accessor (was `super.allySummonCooldown`) |
| `src/main/java/powercrystals/netherores/NetherOresCore.java` | `prefillRawCache()` moved to `postInit`; `setPreferredModOrder` call |
| `src/main/java/powercrystals/netherores/mixins/TargetMods.java` | Unused boilerplate placeholder (safe to delete) |
| `net/minecraft/entity/monster/EntityPigZombie.java` | Reference decompile (not compiled) |
| `net/minecraft/entity/monster/EntitySilverfish.java` | Reference decompile (not compiled) |