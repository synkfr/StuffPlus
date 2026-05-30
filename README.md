# Stuff+ — Modern Moderation & Administrative Utilities

Stuff+ is a lightweight, high-performance moderation and staff utility plugin built for modern Minecraft servers. It works perfectly out of the box on standard **Spigot** and **Paper** servers, and is fully optimized for multi-threaded **Folia** servers to keep your server running lag-free.

With Stuff+, server administrators and staff get access to advanced moderation actions and monitoring tools without ever having to worry about server stutter, lag spikes, or crashes.

---

## Key Features

### 1. Built-in Folia & Lag Safety
* **Lag-Free Actions**: All player checks, database actions, and commands run in the background, keeping your server TPS stable at a constant 20.0.
* **Safe Teleportation**: Standard teleports are replaced with safe background chunk loading, preventing server freeze frames when staff move around.

### 2. Ultimate Vanish (/vanish)
* **100% Invisible**: Hides staff completely from players, online counts, player ping lists, and command autocompletions.
* **Smart Interaction Bypass**: Vanished staff bypass physical pressure plates and tripwire traps.
* **Silent Containers**: Open chests, barrels, and furnaces silently without playing container audio or chest opening animations.
* **Full Invisibility HUD**: A clean real-time action bar HUD always shows when your vanish mode is active.
* **Extra Stealth**: Prevents floor item pickups, hides your join/quit messages, and blocks mobs from targeting or attacking you.

### 3. Full Moderation Suite
* **All-in-One Commands**: Includes `/ban`, `/tempban`, `/ip-ban`, `/tempip-ban`, `/mute`, `/tempmute`, and `/warn`.
* **Warning Logs**: Easily track, clear, and review player warning logs via `/warns <player> [list/clear]`.
* **Smart Time Parsing**: Supports easy-to-understand time inputs (e.g., `1d` for a day, `12h` for twelve hours, `30m` for thirty minutes, or `perm` for permanent).
* **Automatic Database Backups**: Automatically saves moderation logs. Supports local SQLite databases (works out of the box with zero setup) and MySQL databases.

### 4. Smooth Spectator Follow (/monitor)
* **Cinematic Camera Follow**: Watch and automatically follow players smoothly in spectator mode.
* **Jitter-Free Movement**: The camera predicts where the target player is moving and teleports slightly ahead, giving you a smooth, lag-free view.
* **Safe Boundaries**: Fly around freely! You can orbit the player up to 10 blocks away before being gently pulled back.

### 5. Real-Time Inventory Inspector (/invsee)
* **54-Slot Interactive Menu**: View and change a target player's inventory slots (0-35), armor, off-hand items, and stats in real-time.
* **Live Stats HUD**:
  * **Golden Apple**: Hover to view health, hunger level, and experience level.
  * **Potion Bottle**: Hover to view all active potion effects and remaining durations.
  * **Ender Chest**: Click this shortcut item to inspect their Ender Chest directly.
* **100% Crash-Proof**: Fully redesigned to run safely on multi-threaded servers, preventing world/block reading errors completely.

### 6. Fast Staff Shortcuts
* **Instant Gamemodes**: Rapidly shift gamemodes using `/gmc` (creative), `/gms` (survival), `/gmsp` (spectator), and `/gma` (adventure).
* **Flight Toggle**: Quickly toggle flight mode on or off for players using `/fly`.

### 7. Telemetry & Analytics
* **Anonymous Statistics**: Supports bStats (ID: 31675) to anonymously track server counts and player deployment metrics, helping us improve the plugin. Relocated internally to prevent conflicts.

---

## Command & Permission Reference

| Command | Description | Usage | Permission Node |
| :--- | :--- | :--- | :--- |
| `/ban` | Bans a player from the server. | `/ban <player> [reason]` | `stuff.ban` |
| `/tempban` | Temporarily bans a player. | `/tempban <player> <duration> [reason]` | `stuff.tempban` |
| `/unban` | Unbans a player. | `/unban <player>` | `stuff.unban` |
| `/ip-ban` | IP-bans a player. | `/ip-ban <player> [reason]` | `stuff.ipban` |
| `/tempip-ban` | Temporarily IP-bans a player. | `/tempip-ban <player> <duration> [reason]` | `stuff.tempipban` |
| `/unip-ban` | Unbans an IP address or player. | `/unip-ban <player/IP>` | `stuff.unipban` |
| `/mute` | Mutes a player in chat. | `/mute <player> [reason]` | `stuff.mute` |
| `/tempmute` | Temporarily mutes a player. | `/tempmute <player> <duration> [reason]` | `stuff.tempmute` |
| `/unmute` | Unmutes a player. | `/unmute <player>` | `stuff.unmute` |
| `/warn` | Warns a player. | `/warn <player> [reason]` | `stuff.warn` |
| `/warns` | View or clear warning history. | `/warns <player> [list/clear]` | `stuff.warns` |
| `/vanish` | Toggle complete invisibility. | `/vanish` | `stuff.vanish` |
| `/invsee` | Inspect a player's inventory live. | `/invsee <player>` | `stuff.invsee` |
| `/monitor` | Spectate and smoothly follow a player. | `/monitor <player/leave>` | `stuff.monitor` |
| `/fly` | Toggle player flight. | `/fly [player]` | `stuff.fly` |
| `/gmc` | Switch to creative mode. | `/gmc [player]` | `stuff.gmc` |
| `/gms` | Switch to survival mode. | `/gms [player]` | `stuff.gms` |
| `/gmsp` | Switch to spectator mode. | `/gmsp [player]` | `stuff.gmsp` |
| `/gma` | Switch to adventure mode. | `/gma [player]` | `stuff.gma` |

> **Admin Permission**: The permission node `stuff.admin` grants access to all commands and features of Stuff+ by default.
> **Vanish Bypass**: Staff with the permission `stuff.vanish.see` can see other vanished players in-game and list them in completions.

---

## Configuration Files

The plugin generates clean, easy-to-customize configurations. All messages support custom hex colors and text styling.

### `config.yml`
```yaml
# Storage type: SQLITE or MYSQL
storage-type: "sqlite"

# MySQL Connection Details (only used if storage-type is MYSQL)
mysql-host: "localhost"
mysql-port: 3306
mysql-database: "minecraft"
mysql-username: "root"
mysql-password: "password"
mysql-pool-size: 10
mysql-use-ssl: false

# Vanish settings
vanish-silent-container-clicks: true
vanish-ignore-pressure-plates: true
vanish-disable-mob-targeting: true
vanish-disable-item-pickup: true
```

### `messages.yml`
```yaml
# Prefix for all plugin messages
prefix: "<color:#A0A0A0>[<color:#00E262>Stuff+<color:#A0A0A0>] "

no-permission: "<color:#E20000>You do not have permission to execute this command."
player-only: "<color:#E20000>This command can only be executed by a player."
player-not-found: "<color:#E20000>Player '{player}' has not been found or registered."
invalid-duration: "<color:#E20000>Invalid duration format! Use e.g. 1d, 12h, 30m or perm."

# Mutes
player-muted: "<color:#00E262>You have muted {player} for {time}. Reason: {reason}"
player-muted-broadcast: "<color:#E2B700>{player} has been muted by {sender} for {time}. Reason: {reason}"
you-are-muted: "<color:#E20000>You are muted! Remaining time: {time}. Reason: {reason}"

# Bans
player-banned: "<color:#00E262>You have banned {player} for {time}. Reason: {reason}"
player-banned-broadcast: "<color:#E20000>{player} has been banned by {sender} for {time}. Reason: {reason}"
ban-kick-message: "<color:#E20000>You have been banned from the server!\n\nReason: {reason}\nExpiry: {time}"

# Vanish
vanish-enabled: "<color:#00E262>You are now vanished."
vanish-disabled: "<color:#00E262>You are no longer vanished."
vanish-action-bar: "<color:#00E262>★ YOU ARE VANISHED ★"
```

---

## Build & Compilation

To build the plugin and compile the final JAR from source:

### Requirements
* **Java 21** or higher
* **Gradle** (installed or via wrapper)

### Build Command
Run this command in the project root:
```bash
gradle shadowJar
```

The compiled plugin will be located at:
```
build/libs/Stuff-1.0.0.jar
```
