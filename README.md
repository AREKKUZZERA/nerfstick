# Nerfstick

**Nerfstick** is a Paper plugin that provides controlled access to the vanilla Debug Stick through permission-based manipulation categories and optional region-aware restrictions.

It ensures Debug Stick interactions are validated before any block state mutation is applied.

---

## Overview

Nerfstick intercepts Debug Stick usage and evaluates every interaction against a defined permission and rule set.

The system enforces:

* Permission-based authorization
* Manipulation-category restrictions
* Optional integration with region/protection plugins
* Pre-application validation of all block state changes

---

## Features

* Debug Stick control in Survival mode
* Fine-grained permission system
* Permission categories for groups of block-state manipulations
* Protection plugin hooks (WorldGuard, claim systems, etc.)
* Configurable whitelist system
* Paper 1.21+ compatible

---

## 🔐 Permissions

### Base permissions

```
nerfstick.use
```

Allows the player to use Nerfstick's controlled Debug Stick handler.

### Manipulation permissions

```
nerfstick.manipulation.direction
nerfstick.manipulation.openable
nerfstick.manipulation.power
nerfstick.manipulation.water
nerfstick.manipulation.shape
nerfstick.manipulation.level
nerfstick.manipulation.multi_face
nerfstick.manipulation.misc
nerfstick.manipulation.*
```

These permissions are intentionally not per block. A permission grants a type of
block-state manipulation across all blocks that expose that state:

* `direction` — `facing`, `rotation`, `axis`, `orientation`
* `openable` — `open`, `hinge`, `half`, door-like states
* `power` — `powered`, `lit`, `enabled`, `triggered`, comparator mode/power
* `water` — `waterlogged`
* `shape` — `shape`, `type`, `thickness`, `attachment`, `face`, similar form states
* `level` — numeric states such as `age`, `level`, `delay`, `distance`, `layers`
* `multi_face` — `north`, `east`, `south`, `west`, `up`, `down` connection toggles
* `misc` — remaining vanilla block states that do not fit a dedicated category

### Global bypass

```
minecraft.debugstick.always
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
* Replaced per-block/per-property permissions with manipulation-category permissions
* Improved validation pipeline for block state changes
* Enhanced compatibility with modern protection plugins
* **Fixed:** enum-valued block properties (`facing`, `axis`, `half`, `shape`,
  `hinge`, `instrument`, `attachment`, `orientation`, `rotation`, ...) — these
  previously did nothing on right-click; they now cycle correctly through every
  legal value for the block.
* **Fixed:** the `north`/`east`/`south`/`west`/`up`/`down` boolean faces on
  fences, glass panes, walls, and glow lichen, which use a different Bukkit API
  (`MultipleFacing`) than every other property.
* Refactored the plugin into focused services for event handling, block-state
  cycling, permission checks, protection hooks, and update notifications.
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
