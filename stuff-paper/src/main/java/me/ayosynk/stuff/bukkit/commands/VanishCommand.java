package me.ayosynk.stuff.bukkit.commands;

import me.ayosynk.stuff.bukkit.StuffBukkitPlugin;
import me.ayosynk.stuff.bukkit.utils.MiniMessageUtils;
import me.ayosynk.stuff.bukkit.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VanishCommand implements CommandExecutor, TabCompleter {

    private final StuffBukkitPlugin plugin;

    public VanishCommand(StuffBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerOnly()));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Run synchronously on player's regional thread (safe for entity interactions under Folia)
        SchedulerUtils.runEntity(plugin, player, () -> {
            boolean isVanished = plugin.isVanished(uuid);
            if (!isVanished) {
                // Enable Vanish
                plugin.setVanished(uuid, true);
                hidePlayerFromAll(player);
                player.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getVanishEnabled()));
            } else {
                // Disable Vanish
                plugin.setVanished(uuid, false);
                showPlayerToAll(player);
                player.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getVanishDisabled()));
            }
        });

        return true;
    }

    private void hidePlayerFromAll(Player vanished) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(vanished.getUniqueId())) {
                continue;
            }
            if (!other.hasPermission("stuff.vanish.see")) {
                SchedulerUtils.runEntity(plugin, other, () -> other.hidePlayer(plugin, vanished));
            }
        }
    }

    private void showPlayerToAll(Player vanished) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(vanished.getUniqueId())) {
                continue;
            }
            SchedulerUtils.runEntity(plugin, other, () -> other.showPlayer(plugin, vanished));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
