package me.ayosynk.staff.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.ayosynk.staff.database.DatabaseManager;
import me.ayosynk.staff.database.Punishment;
import me.ayosynk.staff.utils.DiscordWebhookUtils;
import me.ayosynk.staff.utils.DurationUtils;
import me.ayosynk.staff.velocity.StaffVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Network-wide punishment command handler for Velocity proxy.
 * Supports: ban, tempban, unban, ip-ban, tempip-ban, unip-ban,
 *           mute, tempmute, unmute, warn, warns,
 *           history, staffhistory, staffrollback, staffallow, staffimport
 */
public class VelocityPunishCommand implements SimpleCommand {

    private final StaffVelocityPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public VelocityPunishCommand(StaffVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        var source = invocation.source();
        String[] args = invocation.arguments();
        String label = invocation.alias().toLowerCase();

        // Permission check
        String permission = "staff." + label.replace("-", "");
        if (!source.hasPermission(permission)) {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getNoPermission()));
            return;
        }

        DatabaseManager db = plugin.getDatabaseManager();

        switch (label) {
            case "ban" -> handleBan(source, args, db, false);
            case "tempban" -> handleTempBan(source, args, db);
            case "unban" -> handleUnban(source, args, db);
            case "ip-ban", "ipban", "banip" -> handleIpBan(source, args, db, false);
            case "tempip-ban", "tempipban", "tempbanip" -> handleTempIpBan(source, args, db);
            case "unip-ban", "unipban", "unbanip" -> handleUnIpBan(source, args, db);
            case "mute" -> handleMute(source, args, db, false);
            case "tempmute" -> handleTempMute(source, args, db);
            case "unmute" -> handleUnmute(source, args, db);
            case "warn" -> handleWarn(source, args, db);
            case "warns" -> handleWarns(source, args, db);
            case "history" -> handleHistory(source, args, db);
            case "staffhistory" -> handleStaffHistory(source, args, db);
            case "staffrollback" -> handleStaffRollback(source, args, db);
            case "staffallow" -> handleStaffAllow(source, args, db);
            case "staffimport" -> handleStaffImport(source, args);
            default -> source.sendMessage(parse("<color:#E20000>Unknown command."));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String input = args.length == 1 ? args[0].toLowerCase() : "";
            return CompletableFuture.completedFuture(
                plugin.getRegisteredNames().stream()
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList())
            );
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    // ==========================================
    // COMMAND HANDLERS
    // ==========================================

    private void handleBan(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db, boolean isTemp) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /ban <player> [reason]"));
            return;
        }
        String targetName = args[0];
        String reason = args.length > 1 ? joinArgs(args, 1) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }

            Punishment punishment = new Punishment(targetUuid, null, senderUuid, Punishment.Type.BAN, reason, new Timestamp(System.currentTimeMillis()), null, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerBanned().replace("{player}", targetName).replace("{time}", "Permanent").replace("{reason}", reason)));

                // Broadcast to all online players
                broadcastToAll(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerBannedBroadcast().replace("{player}", targetName).replace("{sender}", senderName).replace("{time}", "Permanent").replace("{reason}", reason));

                // Kick from proxy if online
                kickPlayer(targetUuid, plugin.getMessageConfig().getBanKickMessage().replace("{reason}", reason).replace("{time}", "Permanent"));

                // Discord Webhook
                DiscordWebhookUtils.sendEmbed(plugin, "Player Banned", plugin.getPluginConfig().getDiscordWebhookColorBan(), targetName, senderName, "Permanent", reason);
            });
        });
    }

    private void handleTempBan(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 2) {
            source.sendMessage(parse("<color:#E20000>Usage: /tempban <player> <time> [reason]"));
            return;
        }
        String targetName = args[0];
        long durationMs = DurationUtils.parseDuration(args[1]);
        if (durationMs == -2) {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getInvalidDuration()));
            return;
        }
        String reason = args.length > 2 ? joinArgs(args, 2) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";
        String timeStr = durationMs == -1 ? "Permanent" : DurationUtils.formatDuration(durationMs);

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }

            Timestamp endTime = durationMs == -1 ? null : new Timestamp(System.currentTimeMillis() + durationMs);
            Punishment punishment = new Punishment(targetUuid, null, senderUuid, Punishment.Type.BAN, reason, new Timestamp(System.currentTimeMillis()), endTime, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerBanned().replace("{player}", targetName).replace("{time}", timeStr).replace("{reason}", reason)));
                broadcastToAll(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerBannedBroadcast().replace("{player}", targetName).replace("{sender}", senderName).replace("{time}", timeStr).replace("{reason}", reason));
                kickPlayer(targetUuid, plugin.getMessageConfig().getBanKickMessage().replace("{reason}", reason).replace("{time}", timeStr));
                DiscordWebhookUtils.sendEmbed(plugin, "Player Temp-Banned", plugin.getPluginConfig().getDiscordWebhookColorBan(), targetName, senderName, timeStr, reason);
            });
        });
    }

    private void handleUnban(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /unban <player>"));
            return;
        }
        String targetName = args[0];
        String senderName = source instanceof Player p ? p.getUsername() : "Console";
        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            db.deactivatePunishment(targetUuid, Punishment.Type.BAN).thenAccept(success -> {
                if (success) {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerUnbanned().replace("{player}", targetName)));
                    broadcastToAll(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerUnbannedBroadcast().replace("{player}", targetName).replace("{sender}", senderName));
                } else {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>" + targetName + " is not banned."));
                }
            });
        });
    }

    private void handleIpBan(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db, boolean isTemp) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /ip-ban <player> [reason]"));
            return;
        }
        String targetName = args[0];
        String reason = args.length > 1 ? joinArgs(args, 1) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";

        db.getPlayerUuidByName(targetName).thenCompose(db::getPlayerRecord).thenAccept(record -> {
            if (record == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            Punishment punishment = new Punishment(record.uuid, record.ip, senderUuid, Punishment.Type.IP_BAN, reason, new Timestamp(System.currentTimeMillis()), null, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getIpBanned().replace("{player}", targetName).replace("{ip}", record.ip).replace("{time}", "Permanent").replace("{reason}", reason)));
                broadcastToAll(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getIpBannedBroadcast().replace("{player}", targetName).replace("{ip}", record.ip).replace("{sender}", senderName).replace("{time}", "Permanent").replace("{reason}", reason));
                kickPlayer(record.uuid, plugin.getMessageConfig().getBanKickMessage().replace("{reason}", reason).replace("{time}", "Permanent"));
                DiscordWebhookUtils.sendEmbed(plugin, "Player IP-Banned", plugin.getPluginConfig().getDiscordWebhookColorBan(), targetName + " (" + record.ip + ")", senderName, "Permanent", reason);
            });
        });
    }

    private void handleTempIpBan(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 2) {
            source.sendMessage(parse("<color:#E20000>Usage: /tempip-ban <player> <time> [reason]"));
            return;
        }
        String targetName = args[0];
        long durationMs = DurationUtils.parseDuration(args[1]);
        if (durationMs == -2) {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getInvalidDuration()));
            return;
        }
        String reason = args.length > 2 ? joinArgs(args, 2) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";
        String timeStr = durationMs == -1 ? "Permanent" : DurationUtils.formatDuration(durationMs);

        db.getPlayerUuidByName(targetName).thenCompose(db::getPlayerRecord).thenAccept(record -> {
            if (record == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            Timestamp endTime = durationMs == -1 ? null : new Timestamp(System.currentTimeMillis() + durationMs);
            Punishment punishment = new Punishment(record.uuid, record.ip, senderUuid, Punishment.Type.IP_BAN, reason, new Timestamp(System.currentTimeMillis()), endTime, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getIpBanned().replace("{player}", targetName).replace("{ip}", record.ip).replace("{time}", timeStr).replace("{reason}", reason)));
                kickPlayer(record.uuid, plugin.getMessageConfig().getBanKickMessage().replace("{reason}", reason).replace("{time}", timeStr));
            });
        });
    }

    private void handleUnIpBan(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /unip-ban <player/IP>"));
            return;
        }
        String target = args[0];
        String senderName = source instanceof Player p ? p.getUsername() : "Console";
        db.deactivateIpBan(target).thenAccept(success -> {
            if (success) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getIpUnbanned().replace("{ip}", target)));
                broadcastToAll(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getIpUnbannedBroadcast().replace("{ip}", target).replace("{sender}", senderName));
            } else {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>" + target + " is not IP-banned."));
            }
        });
    }

    private void handleMute(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db, boolean isTemp) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /mute <player> [reason]"));
            return;
        }
        String targetName = args[0];
        String reason = args.length > 1 ? joinArgs(args, 1) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            Punishment punishment = new Punishment(targetUuid, null, senderUuid, Punishment.Type.MUTE, reason, new Timestamp(System.currentTimeMillis()), null, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerMuted().replace("{player}", targetName).replace("{time}", "Permanent").replace("{reason}", reason)));
                broadcastToAll(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerMutedBroadcast().replace("{player}", targetName).replace("{sender}", senderName).replace("{time}", "Permanent").replace("{reason}", reason));
                DiscordWebhookUtils.sendEmbed(plugin, "Player Muted", plugin.getPluginConfig().getDiscordWebhookColorMute(), targetName, senderName, "Permanent", reason);
            });
        });
    }

    private void handleTempMute(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 2) {
            source.sendMessage(parse("<color:#E20000>Usage: /tempmute <player> <time> [reason]"));
            return;
        }
        String targetName = args[0];
        long durationMs = DurationUtils.parseDuration(args[1]);
        if (durationMs == -2) {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getInvalidDuration()));
            return;
        }
        String reason = args.length > 2 ? joinArgs(args, 2) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";
        String timeStr = durationMs == -1 ? "Permanent" : DurationUtils.formatDuration(durationMs);

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            Timestamp endTime = durationMs == -1 ? null : new Timestamp(System.currentTimeMillis() + durationMs);
            Punishment punishment = new Punishment(targetUuid, null, senderUuid, Punishment.Type.MUTE, reason, new Timestamp(System.currentTimeMillis()), endTime, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerMuted().replace("{player}", targetName).replace("{time}", timeStr).replace("{reason}", reason)));
                broadcastToAll(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerMutedBroadcast().replace("{player}", targetName).replace("{sender}", senderName).replace("{time}", timeStr).replace("{reason}", reason));
                DiscordWebhookUtils.sendEmbed(plugin, "Player Temp-Muted", plugin.getPluginConfig().getDiscordWebhookColorMute(), targetName, senderName, timeStr, reason);
            });
        });
    }

    private void handleUnmute(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /unmute <player>"));
            return;
        }
        String targetName = args[0];
        String senderName = source instanceof Player p ? p.getUsername() : "Console";
        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            db.deactivatePunishment(targetUuid, Punishment.Type.MUTE).thenAccept(success -> {
                if (success) {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerUnmuted().replace("{player}", targetName)));
                    broadcastToAll(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerUnmutedBroadcast().replace("{player}", targetName).replace("{sender}", senderName));
                } else {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>" + targetName + " is not muted."));
                }
            });
        });
    }

    private void handleWarn(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /warn <player> [reason]"));
            return;
        }
        String targetName = args[0];
        String reason = args.length > 1 ? joinArgs(args, 1) : "No reason specified";
        UUID senderUuid = source instanceof Player p ? p.getUniqueId() : null;
        String senderName = source instanceof Player p ? p.getUsername() : "Console";

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            Punishment punishment = new Punishment(targetUuid, null, senderUuid, Punishment.Type.WARN, reason, new Timestamp(System.currentTimeMillis()), null, true);
            db.addPunishment(punishment).thenRun(() -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerWarned().replace("{player}", targetName).replace("{reason}", reason)));
                broadcastToAll(plugin.getMessageConfig().getPrefix() +
                    plugin.getMessageConfig().getPlayerWarnedBroadcast().replace("{player}", targetName).replace("{sender}", senderName).replace("{reason}", reason));

                // Notify target if online
                Optional<Player> targetOpt = plugin.getServer().getPlayer(targetUuid);
                targetOpt.ifPresent(player -> player.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getYouAreWarned().replace("{reason}", reason))));

                DiscordWebhookUtils.sendEmbed(plugin, "Player Warned", plugin.getPluginConfig().getDiscordWebhookColorWarn(), targetName, senderName, "N/A", reason);
            });
        });
    }

    private void handleWarns(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /warns <player> [clear/list]"));
            return;
        }
        String targetName = args[0];
        boolean clear = args.length > 1 && args[1].equalsIgnoreCase("clear");

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            if (clear) {
                db.clearWarnings(targetUuid).thenAccept(success -> {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getWarnCleared().replace("{player}", targetName)));
                });
            } else {
                db.getWarnings(targetUuid).thenAccept(warns -> {
                    if (warns.isEmpty()) {
                        source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getNoWarns().replace("{player}", targetName)));
                    } else {
                        source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getWarnListHeader().replace("{player}", targetName)));
                        for (var w : warns) {
                            source.sendMessage(parse(plugin.getMessageConfig().getWarnListItem()
                                .replace("{reason}", w.getReason())
                                .replace("{sender}", w.getPunisherUuid() != null ? w.getPunisherUuid().toString() : "Console")
                                .replace("{date}", w.getStartTime().toString())));
                        }
                    }
                });
            }
        });
    }

    private void handleHistory(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /history <player>"));
            return;
        }
        db.getPlayerUuidByName(args[0]).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", args[0])));
                return;
            }
            db.getHistory(targetUuid).thenAccept(history -> {
                source.sendMessage(parse("<gradient:#00E262:#00FF7F>★ Punishment History for " + args[0] + " ★</gradient>"));
                if (history.isEmpty()) {
                    source.sendMessage(parse("<color:#A0A0A0>No punishments found."));
                } else {
                    for (var p : history) {
                        String active = p.isActive() ? "<color:#00E262>ACTIVE" : "<color:#E20000>EXPIRED";
                        source.sendMessage(parse("<color:#A0A0A0>[" + p.getType() + "] " + active + " <color:#A0A0A0>- " + p.getReason() + " (" + p.getStartTime() + ")"));
                    }
                }
            });
        });
    }

    private void handleStaffHistory(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /staffhistory <staff>"));
            return;
        }
        db.getPlayerUuidByName(args[0]).thenAccept(staffUuid -> {
            if (staffUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", args[0])));
                return;
            }
            db.getStaffHistory(staffUuid).thenAccept(history -> {
                source.sendMessage(parse("<gradient:#00E262:#00FF7F>★ Staff History for " + args[0] + " ★</gradient>"));
                if (history.isEmpty()) {
                    source.sendMessage(parse("<color:#A0A0A0>No punishments issued by this staff member."));
                } else {
                    for (var p : history) {
                        String active = p.isActive() ? "<color:#00E262>ACTIVE" : "<color:#E20000>EXPIRED";
                        source.sendMessage(parse("<color:#A0A0A0>[" + p.getType() + "] " + active + " <color:#A0A0A0>- " + p.getReason()));
                    }
                }
            });
        });
    }

    private void handleStaffRollback(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            source.sendMessage(parse("<color:#E20000>Usage: /staffrollback <staff> confirm"));
            return;
        }
        db.getPlayerUuidByName(args[0]).thenAccept(staffUuid -> {
            if (staffUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", args[0])));
                return;
            }
            db.rollbackStaff(staffUuid).thenAccept(count -> {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#00E262>Rolled back " + count + " punishments from " + args[0] + "."));
            });
        });
    }

    private void handleStaffAllow(com.velocitypowered.api.command.CommandSource source, String[] args, DatabaseManager db) {
        if (args.length < 1) {
            source.sendMessage(parse("<color:#E20000>Usage: /staffallow <player> [remove]"));
            return;
        }
        String targetName = args[0];
        boolean remove = args.length > 1 && args[1].equalsIgnoreCase("remove");
        String senderName = source instanceof Player p ? p.getUsername() : "Console";

        db.getPlayerUuidByName(targetName).thenAccept(targetUuid -> {
            if (targetUuid == null) {
                source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerNotFound().replace("{player}", targetName)));
                return;
            }
            if (remove) {
                db.removeAllow(targetUuid).thenAccept(success -> {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerUnallowed().replace("{player}", targetName)));
                });
            } else {
                db.addAllow(targetUuid).thenRun(() -> {
                    source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerAllowed().replace("{player}", targetName)));
                    broadcastToAll(plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getPlayerAllowedBroadcast().replace("{player}", targetName).replace("{sender}", senderName));
                });
            }
        });
    }

    private void handleStaffImport(com.velocitypowered.api.command.CommandSource source, String[] args) {
        var migrationManager = plugin.getMigrationManager();

        if (args.length == 0) {
            source.sendMessage(parse("<gradient:#00E262:#00FF7F>★ Staff+ Modern Importer & Migration System ★</gradient>"));
            for (var s : migrationManager.getSources()) {
                source.sendMessage(parse("<color:#A0A0A0> • </color><color:#00E262>/staffimport " + s.getName() + "</color> - <color:#707070>" + s.getDescription() + "</color>"));
            }
            return;
        }

        var sourceObj = migrationManager.getSource(args[0]);
        if (sourceObj == null) {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>Unknown migration source! Use /staffimport to view sources."));
            return;
        }

        source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#A0A0A0>Starting import from <color:#00E262>" + sourceObj.getName() + "<color:#A0A0A0>..."));
        sourceObj.migrate(plugin, msg -> source.sendMessage(parse(msg)), args).thenAccept(count -> {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<gradient:#00E262:#00FF7F>Successfully imported " + count + " punishments from " + sourceObj.getName() + "!</gradient>"));
        }).exceptionally(ex -> {
            source.sendMessage(parse(plugin.getMessageConfig().getPrefix() + "<color:#E20000>Migration failed: " + ex.getCause().getMessage()));
            return null;
        });
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private Component parse(String msg) {
        return miniMessage.deserialize(msg);
    }

    private void broadcastToAll(String msg) {
        Component component = parse(msg);
        for (Player player : plugin.getServer().getAllPlayers()) {
            player.sendMessage(component);
        }
    }

    private void kickPlayer(UUID uuid, String reason) {
        plugin.getServer().getPlayer(uuid).ifPresent(player ->
            player.disconnect(parse(reason))
        );
    }

    private String joinArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
