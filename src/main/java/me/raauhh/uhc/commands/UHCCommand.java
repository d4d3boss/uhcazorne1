package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.events.GameSetUpEvent;
import net.minecraft.util.com.google.gson.JsonObject;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHCCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        GameManager gameManager = UHC.getInstance().getGameManager();

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage("§cPlease use /uhc <start, host, mod, config>");
            return true;
        }

        switch (args[0]) {
            case "info":
                player.sendMessage("§7§m------------------------------------");
                player.sendMessage("§e§lUHC Information:");
                player.sendMessage(" §7> §cPlayers§7:§f " + Common.getOnlinePlayers().size());
                player.sendMessage(" §7> §cEntities§7:§f " + UHC.getInstance().getWorldManager().getUhcWorld().getEntities().size());

                int tps = (int) Math.min(Math.round(Double.parseDouble(Common.getTPS())), 20);
                StringBuilder tpsBar = new StringBuilder(60);
                for (int i = 0; i < 20; i++)
                    tpsBar.append(tps > i ? ChatColor.GREEN.toString() : ChatColor.RED.toString()).append('|');

                player.sendMessage(" §7> §cAverage TPS§7:§f " + Common.getTPS() + " §7[" + tpsBar + "§7]");
                player.sendMessage("§7§m------------------------------------");
                break;
            case "butcher":
                player.sendMessage("§aMobs were butchered successful");
                gameManager.butcherMobs();
                break;
            case "config":
                UHC.getInstance().getUhcConfigMenu().open(player);
                break;
            case "start":
                if (gameManager.isGameRunning()) {
                    player.sendMessage("§cThe game has already begun");
                    return true;
                }

                if (UHC.getInstance().isUsingRedis()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("server", Bukkit.getServerName());
                    UHC.getInstance().getRedisMessagingHandler().sendMessage("uhc:start", object.toString());
                }
                player.sendMessage("§aYou forced the game to start");
                Bukkit.getPluginManager().callEvent(new GameSetUpEvent());
                break;
            case "mod":
            case "moderator":
                if (args.length <= 2) {
                    if (gameManager.getModerators().contains(player.getUniqueId())) {
                        player.sendMessage("§7Your §6moderator mode§7 has been §cdisabled");
                        gameManager.getModerators().remove(player.getUniqueId());
                    } else {
                        player.sendMessage("§7Your §6moderator mode§7 has been §aenabled");
                        gameManager.getModerators().add(player.getUniqueId());
                    }
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (!offlinePlayer.isOnline()) {
                        player.sendMessage("§cThat player is offline");
                        return true;
                    }

                    Player target = offlinePlayer.getPlayer();
                    if (!target.hasPermission("uhc.staff")) {
                        player.sendMessage("§cThat player must be an staff member");
                        return true;
                    }

                    if (gameManager.getModerators().contains(target.getUniqueId())) {
                        gameManager.getModerators().remove(player.getUniqueId());
                        player.sendMessage("§7" + target.getName() + "'s §6moderator mode§7 has been §cdisabled");
                    } else {
                        gameManager.getModerators().add(player.getUniqueId());
                        player.sendMessage("§7" + target.getName() + "'s §6moderator mode§7 has been §aenabled");
                        // TODO: make him spec
                    }
                }
                break;
            case "host":
                if (args.length <= 2) {
                    if (gameManager.getHost().contains(player.getUniqueId())) {
                        gameManager.getHost().remove(player.getUniqueId());
                        player.sendMessage("§7Your §6host mode§7 has been §cdisabled");
                    } else {
                        if (gameManager.getHost().size() > 0) {
                            player.sendMessage("§cThere can only be one host per game");
                            return true;
                        }

                        gameManager.getHost().add(player.getUniqueId());
                        player.sendMessage("§7Your §6host mode§7 has been §aenabled");
                    }
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (!offlinePlayer.isOnline()) {
                        player.sendMessage("§cThat player is offline");
                        return true;
                    }

                    Player target = offlinePlayer.getPlayer();
                    if (!target.hasPermission("uhc.staff")) {
                        player.sendMessage("§cThat player must be an staff member");
                        return true;
                    }

                    if (gameManager.getHost().contains(target.getUniqueId())) {
                        gameManager.getHost().remove(target.getUniqueId());
                        player.sendMessage("§7" + target.getName() + "'s §6host mode§7 has been §cdisabled");
                    } else {
                        if (gameManager.getHost().size() > 0) {
                            player.sendMessage("§cThere can only be one host per game");
                            return true;
                        }
                        gameManager.getHost().add(target.getUniqueId());
                        player.sendMessage("§7" + target.getName() + "'s §6host mode§7 has been §aenabled");
                    }
                }
                break;
            default:
                player.sendMessage("§cPlease use /uhc <start, host, mod, config>");
                break;
        }
        return true;
    }
}
