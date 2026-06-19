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

---

## 🙏 Credits

Nerfstick was originally created by **akdukaan** and **Reimnop**.

* [Original GitHub Repository](https://github.com/akdukaan/Nerfstick?utm_source=chatgpt.com)
* [Modrinth Project Page](https://modrinth.com/plugin/nerfstick?utm_source=chatgpt.com)

This version is a maintained / updated fork with improvements for modern Paper (1.21+) compatibility.
