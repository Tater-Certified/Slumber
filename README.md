# Slumber
Let the server rest while there are no players online.

This mod allows you to freeze your server when no players are online.

Minecraft version before 1.20.3 requires [fabric-carpet](https://github.com/gnembon/fabric-carpet).

## Commands
`/slumber` displays current status whether it is frozed or not, it has the following output.
- **Enabled** Tells if the mod is enabled or not.
- **Frozen** Check if the server is frozed `/tick freeze` also affect this message.
- **Deeply** If true, chunk loading is also removed.

`/slumber true` enables freezing.

`/slumber false` disables freezing.

## Configuration file
Located at <SERVER_DIR>/config/slumber.properties

The syntax is key=value

`value` should have type same as describe below.

| Key | Type | Default Value | Description |
| --- | ---- | ------------- | ----------- |
| freeze-delay-seconds | integer | 20 | Time (in seconds) to freeze after no player left in the server |
| debug-messages | boolean | false | Show message in server log when game is frozen or unfrozen |
| config-version | string | 1.2 | Version of the config |
| safe-starting | boolean | true | Freeze when the terrain is loading to prevent redstone mechine from breaking. see [#8](https://github.com/Tater-Certified/Slumber/issues/8#issuecomment-1684173850). |
| complete-freeze | boolean | false | Remove chunk loading, but useless as slumber is used when no one is online. see [#10](https://github.com/Tater-Certified/Slumber/issues/10#issuecomment-1995925344). |
| toggle | boolean | true | Enable freeze by default |
