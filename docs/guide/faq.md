# Frequently Asked Questions (FAQ)

This FAQ answers common operational and architectural questions about Staff+'s capabilities, including the interactive GUI, Folia region safety, database synchronization, and LuckPerms-independent hierarchy protection.

---

## 🛡️ Hierarchy Protection

### Q: How does the Hierarchy Protection work? Does it require hooking into LuckPerms?
**No. Staff+ does not require any direct API hooks to LuckPerms.**

Instead, it reads permission nodes natively via Spigot/Paper's standard `getEffectivePermissions()` API.
* **How to assign weight**: Assign staff members the permission node `staff.hierarchy.weight.<number>` (e.g. `staff.hierarchy.weight.50` for moderators or `staff.hierarchy.weight.100` for administrators).
* **Authority checks**: When a punishment is placed, the weight of the punisher is recorded. If another staff member attempts to lift, override, or clear that punishment, Staff+ checks if their weight is equal to or greater than the original punisher.
* **Decoupled design**: Because it relies on standard Bukkit permission checks, this system works out-of-the-box with **any** permissions plugin (LuckPerms, UltraPermissions, GroupManager, etc.).

---

## 📊 Staff Info GUI

### Q: What is `/staff info <player>`?
It is a dynamic, interactive chest GUI that presents comprehensive profile, connection, and moderation statistics for a specific player. This allows staff members to inspect details in busy lobbies without chat flood.

### Q: Does `/staff info` work for offline players?
**Yes.** When a player is offline, Staff+ queries their last known database record to display:
* Last known IP address
* Hierarchy weight
* Total active warnings
* Alt accounts registered under their IP
* Whitelist IP-ban exemption status

Actions that require the player to be online (such as **Teleport**, **Kick**, and **Inspect Inventory**) are visually marked as disabled (using red stained glass panes) and clicking them is blocked. Moderation commands (ban, mute, warn, allow, history logs) remain fully active.

---

## 🧵 Folia & Regional Safety

### Q: What makes Staff+ fully Folia-compatible?
Traditional plugins fail under Folia due to region-threading constraints. Staff+ enforces complete region-safety:
1. **Asynchronous Database & Webhooks**: All database and Discord webhook transactions are run asynchronously via `ForkJoinPool.commonPool()`, preventing any tick lag.
2. **Asynchronous Teleports**: All teleports (such as spectator camera follows) use modern non-blocking `Entity#teleportAsync()` futures rather than standard synchronous teleports.
3. **Recipient-Threaded Packets**: Player visibility toggles (hide/show packet loops in Vanish) are run on each recipient's specific entity scheduler to prevent cross-region threading exceptions.
4. **Isolating block-state reads**: Traditional `/invsee` plugins query `.getHolder()` on block inventories in other regions, which triggers Folia crashes. Staff+ uses a thread-safe `InvseeSession` registry to bypass this entirely.

---

## 🌐 Network Sync & Database

### Q: How do we sync mutes and bans across our server network?
To enable network-wide moderation, configure **both** the Velocity proxy plugin and the Paper backend plugins to point to the same database (MySQL/MariaDB).
* **Mutes**: Actively muted players are blocked from typing in chat network-wide at the Velocity proxy level.
* **Bans**: Banned players are rejected at the Velocity proxy pre-login event, saving backend resources.
* **IP-Ban Exemptions**: Whitelisted players bypass active IP bans at the gateway without removing the IP ban itself.
