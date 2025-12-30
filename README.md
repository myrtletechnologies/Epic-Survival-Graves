# Epic Survival Graves - Custom Fork

**⚠️ This is an unofficial, custom build of AvarionMC/graves.**

This fork exists solely to run the EnderCartCraft survival world. It is not intended to replace or provide any other server with an alternative to the original project, we provide zero support, and we will decline / not monitor any pull requests.

Only use our version here, if you happen to desire our exact stripped down version of the original plugin. We removed most of the original plugin's cool features and capabilities, for our own nice use-case.

**Looking for a Graves plugin?** Please use the actively maintained [AvarionMC/graves](https://github.com/AvarionMC/graves) project instead, unless you specifically need the exact customizations made for our server.

## Attribution

This is a fork of [AvarionMC/graves](https://github.com/AvarionMC/graves), maintained by Steven Van Ingelgem, which itself is a fork of the original plugin by Ranull from his [GitLab repository](https://gitlab.com/ranull/minecraft/graves).

All code is released under GPLv3, same as the original projects.

## EnderCartCraft direction (build-6)

Our stripped fork keeps only the behaviors EnderCartCraft needs:

- Graves always spawn player heads on survival deaths, with enchantment particles for visual cues.
- Slot-perfect restoration is mandatory: hotbar, main inventory, armor, off-hand, and stored layout all snap back into place. Extra items spill to the ground.
- A flat 10 % XP tax is enforced. Graves store 90 % of the player’s experience and hand it back on loot.
- Placement always attempts the original death spot or a nearby safe block. If we cannot place, we force keep-inventory/keep-xp for that death.
- Protection is simple: only the owner or an operator with `graves.bypass` can loot before timeout.
- Every grave expires after 30 minutes. On timeout, contents drop at the primary head location and the grave block is removed.
- `/graves` is the only command. It lists active graves with world, coordinates, and time remaining.
- PlaceholderAPI is the lone optional integration; we expose placeholders for HUD text but nothing else.
- No configs, recipes, crafting tokens, or importers remain. Every value is hardcoded in `Settings`.
- No telemetry (bStats, metrics, update pings) ships with this build.

## Disclaimer

For any plugin-related concerns, feature requests, or support:
- Create your own fork, or
- Direct your inquiries to the [upstream AvarionMC project](https://github.com/AvarionMC/graves)

We will not provide support or manage contributions for this repository.

# What is it now?

A hardcoded, zero-configuration graves plugin purpose-built for our survival server. Every required behavior is baked
directly into the code:

* Player-head graves always spawn for survival-player deaths and emit enchantment-table particles.
* Graves track exact slot contents (hotbar, inventory grid, armor, and off-hand) and auto-equip on loot.
* Stored experience always returns 90% of the player's balance (10% death tax).
* Graves expire 30 minutes after creation; if placement fails we fall back to keep-inventory semantics.
* `/graves` lists the caller's active graves with coordinates and remaining lifetime.
* PlaceholderAPI expansions are supported when the PlaceholderAPI plugin is present; no other integrations remain.

There are **no** YAML files, commands, or toggles left. If you need a different behavior, fork this repo again and hardcode it.

## Commands

| Command     | Description                               |
|-------------|-------------------------------------------|
| `/graves`   | Lists the caller's active graves inline.  |

## Permissions

| Permission      | Description                                                |
|-----------------|------------------------------------------------------------|
| `graves.bypass` | Allows bypassing grave protection timers when looting.     |

## Upstream Project Links

For the official, actively maintained version:

* **GitHub**: <https://github.com/AvarionMC/graves>
* **Spigot**: <https://www.spigotmc.org/resources/graves.116202/>
