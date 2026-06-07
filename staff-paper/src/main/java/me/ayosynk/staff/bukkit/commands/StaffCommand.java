package me.ayosynk.staff.bukkit.commands;

import me.ayosynk.staff.bukkit.StaffBukkitPlugin;
import me.ayosynk.staff.database.Punishment;
import me.ayosynk.staff.bukkit.utils.MiniMessageUtils;
import me.ayosynk.staff.bukkit.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffCommand implements CommandExecutor, TabCompleter {

    private final StaffBukkitPlugin plugin;
    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public StaffCommand(StaffBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerOnly()));
            return true;
        }

        Player staff = (Player) sender;

        if (args.length < 2 || !args[0].equalsIgnoreCase("info")) {
            staff.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>Usage: /staff info <player>"));
            return true;
        }

        String targetInput = args[1];

        // Run async lookup to prevent thread locking under Folia
        SchedulerUtils.runAsync(plugin, () -> handleInfoAsync(staff, targetInput));
        return true;
    }

    private void handleInfoAsync(Player staff, String targetInput) {
        resolveTarget(targetInput).thenAccept(target -> {
            if (target == null) {
                staff.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetInput)));
                return;
            }

            // Fetch all database records in parallel
            CompletableFuture<List<Punishment>> warningsFuture = plugin.getDatabaseManager().getWarnings(target.uuid);
            CompletableFuture<List<me.ayosynk.staff.database.DatabaseManager.PlayerRecord>> altsFuture = 
                    (target.ip != null && !target.ip.isEmpty()) 
                    ? plugin.getDatabaseManager().getAltsByIp(target.ip) 
                    : CompletableFuture.completedFuture(Collections.emptyList());
            CompletableFuture<Boolean> allowedFuture = plugin.getDatabaseManager().isAllowed(target.uuid);
            CompletableFuture<Integer> weightFuture = plugin.getDatabaseManager().getPlayerWeight(target.uuid);

            CompletableFuture.allOf(warningsFuture, altsFuture, allowedFuture, weightFuture).thenAccept(v -> {
                List<Punishment> warnings = warningsFuture.join();
                List<me.ayosynk.staff.database.DatabaseManager.PlayerRecord> alts = altsFuture.join();
                boolean isAllowed = allowedFuture.join();
                int weight = weightFuture.join();

                // Open the inventory GUI on the staff player's regional entity thread
                SchedulerUtils.runEntity(plugin, staff, () -> {
                    openStaffInfoGui(staff, target, warnings, alts, isAllowed, weight);
                });
            }).exceptionally(ex -> {
                plugin.getLogger().severe("Error loading staff info for " + target.name + ": " + ex.getMessage());
                staff.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>Database error loading profile details."));
                return null;
            });
        });
    }

    private void openStaffInfoGui(Player staff, ResolvedTarget target, List<Punishment> warnings, List<me.ayosynk.staff.database.DatabaseManager.PlayerRecord> alts, boolean isAllowed, int weight) {
        Player targetPlayer = Bukkit.getPlayer(target.uuid);
        boolean isOnline = targetPlayer != null && targetPlayer.isOnline() && plugin.canSee(staff, targetPlayer);

        StaffInfoHolder holder = new StaffInfoHolder(target.uuid, target.name, target.ip, isOnline);
        Inventory inv = Bukkit.createInventory(holder, 54, MiniMessageUtils.parse("<color:#A0A0A0>Staff Info: <color:#00E262>" + target.name));
        holder.setInventory(inv);

        // 1. Fill borders with Gray Stained Glass Panes
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(MiniMessageUtils.parse(" "));
            border.setItemMeta(borderMeta);
        }

        // Fill top row (0-8) and bottom row (45-53, except close button)
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) {
            if (i != 49) inv.setItem(i, border);
        }
        // Fill columns
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }

        // 2. Profile Head (Slot 13)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta != null) {
            headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(target.uuid));
            headMeta.displayName(MiniMessageUtils.parse("<color:#00E262>" + target.name));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>UUID: <color:#FFFFFF>" + target.uuid));
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>IP Address: <color:#FFFFFF>" + (target.ip != null && !target.ip.isEmpty() ? target.ip : "N/A")));
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Hierarchy Weight: <color:#00E262>" + weight));
            headMeta.lore(lore);
            head.setItemMeta(headMeta);
        }
        inv.setItem(13, head);

        // 3. Compass - Connection & Session Details (Slot 11)
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compMeta = compass.getItemMeta();
        if (compMeta != null) {
            compMeta.displayName(MiniMessageUtils.parse("<color:#E2B700>Session Status"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Status: " + (isOnline ? "<color:#00E262>Online" : "<color:#E20000>Offline")));
            if (isOnline) {
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>GameMode: <color:#FFFFFF>" + targetPlayer.getGameMode().name()));
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Flying: <color:#FFFFFF>" + (targetPlayer.isFlying() ? "Yes" : "No")));
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Vanished: <color:#FFFFFF>" + (plugin.isVanished(target.uuid) ? "Yes" : "No")));
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Ping: <color:#FFFFFF>" + targetPlayer.getPing() + "ms"));
            }
            compMeta.lore(lore);
            compass.setItemMeta(compMeta);
        }
        inv.setItem(11, compass);

        // 4. Grass Block - World Location (Slot 15)
        ItemStack grass = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta grassMeta = grass.getItemMeta();
        if (grassMeta != null) {
            grassMeta.displayName(MiniMessageUtils.parse("<color:#00C2E2>Location Info"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            if (isOnline) {
                Location loc = targetPlayer.getLocation();
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>World: <color:#FFFFFF>" + loc.getWorld().getName()));
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>X: <color:#FFFFFF>" + String.format("%.1f", loc.getX())));
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Y: <color:#FFFFFF>" + String.format("%.1f", loc.getY())));
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Z: <color:#FFFFFF>" + String.format("%.1f", loc.getZ())));
            } else {
                lore.add(MiniMessageUtils.parse("<color:#E20000>Target is offline."));
            }
            grassMeta.lore(lore);
            grass.setItemMeta(grassMeta);
        }
        inv.setItem(15, grass);

        // 5. Book - Database Status (Slot 22)
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.displayName(MiniMessageUtils.parse("<color:#FFAA00>Database Statistics"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Active Warnings: <color:#FFAA00>" + warnings.size()));
            int altCount = alts.size() > 1 ? alts.size() - 1 : 0;
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Alt Accounts on IP: <color:#FFAA00>" + altCount));
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>IP Ban Exempted: <color:#FFFFFF>" + (isAllowed ? "<color:#00E262>Yes" : "<color:#E20000>No")));
            bookMeta.lore(lore);
            book.setItemMeta(bookMeta);
        }
        inv.setItem(22, book);

        // 6. Ender Pearl - Teleport (Slot 29, online only)
        if (isOnline) {
            ItemStack pearl = new ItemStack(Material.ENDER_PEARL);
            ItemMeta pearlMeta = pearl.getItemMeta();
            if (pearlMeta != null) {
                pearlMeta.displayName(MiniMessageUtils.parse("<color:#00E262>Teleport to Target"));
                pearlMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to teleport to player's current location.")));
                pearl.setItemMeta(pearlMeta);
            }
            inv.setItem(29, pearl);
        } else {
            inv.setItem(29, makeDisabledPane("Teleport to Target"));
        }

        // 7. Chest - Invsee Inspector (Slot 30, online only)
        if (isOnline) {
            ItemStack chest = new ItemStack(Material.CHEST);
            ItemMeta chestMeta = chest.getItemMeta();
            if (chestMeta != null) {
                chestMeta.displayName(MiniMessageUtils.parse("<color:#00E262>Inspect Inventory"));
                chestMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to open live inventory viewer (/invsee).")));
                chest.setItemMeta(chestMeta);
            }
            inv.setItem(30, chest);
        } else {
            inv.setItem(30, makeDisabledPane("Inspect Inventory"));
        }

        // 8. Bookshelf - Active Warnings Log (Slot 31)
        ItemStack bookshelf = new ItemStack(Material.BOOKSHELF);
        ItemMeta shelfMeta = bookshelf.getItemMeta();
        if (shelfMeta != null) {
            shelfMeta.displayName(MiniMessageUtils.parse("<color:#FFAA00>Active Warnings Profile"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            if (warnings.isEmpty()) {
                lore.add(MiniMessageUtils.parse("<color:#A0A0A0>No active warnings logged."));
            } else {
                for (Punishment w : warnings) {
                    lore.add(MiniMessageUtils.parse("<color:#A0A0A0>- " + w.getReason() + " (" + DATE_FORMAT.format(w.getStartTime()) + ")"));
                }
                lore.add(MiniMessageUtils.parse(" "));
                lore.add(MiniMessageUtils.parse("<color:#FFAA00>Click to clear all warnings."));
            }
            shelfMeta.lore(lore);
            bookshelf.setItemMeta(shelfMeta);
        }
        inv.setItem(31, bookshelf);

        // 9. Beacon / Barrier - IP Ban Exemption Toggle (Slot 32)
        ItemStack exemptItem = new ItemStack(isAllowed ? Material.BEACON : Material.ANVIL);
        ItemMeta exmMeta = exemptItem.getItemMeta();
        if (exmMeta != null) {
            exmMeta.displayName(MiniMessageUtils.parse(isAllowed ? "<color:#00E262>Exemption Status: Whitelisted" : "<color:#E2B700>Exemption Status: Normal"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessageUtils.parse("<color:#A0A0A0>Allows player to bypass active IP-bans."));
            lore.add(MiniMessageUtils.parse(" "));
            lore.add(MiniMessageUtils.parse(isAllowed ? "<color:#E20000>Click to REMOVE whitelist bypass." : "<color:#00E262>Click to ENABLE whitelist bypass."));
            exmMeta.lore(lore);
            exemptItem.setItemMeta(exmMeta);
        }
        inv.setItem(32, exemptItem);

        // 10. Paper - View Full History (Slot 33)
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        if (paperMeta != null) {
            paperMeta.displayName(MiniMessageUtils.parse("<color:#00E262>View Logs History"));
            paperMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to print full punishment history to chat.")));
            paper.setItemMeta(paperMeta);
        }
        inv.setItem(33, paper);

        // 11. Feather - Kick Target (Slot 38, online only)
        if (isOnline) {
            ItemStack feather = new ItemStack(Material.FEATHER);
            ItemMeta featherMeta = feather.getItemMeta();
            if (featherMeta != null) {
                featherMeta.displayName(MiniMessageUtils.parse("<color:#E2B700>Kick Player"));
                featherMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to kick player from the server.")));
                feather.setItemMeta(featherMeta);
            }
            inv.setItem(38, feather);
        } else {
            inv.setItem(38, makeDisabledPane("Kick Player"));
        }

        // 12. Golden Axe - Warn Player (Slot 39)
        ItemStack axe = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta axeMeta = axe.getItemMeta();
        if (axeMeta != null) {
            axeMeta.displayName(MiniMessageUtils.parse("<color:#FFAA00>Warn Player"));
            axeMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to issue warning (Reason: GUI Warning).")));
            axe.setItemMeta(axeMeta);
        }
        inv.setItem(39, axe);

        // 13. Bell - Mute Player (Slot 40)
        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta bellMeta = bell.getItemMeta();
        if (bellMeta != null) {
            bellMeta.displayName(MiniMessageUtils.parse("<color:#E2B700>Mute Player"));
            bellMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to mute player for 1 hour (Reason: GUI Mute).")));
            bell.setItemMeta(bellMeta);
        }
        inv.setItem(40, bell);

        // 14. Netherite Sword - Ban Player (Slot 41)
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        if (swordMeta != null) {
            swordMeta.displayName(MiniMessageUtils.parse("<color:#E20000>Ban Player"));
            swordMeta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Click to ban player permanently (Reason: GUI Ban).")));
            sword.setItemMeta(swordMeta);
        }
        inv.setItem(41, sword);

        // 15. Barrier - Close Menu (Slot 49)
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.displayName(MiniMessageUtils.parse("<color:#E20000>Close GUI"));
            barrier.setItemMeta(barrierMeta);
        }
        inv.setItem(49, barrier);

        staff.openInventory(inv);
    }

    private ItemStack makeDisabledPane(String title) {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessageUtils.parse("<color:#E20000>" + title + " <color:#A0A0A0>(Offline)"));
            meta.lore(Collections.singletonList(MiniMessageUtils.parse("<color:#A0A0A0>Action unavailable while target is offline.")));
            item.setItemMeta(meta);
        }
        return item;
    }

    private CompletableFuture<ResolvedTarget> resolveTarget(String input) {
        CompletableFuture<ResolvedTarget> future = new CompletableFuture<>();

        if (IP_PATTERN.matcher(input).matches()) {
            future.complete(new ResolvedTarget(null, input, input));
            return future;
        }

        Player player = Bukkit.getPlayer(input);
        if (player != null && player.isOnline()) {
            future.complete(new ResolvedTarget(player.getUniqueId(), player.getName(), player.getAddress().getAddress().getHostAddress()));
            return future;
        }

        plugin.getDatabaseManager().getPlayerUuidByName(input).thenAccept(uuid -> {
            if (uuid == null) {
                future.complete(null);
                return;
            }

            plugin.getDatabaseManager().getPlayerRecord(uuid).thenAccept(record -> {
                if (record != null) {
                    future.complete(new ResolvedTarget(record.uuid, record.name, record.ip));
                } else {
                    future.complete(null);
                }
            }).exceptionally(ex -> {
                future.complete(null);
                return null;
            });
        }).exceptionally(ex -> {
            future.complete(null);
            return null;
        });

        return future;
    }

    public static class ResolvedTarget {
        public final UUID uuid;
        public final String name;
        public final String ip;

        ResolvedTarget(UUID uuid, String name, String ip) {
            this.uuid = uuid;
            this.name = name;
            this.ip = ip;
        }
    }

    // Tab Completion for /staff info
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Collections.singletonList("info").stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            String input = args[1].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (Player p : plugin.getVisibleOnlinePlayers(sender)) {
                if (p.getName().toLowerCase().startsWith(input)) {
                    suggestions.add(p.getName());
                }
            }
            for (String regName : plugin.getRegisteredNames()) {
                if (regName.toLowerCase().startsWith(input) && !suggestions.contains(regName)) {
                    suggestions.add(regName);
                }
            }
            Collections.sort(suggestions);
            return suggestions;
        }

        return Collections.emptyList();
    }
}
