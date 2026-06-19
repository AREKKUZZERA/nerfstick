# 🧪 Nerfstick

**Nerfstick** is a modern Paper plugin that gives server owners fine-grained control over the vanilla Debug Stick.

It allows safe usage of the Debug Stick in Survival mode while enforcing strict rules for block state modifications, including region-based restrictions and permission-driven access control.

---

## ✨ Features

* 🧪 Safe Debug Stick usage in Survival mode
* 🔐 Fully permission-based access system
* 🧱 Granular control over blocks and block properties
* 🌍 Integration support for protection plugins (WorldGuard, claim systems, etc.)
* ⚙️ Per-block and per-property configuration
* 🚀 Optimized for Paper 1.21+

---

## 🧠 How it works

Nerfstick intercepts Debug Stick interactions at runtime and validates every attempted block state change against server-defined rules.

This allows server owners to:

* Prevent Debug Stick usage in protected regions
* Restrict specific blocks from being modified
* Limit editable block properties (e.g. facing, powered, waterlogged)
* Allow controlled block state cycling in Survival environments

All modifications are validated before being applied, ensuring no unintended state changes occur.

---

## 🔐 Permissions

### Base usage

```
nerfstick.use.minecraft.*
nerfstick.use.minecraft.lever.*
nerfstick.use.minecraft.furnace.facing
```

### Global bypass (optional)

```
minecraft.debugstick.always
```

---

## 🧱 Example permission nodes

```
nerfstick.use.minecraft.barrel.*
nerfstick.use.minecraft.bell.*
nerfstick.use.minecraft.furnace.*
nerfstick.use.minecraft.lever.*
nerfstick.use.minecraft.note_block.*
nerfstick.use.minecraft.rail.*
nerfstick.use.minecraft.observer.*
```

### Property-level control

```
nerfstick.use.minecraft.dispenser.facing
nerfstick.use.minecraft.dropper.facing
```

---

## 🌍 Compatibility

Nerfstick provides hooks for popular protection systems, including:

* WorldGuard
* GriefPrevention
* Other region/claim-based protection plugins

---

## 📦 Requirements

* Paper 1.21+
* Java 21+

---

## 🔄 Fork status

This project is a maintained fork of the original Nerfstick plugin.

### Improvements

* Updated for Paper 1.21+ API
* Removed deprecated NMS usage
* Improved permission resolution system
* Safer and more consistent block state validation
* Better compatibility with modern protection plugins
