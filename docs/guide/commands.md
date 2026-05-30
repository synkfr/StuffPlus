# Commands & Permissions

`Stuff+` provides a complete command and permission system mapping administrative actions and utilities. Commands are split into **network-wide** (available on both Paper and Velocity) and **Paper-only** (server-side features).

---

## Network-Wide Commands (Paper & Velocity)

These commands are available on **both** Paper/Purpur/Folia servers and Velocity proxies. When using a shared MySQL database, punishments issued on either platform are enforced network-wide.

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
| `/history` | Views complete historical punishment logs. | `/history <player>` | `stuff.history` |
| `/staffhistory` | Audits punishments issued by a staff member. | `/staffhistory <staff>` | `stuff.staffhistory` |
| `/staffrollback` | Revokes all active punishments placed by a staff member. | `/staffrollback <staff> [confirm]` | `stuff.staffrollback` |
| `/stuffallow` | Exempts a player/UUID from active IP bans. | `/stuffallow <player> [remove]` | `stuff.stuffallow` |
| `/stuffimport` | Imports punishments from other moderation plugins. | `/stuffimport <source> [params...]` | `stuff.import` |

---

## Paper-Only Commands

These commands are available **only** on Paper/Purpur/Folia servers. They require server-side features (inventories, gamemode, spectator mode) that are not available on a proxy.

| Command | Description | Default Usage | Permission Node |
| :--- | :--- | :--- | :--- |
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
* **`stuff.vanish.see`**: Allows staff to see other vanished players in the game, in tab lists, and list them in command autocompletions. *(Paper only)*

---

## Overwrite Hierarchy Weights

`Stuff+` implements a robust Administrative Overwrite Hierarchy. Lower-ranking staff members cannot bypass, overwrite, or remove punishments issued by senior administrators.

Weights are assigned using custom permission nodes:
* **`stuff.hierarchy.weight.<number>`**: Defines the hierarchy weight of the player (e.g. `stuff.hierarchy.weight.100` for Owners, `stuff.hierarchy.weight.50` for Helpers).
* **Console Authority**: The Server Console holds infinite authority (`Integer.MAX_VALUE`). Console-issued bans, mutes, or warnings can never be overwritten in-game by any player, regardless of rank.
* **Database Cache**: Dynamic weights of staff members are automatically resolved and cached in the database on join, enabling offline hierarchy protection for senior staff.

::: tip Velocity Hierarchy
On Velocity, hierarchy weights are also enforced through the same database-backed weight system. Permission nodes work with Velocity's permission providers (e.g., LuckPerms Velocity).
:::

---

## Command Aliases

Many commands have shorthand aliases for convenience:

| Command | Aliases |
| :--- | :--- |
| `/mute` | `/silence` |
| `/ip-ban` | `/ipban`, `/banip` |
| `/tempip-ban` | `/tempipban`, `/tempbanip` |
| `/unip-ban` | `/unipban`, `/unbanip` |
| `/vanish` | `/v`, `/vmode` |
| `/monitor` | `/spectate`, `/mon` |
| `/invsee` | `/inspect`, `/inv` |
| `/fly` | `/flight` |
| `/history` | `/punishhistory`, `/historylog` |
| `/staffrollback` | `/rollbackstaff`, `/rollback` |
| `/stuffallow` | `/allowip`, `/allow` |
| `/stuffimport` | `/migrate`, `/stuffmigrate` |
