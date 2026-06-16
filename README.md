# Slumber
Let the server slumber while there are no players online.

This mod allows you to freeze your server when no players are online.

Minecraft versions before 1.20.3 require [fabric-carpet](https://github.com/gnembon/fabric-carpet).

## Commands
`/slumber` displays current status whether it is in slumber or not, it has the following output.
- **Enabled** Tells if the mod is enabled or not.
- **Frozen** Check if the server is frozen `/tick freeze` also affect this message.

`/slumber true` enables freezing.

`/slumber false` disables freezing.

## Configuration file
Located at <SERVER_DIR>/config/slumber.properties

The syntax is key=value

`value` should have type same as describe below.

| Key                  | Type    | Default Value | Description                                                                                 |
|----------------------|---------|---------------|---------------------------------------------------------------------------------------------|
| freeze-delay-seconds | integer | 20            | Time (in seconds) to slumber after no player left in the server                             |
| debug-messages       | boolean | false         | Show message in server log when game is in slumber or not                                   |
| config-version       | string  | 1.4           | Version of the config                                                                       |
| safe-starting        | boolean | true          | Freeze when the terrain is loading to prevent redstone machines from breaking               |
| sleep_tick_speed     | double  | 0.0           | Sets the TPS that should be used when the server is in slumber. 0.0 means a complete freeze |
| sleep_autosaving     | boolean | false         | Enable autosaving when the server is in slumber                                             |
| toggle               | boolean | true          | Enable slumber by default                                                                   |

# Supported Platforms (As of v2.0.0)
- Fabric/Quilt (1.14.3 - 26.2)
- Forge (26.2 - )
- NeoForge (26.2 - )
- PaperMC/Spigot/Folia (26.2 - )
- Sponge (26.2 - )

# Installation
## Fabric, Quilt, Forge, NeoForge
Simply put the mod in the mods folder
## Sponge
Simply put the plugin in the plugins folder
## Spigot/PaperMC
1. Install the [Ignite](https://github.com/vectrix-space/ignite) Mixin loader
2. Run the ignite jar alongside the paper/spigot jar
3. Put the mod in the mods folder and restart
## Folia
1. Install the [Ignite](https://github.com/vectrix-space/ignite) Mixin loader
2. Rename the Folia jar to "paper.jar". Alternatively, you can launch the game with the following JVM args: `-Dignite.locator=paper -Dignite.jar=./folia.jar`
3. Run the ignite jar alongside the folia jar
4. Put the mod in the mods folder and restart
