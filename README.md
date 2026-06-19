# Nerfstick

**Nerfstick** is a Paper plugin that provides controlled access to the vanilla Debug Stick through permission-based rules and optional region-aware restrictions.

It ensures Debug Stick interactions are validated before any block state mutation is applied.

---

## Overview

Nerfstick intercepts Debug Stick usage and evaluates every interaction against a defined permission and rule set.

The system enforces:

* Permission-based authorization
* Block-level and property-level restrictions
* Optional integration with region/protection plugins
* Pre-application validation of all block state changes

---

## Features

* Debug Stick control in Survival mode
* Fine-grained permission system
* Block and property-level access control
* Protection plugin hooks (WorldGuard, claim systems, etc.)
* Configurable whitelist system
* Paper 1.21+ compatible

---

## 🔐 Permissions

### Base permissions

```
nerfstick.use.minecraft.*
nerfstick.use.minecraft.lever.*
nerfstick.use.minecraft.furnace.facing
```

### Global bypass

```
minecraft.debugstick.always
```

---

## 🧱 Recommended whitelist (blocks)

```
nerfstick.use.minecraft.barrel.*
nerfstick.use.minecraft.bell.*
nerfstick.use.minecraft.furnace.*
nerfstick.use.minecraft.ladder.*
nerfstick.use.minecraft.lectern.*
nerfstick.use.minecraft.lever.*
nerfstick.use.minecraft.lightning_rod.*
nerfstick.use.minecraft.note_block.*
nerfstick.use.minecraft.observer.*
nerfstick.use.minecraft.rail.*
nerfstick.use.minecraft.redstone_comparator.*
nerfstick.use.minecraft.tripwire_hook.*
nerfstick.use.minecraft.redstone_lamp.*
nerfstick.use.minecraft.ender_chest.*
nerfstick.use.minecraft.dispenser.facing
nerfstick.use.minecraft.dropper.facing
nerfstick.use.minecraft.dried_ghast.facing
nerfstick.use.minecraft.leaf_litter.facing
nerfstick.use.minecraft.wildflowers.facing
```

---

## 🌿 Block families (regex-based permissions)

```
r=nerfstick.use.minecraft.*_chest.*
r=nerfstick.use.minecraft.*_fence.*
r=nerfstick.use.minecraft.*_gate.*
r=nerfstick.use.minecraft.*_glazed_terracotta.*
r=nerfstick.use.minecraft.*_lantern.*
r=nerfstick.use.minecraft.*_leaves.*
r=nerfstick.use.minecraft.*_log.*
r=nerfstick.use.minecraft.*_rail.*
r=nerfstick.use.minecraft.*_repeater.*
r=nerfstick.use.minecraft.*_sign.*
r=nerfstick.use.minecraft.*_stairs.*
r=nerfstick.use.minecraft.*_slab.*
r=nerfstick.use.minecraft.*_trapdoor.*
r=nerfstick.use.minecraft.*_wall.*
r=nerfstick.use.minecraft.*_door.hinge
```

---

## 🌍 Compatibility

Nerfstick can optionally integrate with:

* WorldGuard
* GriefPrevention
* Other region/claim protection plugins providing APIs for event interception

---

## 📦 Requirements

* Paper 1.21+
* Java 21+

---

### Changes from upstream

* Updated for Paper 1.21+ API
* Removed deprecated NMS usage
* Revised permission resolution logic
* Improved validation pipeline for block state changes
* Enhanced compatibility with modern protection plugins
* **Fixed:** enum-valued block properties (`facing`, `axis`, `half`, `shape`,
  `hinge`, `instrument`, `attachment`, `orientation`, `rotation`, ...) — these
  previously did nothing on right-click; they now cycle correctly through every
  legal value for the block.
* **Fixed:** the `north`/`east`/`south`/`west`/`up`/`down` boolean faces on
  fences, glass panes, walls, and glow lichen, which use a different Bukkit API
  (`MultipleFacing`) than every other property.
* **Fixed:** the `nerfstick.use.minecraft.<block>.<state>` permission whitelist
  from the README was defined but never actually checked — every player could
  edit every property regardless of permissions. It is now enforced per
  property: unauthorized properties are filtered out of the selection cycle
  entirely.
* **Fixed:** a crash (`ArrayIndexOutOfBoundsException`) in permission
  resolution for any block ID without an explicit namespace.
* Added `softdepend` on WorldGuard/GriefPrevention so protection checks are
  reliable regardless of plugin load order.
* Added a lightweight, notification-only update checker (Modrinth, falling
  back to GitHub Releases) — see **Updates** below.

---

## 🔄 Updates

On startup, Nerfstick checks Modrinth (and GitHub Releases as a fallback) once,
asynchronously, for a newer version. If one is found:

* It's logged to console.
* Players with `nerfstick.admin` (defaults to server operators) get a clickable
  chat link on join.

It **never downloads or replaces the plugin jar automatically** — silently
swapping binaries on a running server has no checksum/signature verification
and risks corruption mid-write, so updates always stay a manual, deliberate
action by the operator. You can disable the check entirely in `config.yml`:

```yaml
check-for-updates: false
```

---

## 🙏 Credits

Nerfstick was originally created by **akdukaan** and **Reimnop**.

* [Original GitHub Repository](https://github.com/akdukaan/Nerfstick?utm_source=chatgpt.com)
* [Modrinth Project Page](https://modrinth.com/plugin/nerfstick?utm_source=chatgpt.com)

This version is a maintained / updated fork with improvements for modern Paper (1.21+) compatibility.
