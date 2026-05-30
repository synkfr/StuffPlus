# Configuration Reference

`Stuff+` utilizes **Okaeri Config** to generate, validate, and dynamically update configurations. All configurations feature full hex color and gradient support via **Adventure MiniMessage** (e.g., `<gradient:#FF5F6D:#FFC371>Gradients</gradient>` or `<color:#00E262>Hex Colors</color>`).

---

## Plugin Configuration (`config.yml`)

The primary configuration file manages database setups and administrative vanish settings:

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

### Config Mappings Explained

* **`storage-type`**: Choose between local `sqlite` (zero configuration required) or a centralized remote `mysql` cluster.
* **`mysql-*`**: Mappings for your database connections. Powered by an isolated **HikariCP** connection pool for maximum performance and asynchronous safety.
* **`vanish-silent-container-clicks`**: If enabled, vanished staff can open chests, barrels, and furnaces silently, and container opening/closing animations are suppressed.
* **`vanish-ignore-pressure-plates`**: Toggles plate trigger suppression. Staff will not trigger physical stone, wood, gold, or iron plate interactions.
* **`vanish-disable-mob-targeting`**: Prevents monsters and hostile entities from targeting vanished staff.
* **`vanish-disable-item-pickup`**: Blocks vanished staff from picking up floor items to prevent accidental inventory pollution or visual reveals.

---

## Message Configuration (`messages.yml`)

This file allows you to customize all system messages with rich color styling:

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
