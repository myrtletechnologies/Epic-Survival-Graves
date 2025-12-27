# Epic Survival Graves - Custom Fork

**⚠️ This is a private customized fork for internal server use only.**

This fork is maintained exclusively for use on our Minecraft server and is **not intended for public use**. We provide no support, will not accept pull requests, and make no guarantees about maintenance beyond our own needs.

**Looking for a Graves plugin?** Please use the actively maintained [AvarionMC/graves](https://github.com/AvarionMC/graves) project instead, unless you specifically need the exact customizations made for our server.

## Attribution

This is a fork of [AvarionMC/graves](https://github.com/AvarionMC/graves), maintained by Steven 'KaReL' Van Ingelgem, which itself is a fork of the original plugin by Ranull from his [GitLab repository](https://gitlab.com/ranull/minecraft/graves).

All code is released under GPLv3, same as the original projects.

## Custom Modifications

This fork includes the following changes from the upstream AvarionMC version:

- **Removed:** Obituary functionality
- **Removed:** Graveyard system (commands, permissions, management)
- **Removed:** Right-click to open graves (graves must be broken to access items)
- **Modified:** Version check notifications (shows upstream updates available, but acknowledges custom build)
- **Updated:** Based on v4.9.11 with dependency updates and bug fixes

## Disclaimer

For any plugin-related concerns, feature requests, or support:
- Create your own fork, or
- Direct your inquiries to the [upstream AvarionMC project](https://github.com/AvarionMC/graves)

We will not provide support or manage contributions for this repository.

# What is it?

The **ULTIMATE** full-featured lightweight death chest plugin / player grave plugin! Every feature you could ever need
and more! While still being lightweight and efficient.

## Top Features

* Customizable
* Schematics
* Economy
* Regions
* Placeholders
* Protection
* Zombies
* Corpses
* Models
* Obituary
* Compass
* Head Drops
* Holograms
* Particles
* Tokens
* Blacklisting
* Reload Safe

## Supports

* 1.18.\*, 1.19.\*, 1.20.\*, 1.21.\*
* Spigot, Paper, Purpur, Airplane, Pufferfish, Tuinity, CraftBukkit, CatServer, Mohist, Magma, MultiPaper
* GeyserMC (Bedrock Players)
* Forge/Bukkit Hybrid servers (Mohist, Magma, CatServer)

## Integrations

* Vault (Economy)
* WorldEdit (Schematics)
* WorldGuard (Flags)
* PlaceholderAPI (Placeholders)
* FurnitureLib/DiceFurniture (Furniture)
* FurnitureEngine (Furniture)
* ItemsAdder (Furniture/Blocks)
* Oraxen (Furniture/Blocks)
* ChestSort (Sorting Grave)
* ProtectionLib (Protected Region Detection)
* PlayerNPC (Corpses)

* Towny
* ItemBridge
* MineDown
* MiniMessage
* SimpleClaimSystem

## Screenshots

![Screenshot 1](images/screenshot_1.png)
![Screenshot 2](images/screenshot_2.png)

## Videos

[![Graves plugin](https://img.youtube.com/vi/mq8aoZE6Jl0/0.jpg)](https://www.youtube.com/watch?v=mq8aoZE6Jl0)

**Video by:** _ServerMiner_

## Commands

| Command                                           | what does it do?             |
|---------------------------------------------------|------------------------------|
| **/graves**                                       | Player graves                |
| **/graves help**                                  | Plugin info                  |
| **/graves list** _{player}_                       | List another players graves. |
| **/graves givetoken** _{player} {token} {amount}_ | Give grave token (OP)        |
| **/graves dump**                                  | Dump server information (OP) |
| **/graves debug** _{level}_                       | Change debug level (OP)      |
| **/graves reload**                                | Reload command (OP)          |

## Permissions

    graves.place (Default)
    graves.open (Default)
    graves.break (Default)
    graves.teleport (Default)
    graves.experience (Default)
    graves.autoloot (Default)
    graves.gui (Default)
    graves.gui.other (OP)
    graves.givetoken (OP)
    graves.bypass (OP)
    graves.reload (OP)

## Bug Reports

If you find bugs please report them [here](https://github.com/svaningelgem/graves/issues).

## Usage

![Server usage](https://bstats.org/signatures/bukkit/AvarionGraves.svg)

## Contributing

### How to build

You can build the project using Maven:

```bash
mvn clean install
```

### Testing on a local server

You can automatically copy the built plugin to your local server's plugins directory by specifying the `test.server.path` property:

```bash
mvn clean install -Dtest.server.path=/path/to/your/server/plugins
```

By default, the plugin will be copied to `target/test-server`.

## Upstream Project Links

For the official, actively maintained version:

* **GitHub**: <https://github.com/AvarionMC/graves>
* **Spigot**: <https://www.spigotmc.org/resources/graves.116202/>
* **bStats**: <https://bstats.org/plugin/bukkit/AvarionGraves/21607>
