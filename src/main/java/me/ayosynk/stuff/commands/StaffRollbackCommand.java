package me.ayosynk.stuff.commands;

import me.ayosynk.stuff.StuffPlugin;
import me.ayosynk.stuff.utils.MiniMessageUtils;
import me.ayosynk.stuff.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StaffRollbackCommand implements CommandExecutor, TabCompleter {

    private final StuffPlugin plugin;

    public StaffRollbackCommand(StuffPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>Usage: /staffrollback <staff> [confirm]"));
            return true;
        }

        String staffInput = args[0];
        boolean confirm = args.length > 1 && args[1].equalsIgnoreCase("confirm");

        SchedulerUtils.runAsync(plugin, () -> handleCommandAsync(sender, staffInput, confirm));
        return true;
    }

    private void handleCommandAsync(CommandSender sender, String staffInput, boolean confirm) {
        resolveTarget(staffInput).thenAccept(target -> {
            if (target == null) {
                sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", staffInput)));
                return;
            }

            if (!confirm) {
                sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + 
                        "<color:#E2B700>WARNING: This will deactivate all active bans, mutes, and warnings issued by <color:#00E262>" + target.name + "</color>.\n" +
                        "To proceed, please type: <color:#00E262>/staffrollback " + target.name + " confirm</color>"));
                return;
            }

            plugin.getDatabaseManager().rollbackStaff(target.uuid).thenAccept(revokedCount -> {
                sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + 
                        "<color:#00E262>Successfully rolled back <color:#E2B700>" + revokedCount + "</color> punishments issued by <color:#E2B700>" + target.name + "</color>!"));
                
                // Broadcast to all administrative staff
                String broadcast = "<color:#E20000>[Rollback] " + sender.getName() + " rolled back " + revokedCount + " punishments issued by " + target.name + ".";
                Bukkit.broadcast(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + broadcast), "stuff.admin");
            });
        });
    }

    private CompletableFuture<ResolvedTarget> resolveTarget(String input) {
        CompletableFuture<ResolvedTarget> future = new CompletableFuture<>();
        Player online = Bukkit.getPlayer(input);

        if (online != null && online.isOnline()) {
            future.complete(new ResolvedTarget(online.getUniqueId(), online.getName(), online.getAddress().getAddress().getHostAddress()));
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

    private static class ResolvedTarget {
        final UUID uuid;
        final String name;
        final String ip;

        ResolvedTarget(UUID uuid, String name, String ip) {
            this.uuid = uuid;
            this.name = name;
            this.ip = ip;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
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
            return suggestions;
        } else if (args.length == 2 && !args[0].isEmpty()) {
            String input = args[1].toLowerCase();
            if ("confirm".startsWith(input)) {
                return Collections.singletonList("confirm");
            }
        }
        return Collections.emptyList();
    }
}
