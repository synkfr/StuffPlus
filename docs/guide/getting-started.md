# Getting Started with Stuff+

`Stuff+` is an enterprise-grade, high-performance moderation and utility plugin built for modern Minecraft networks. It is designed from the ground up to support both **Folia's multi-threaded region architecture** and standard **Paper / Spigot (1.20 - 1.21+)** servers.

---

## Requirements

To run or build `Stuff+` on your local machine or server, you need the following dependencies:

* **Java Runtime Environment**: Java 21 or higher (OpenJDK / Eclipse Temurin recommended).
* **Server Platform**: Folia, Paper, or Spigot (1.20.x, 1.21.x, or higher).
* **Build System** (for developers): Gradle 8+ and JDK 21+ are required for compiling from source.

---

## Installation

1. Grab the latest shaded production release JAR from the [GitHub Releases](https://github.com/synkfr/StuffPlus/releases).
2. Drop the compiled `Stuff-1.0.0.jar` into your server's `plugins/` directory.
3. Start the server to generate the default configuration files.
4. Customize `config.yml` and `messages.yml` inside the `plugins/Stuff/` directory, then reload/restart.

---

## Compilation from Source

If you want to modify the source code or build the latest development snapshot, clone the repository and use Gradle:

```bash
# Clone the repository
git clone https://github.com/synkfr/StuffPlus.git
cd StuffPlus

# Compile and build the relocated shadow JAR
gradle shadowJar
```

The compiled shaded plugin ready for production deployment will be located at:
```
build/libs/Stuff-1.0.0.jar
```

---

## Migrating from Other Plugins

`Stuff+` includes a powerful multi-source administrative migration and import tool. You can migrate mutes, bans, warnings, and IP bans from **7 popular moderation systems**:

1. **Vanilla Minecraft**: Imports bans and IP bans natively from `banned-players.json` and `banned-ips.json` in the server root.
2. **Essentials / EssentialsX**: Scans and parses active ban/mute records inside player profiles under `plugins/Essentials/userdata/*.yml`.
3. **LiteBans**: ConnectsSQLite/SQL structures to migrate historical and active mutes, bans, warnings, and kicks.
4. **AdvancedBan**: Normalizes and imports entries from the legacy SQL `Punishments` table.
5. **MaxBans**: Performs dynamic name-to-UUID resolving and imports bans, IP bans, mutes, and warnings.
6. **BanManager**: Integrates legacy player bans, player mutes, and warning tables.
7. **BungeeAdminTools**: Imports entries from database tables `bat_ban` and `bat_mute`.

Run the `/stuffimport` command in-game to view auto-detected local setups, or see the [Commands Guide](/guide/commands) for more details.

::: info Relocated Dependencies
The build system automatically relocates and packages all required libraries inside the final JAR file, ensuring it runs out of the box with zero conflicts against any other plugins on your server.
:::
