# Nerfstick

**Nerfstick** is a Paper plugin that provides controlled access to the vanilla Debug Stick through permission checks and region-aware restrictions.

It is designed to allow Debug Stick usage in Survival environments while enforcing strict validation of block state mutations.

---

## Overview

The plugin intercepts Debug Stick interactions and evaluates each attempted block state change against configured rules.

It supports:

* Permission-based access control
* Region/protection-aware restrictions
* Per-block and per-property limitations
* Safe validation of block state transitions prior to application

---

## Features

* Debug Stick usage control in Survival mode
* Permission-driven authorization model
* Block and block-property level restrictions
* Integration hooks for protection systems (e.g. WorldGuard, claim plugins)
* Configurable behavior per block type
* Paper 1.21+ compatible implementation

---

## Internal Behavior

All Debug Stick interactions are processed through a validation layer before any modification is applied.

Validation checks include:

* Player permissions
* Block type restrictions
* Property-level access rules
* Region/protection constraints (if applicable integrations are present)

If any condition fails, the interaction is denied and no state change is performed.

---

## Permissions

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

## Example permissions

### Block-level access

```
nerfstick.use.minecraft.barrel.*
nerfstick.use.minecraft.bell.*
nerfstick.use.minecraft.furnace.*
nerfstick.use.minecraft.lever.*
nerfstick.use.minecraft.note_block.*
nerfstick.use.minecraft.rail.*
nerfstick.use.minecraft.observer.*
```

### Property-level access

```
nerfstick.use.minecraft.dispenser.facing
nerfstick.use.minecraft.dropper.facing
```

---

## Compatibility

The plugin exposes hooks for integration with region and protection systems, including:

* WorldGuard
* GriefPrevention
* Other claim/region protection plugins providing applicable APIs

---

## Requirements

* Paper 1.21+
* Java 21+

---

### Changes from upstream

* Updated for Paper 1.21+ API
* Removal of deprecated NMS usage
* Revised permission resolution logic
* Hardened block state validation pipeline
* Improved compatibility with modern protection systems
