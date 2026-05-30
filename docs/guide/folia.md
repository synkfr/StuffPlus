# Folia Compatibility & Best Practices

`Stuff+` is engineered specifically to exploit the benefits of **Folia's multi-threaded region architecture** while maintaining strict safety boundaries to prevent thread clashes or data corruption.

For server administrators and developers looking to understand or extend the codebase, here is a detailed breakdown of the safety patterns integrated into this repository:

---

## The Core Challenge: Region Threading

Traditional Spigot/Paper plugins assume a single-threaded server where all entity ticks, world events, and inventory queries execute sequentially on the main server thread. 

**Folia completely shifts this paradigm** by ticking different parts of the world (regions) on separate threads. Attempting to query cross-region data or execute traditional synchronous Spigot calls from arbitrary threads will immediately throw an `IllegalStateException` or crash the ticking region.

`Stuff+` solves this using four distinct architectural pillars:

---

## Pillar 1: Asynchronous Teleportation

Synchronous `Entity#teleport(Location)` is completely disabled under Folia. Attempting to call it synchronously on ticking threads throws an `UnsupportedOperationException`.

`Stuff+` implements non-blocking asynchronous teleportation futures:
```java
// Safe asynchronous teleportation
staff.teleportAsync(location).thenAccept(success -> {
    if (success) {
        // Run dependent actions safely after teleportation resolves
    }
});
```

---

## Pillar 2: Recipient-Confined Packet Delivery

Visibility states (such as `/vanish` packet updates hiding or showing a player) must be scheduled directly on each recipient player's regional thread. Running this logic synchronously from the vanished player's thread will fail silently or throw cross-region threading exceptions.

`Stuff+` handles this by scheduling individual packet updates on each online player's specific regional scheduler:
```java
// Schedule hidePlayer on recipient's regional thread context
SchedulerUtils.runEntity(plugin, recipient, () -> {
    recipient.hidePlayer(plugin, vanishedPlayer);
});
```

---

## Pillar 3: Thread-Safe Session Registry

Under Folia, calling `.getOpenInventory().getTopInventory().getHolder()` on block-based inventories (e.g. Furnaces or Chests) open by staff in other regions triggers synchronous world reads. If the block is in another region, it fails the region thread check and crashes.

`Stuff+` completely avoids cross-thread inventory/block-state reads by introducing a **Thread-Safe Session Registry** (`ConcurrentHashMap`):
* **Registration**: When a staff member runs `/invsee`, the plugin registers an `InvseeSession` mapping `staffUuid -> InvseeSession` containing the staff UUID, target UUID, and custom `Inventory` instance.
* **Sync Updates**: When the target player interacts with their inventory, the plugin loops over active registered sessions in memory (entirely thread-safe) and schedules GUI updates safely on the viewing staff's specific regional thread.
* **Cleanup**: Cleans up automatically on `InventoryCloseEvent` (which runs on the closing player's thread) and `PlayerQuitEvent`.

---

## Pillar 4: Velocity-Predicted Cinematic Spectator Camera

Calling `teleportAsync` continuously on every server tick (20x/sec) for camera tracking creates severe visual stutter and rubber-banding, as the client is flooded with position corrections before its predicted movement is resolved.

`Stuff+` optimizes `/monitor` tracking using a **Cinematic Interpolation Engine**:
1. **4-Tick Dampener**: Follow tasks run every 4 ticks (200ms) instead of every tick, reducing correction packet saturation by 75%.
2. **1.5-Block Threshold**: Teleports only trigger if the target has moved > 1.5 blocks from the last tracked reference location, filtering micro-movement jitters.
3. **Velocity Prediction**: Evaluates target velocity vectors (`target.getVelocity()`) and projects coordinates slightly *ahead* of the target's vector, allowing the client-side prediction system to interpolate motion smoothly.
4. **Follow Cooldown**: Enforces a minimum 2-cycle (400ms) delay between teleports so async futures can fully complete before a new teleport is queued.
