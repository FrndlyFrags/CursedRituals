package com.itemrituals.commands;

import com.itemrituals.location.LocationFinder.LocationSearchSession;
import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public class RitualCommand implements CommandExecutor, TabCompleter {
    private final ItemRitualsPlugin plugin;
    public RitualCommand(ItemRitualsPlugin plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "gui" -> {
                if (!checkPerm(sender, "ritual.gui")) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
                plugin.getRitualItemGUI().open(p);
            }
            case "start" -> {
                if (!checkPerm(sender, "ritual.start")) return true;
                if (plugin.getRitualManager().isActive()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getRitualAlreadyActive());
                    return true;
                }
                if (plugin.getItemPoolManager().isEmpty()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getPoolEmpty());
                    return true;
                }
                // 10-second countdown broadcast to all players
                sender.sendMessage(plugin.getConfigManager().getPrefix() + "§eRitual starting in §c10 §eseconds...");
                new org.bukkit.scheduler.BukkitRunnable() {
                    int countdown = 10;
                    @Override public void run() {
                        if (countdown > 0) {
                            String msg;
                            if (countdown <= 3) {
                                msg = plugin.getConfigManager().getPrefix() + "§c§l" + countdown + "...";
                            } else {
                                msg = plugin.getConfigManager().getPrefix() + "§e☠ Ritual begins in §c" + countdown + " §eseconds!";
                            }
                            plugin.getServer().broadcastMessage(msg);
                            countdown--;
                        } else {
                            cancel();
                            if (!plugin.getRitualManager().isActive()) {
                                if (!plugin.getRitualManager().startRitual()) {
                                    plugin.getServer().broadcastMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getPoolEmpty());
                                }
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
            case "stop" -> {
                if (!checkPerm(sender, "ritual.stop")) return true;
                if (!plugin.getRitualManager().isActive()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getNoActiveRitual());
                    return true;
                }
                plugin.getRitualManager().stopRitual(true);
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getRitualStopped());
            }
            case "status" -> {
                if (!checkPerm(sender, "ritual.status")) return true;
                Ritual ritual = plugin.getRitualManager().getActiveRitual();
                if (ritual == null) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getNoActiveRitual());
                    return true;
                }
                long sec = ritual.getRemainingTime() / 1000;
                sender.sendMessage("§8§m-------------------");
                sender.sendMessage("§c☠ ACTIVE RITUAL");
                sender.sendMessage("§7Item: §f" + ritual.getRewardItem().getType());
                sender.sendMessage("§7World: §f" + ritual.getCenterLocation().getWorld().getName());
                sender.sendMessage("§7Location: §f" + ritual.getCenterLocation().getBlockX() + ", " +
                        ritual.getCenterLocation().getBlockY() + ", " + ritual.getCenterLocation().getBlockZ());
                sender.sendMessage("§7Phase: §f" + ritual.getPhase());
                sender.sendMessage("§7Time Remaining: §f" + String.format("%02d:%02d", sec / 60, sec % 60));
                sender.sendMessage("§7Radius: §f" + ritual.getRadius());
                sender.sendMessage("§8§m-------------------");
            }
            case "locate" -> {
                if (!checkPerm(sender, "ritual.locate")) return true;
                Ritual ritual = plugin.getRitualManager().getActiveRitual();
                if (ritual == null) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getNoActiveRitual());
                    return true;
                }
                Location loc = ritual.getCenterLocation();
                sender.sendMessage("§c☠ Ritual Location: §f" + loc.getWorld().getName() + " @ " +
                        loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            }
            case "reload" -> {
                if (!checkPerm(sender, "ritual.reload")) return true;
                plugin.reload();
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getConfigReloaded());
            }
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private boolean checkPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getNoPermission());
            return false;
        } return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m-------------------");
        sender.sendMessage("§c☠ Item Rituals Commands");
        sender.sendMessage("§8§m-------------------");
        sender.sendMessage("§7/ritual gui §8- Open the ritual item GUI (left-click to add, right-click to remove)");
        sender.sendMessage("§7/ritual start §8- Start a ritual");
        sender.sendMessage("§7/ritual stop §8- Cancel active ritual");
        sender.sendMessage("§7/ritual status §8- Show ritual info");
        sender.sendMessage("§7/ritual locate §8- Show ritual coords");
        sender.sendMessage("§7/ritual reload §8- Reload config");
        sender.sendMessage("§8§m-------------------");
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("gui","start","stop","status","locate","reload","help");
        return List.of();
    }
}
