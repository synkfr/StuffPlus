# Commands & Permissions

`Stuff+` provides a complete command and permission system mapping administrative actions and utilities.

---

## Administrative & Utility Command List

| Command | Description | Default Usage | Permission Node |
| :--- | :--- | :--- | :--- |
| `/ban` | Permanently bans a player. | `/ban <player> [reason]` | `stuff.ban` |
| `/tempban` | Temporarily bans a player. | `/tempban <player> <duration> [reason]` | `stuff.tempban` |
| `/unban` | Revokes an active ban profile. | `/unban <player>` | `stuff.unban` |
| `/ip-ban` | Permanently IP-bans a target. | `/ip-ban <player> [reason]` | `stuff.ipban` |
| `/tempip-ban` | Temporarily IP-bans a player/IP. | `/tempip-ban <player> <duration> [reason]` | `stuff.tempipban` |
| `/unip-ban` | Revokes an active IP ban profile. | `/unip-ban <player/IP>` | `stuff.unipban` |
| `/mute` | Permanently mutes a player. | `/mute <player> [reason]` | `stuff.mute` |
| `/tempmute` | Temporarily mutes a player. | `/tempmute <player> <duration> [reason]` | `stuff.tempmute` |
| `/unmute` | Restores a player's chat access. | `/unmute <player>` | `stuff.unmute` |
| `/warn` | Issues a warning profile to a player. | `/warn <player> [reason]` | `stuff.warn` |
| `/warns` | View or clear warning logs. | `/warns <player> [list/clear]` | `stuff.warns` |
| `/vanish` | Toggles complete invisibility. | `/vanish` | `stuff.vanish` |
| `/invsee` | Live inspects player inventories. | `/invsee <player>` | `stuff.invsee` |
| `/monitor` | Smoothly spectates and follows players. | `/monitor <player/leave>` | `stuff.monitor` |
| `/fly` | Toggles flight mode for players. | `/fly [player]` | `stuff.fly` |
| `/gmc` | Sets creative gamemode shortcut. | `/gmc [player]` | `stuff.gmc` |
| `/gms` | Sets survival gamemode shortcut. | `/gms [player]` | `stuff.gms` |
| `/gmsp` | Sets spectator gamemode shortcut. | `/gmsp [player]` | `stuff.gmsp` |
| `/gma` | Sets adventure gamemode shortcut. | `/gma [player]` | `stuff.gma` |

---

## Special Wildcard & Bypass Nodes

* **`stuff.admin`**: Grants access to all administrative capabilities and commands of `Stuff+` by default. Typically assigned to OPs and high-tier Administrators.
* **`stuff.vanish.see`**: Allows staff to see other vanished players in the game, in tab lists, and list them in command autocompletions.
