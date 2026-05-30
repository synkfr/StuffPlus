# Folia Support & Lag Safety

`Stuff+` is designed from the ground up to run perfectly on **Folia** multi-threaded servers as well as standard **Paper** and **Spigot** servers. 

If you are running a large server network, you might know that Folia runs different regions of your world on separate threads to prevent lag. This makes standard admin tools crash or lag, but `Stuff+` keeps your gameplay completely safe and fast.

Here is how we do it in simple terms:

---

## 1. Asynchronous Teleports (No Server Jumps)
In standard plugins, teleporting a player stops the server for a split second to load chunks. On Folia, this causes crashes. 

`Stuff+` uses safe, modern **Asynchronous Teleporting**. The server loads the chunks in the background and moves the player only when the destination is completely ready, keeping your TPS stable at a constant 20.0.

---

## 2. Smart Invisibility (Vanish packet delivery)
When you vanish, your character is hidden from other players by sending packets. On Folia, if these packets are sent incorrectly, other players will still see you or your server will throw errors. 

`Stuff+` safely queues and schedules visibility updates on each individual player's tick scheduler, making sure you are **100% invisible** every time you toggle `/vanish`.

---

## 3. Real-time Inventory Inspector (/invsee)
Inspecting player inventories on Folia usually crashes the server if the players are located in different regions or worlds, as the plugin tries to read block containers (like chests) from the wrong thread.

`Stuff+` uses a custom **Session Registry** to solve this. Instead of reading blocks from the world continually, we track active inspect sessions in memory and sync updates safely, preventing crashes completely.

---

## 4. Lag-free Spectate Follow (/monitor)
Teleporting a spectating staff member to follow a player on every single tick causes heavy server lag and makes the camera rubber-band or stutter.

`Stuff+` implements **Cinematic Follow Damping**:
* **Fewer updates**: Follow checks run every 200 milliseconds instead of 20 times a second, reducing server load by 75%.
* **Smooth prediction**: The camera predicts where the target player is moving and teleports slightly ahead, giving you a smooth, professional view.
* **Tether limit**: Staff can fly freely up to 10 blocks away from the target before being gently pulled back.
