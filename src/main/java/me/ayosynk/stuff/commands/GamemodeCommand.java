package me.ayosynk.stuff.commands;

import me.ayosynk.stuff.StuffPlugin;
import me.ayosynk.stuff.utils.MiniMessageUtils;
import me.ayosynk.stuff.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private final StuffPlugin plugin;

    public GamemodeCommand(StuffPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        GameMode gm = resolveGameMode(cmd.getName().toLowerCase());
        if (gm == null) return true;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerOnly()));
                return true;
            }

            Player player = (Player) sender;
            SchedulerUtils.runEntity(plugin, player, () -> setGameMode(player, player, gm));
        } else {
            if (!sender.hasPermission("stuff.gamemode.others")) {
                sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getNoPermission()));
                return true;
            }

            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null || !target.isOnline() || !plugin.canSee(sender, target)) {
                sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return true;
            }

            SchedulerUtils.runEntity(plugin, target, () -> setGameMode(sender, target, gm));
        }
        return true;
    }

    private GameMode resolveGameMode(String cmdName) {
        switch (cmdName) {
            case "gmc": return GameMode.CREATIVE;
            case "gms": return GameMode.SURVIVAL;
            case "gmsp": return GameMode.SPECTATOR;
            case "gma": return GameMode.ADVENTURE;
            default: return null;
        }
    }

    private void setGameMode(CommandSender sender, Player target, GameMode gm) {
        target.setGameMode(gm);
        String modeName = gm.name().toLowerCase();

        target.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + "<color:#A0A0A0>Your game mode has been set to <color:#00E262>" + modeName + "<color:#A0A0A0>."));
        
        if (!(sender instanceof Player) || !((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(MiniMessageUtils.parse(plugin.getMessageConfig().getPrefix() + "<color:#A0A0A0>Set <color:#00E262>" + target.getName() + "<color:#A0A0A0>'s game mode to <color:#00E262>" + modeName + "<color:#A0A0A0>."));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("stuff.gamemode.others")) {
            String input = args[0].toLowerCase();
            return plugin.getVisibleOnlinePlayers(sender).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
