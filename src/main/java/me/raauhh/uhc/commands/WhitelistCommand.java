package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhitelistCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        Player player = (Player) sender;
        GameManager gameManager = UHC.getInstance().getGameManager();

        if(gameManager.isGameRunning()) {
            player.sendMessage("§cCannot modify the whitelist while the game is running.");
            return true;
        }

        if (args.length < 1) {
            if (gameManager.getHost().contains(player.getUniqueId()) || player.isOp())
                player.sendMessage("§cPlease use /whitelist <add, remove, on, off>");
            else {
                int count;
                if (UHC.getInstance().getPlayerWhitelists().get(player.getUniqueId()).isEmpty()) count = 1;
                else count = 0;
                player.sendMessage("§cPlease use /whitelist <add, remove>. You've remaining " + count + " whitelits");
            }
            return true;
        }

        switch (args[0]) {
            case "on":
                if (!gameManager.getHost().contains(player.getUniqueId()) || !player.isOp()) {
                    player.sendMessage("§cYou must be the host of the game.");
                    return true;
                }

                player.sendMessage("§aYou've turned §2on §athe whitelist.");
                gameManager.setWhitelist(true);
                break;
            case "off":
                if (!gameManager.getHost().contains(player.getUniqueId()) || !player.isOp()) {
                    player.sendMessage("§cYou must be the host of the game.");
                    return true;
                }

                player.sendMessage("§aYou've turned §coff §athe whitelist.");
                gameManager.setWhitelist(false);
                break;
            case "add":
                if (!gameManager.getHost().contains(player.getUniqueId()) || !player.isOp()) {

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (offlinePlayer.isOnline()) {
                        player.sendMessage("§cThat player is already in the server.");
                        return true;
                    }

                    if (UHC.getInstance().getWhitelist().contains(args[1])) {
                        player.sendMessage("§cThat player is already whitelisted.");
                        return true;
                    }


                    UHC.getInstance().getWhitelist().add(args[1].toLowerCase());
                    player.sendMessage("§6" + args[1] + " §awas added to the whitelist successful.");
                } else {
                    if (UHC.getInstance().getPlayerWhitelists().get(player.getUniqueId()).size() == 1) {
                        player.sendMessage("§cYou've exceeded your maximum whitelists. Type /whitelist list to check your whitelist.");
                        return true;
                    }

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (offlinePlayer.isOnline()) {
                        player.sendMessage("§cThat player is already in the server.");
                        return true;
                    }

                    if (!UHC.getInstance().getPlayerWhitelists().get(player.getUniqueId()).contains(args[1].toLowerCase())) {

                        if (UHC.getInstance().getWhitelist().contains(args[1])) {
                            player.sendMessage("§cThat player is already whitelisted.");
                            return true;
                        }

                        UHC.getInstance().getWhitelist().add(args[1].toLowerCase());
                        UHC.getInstance().getPlayerWhitelists().get(player.getUniqueId()).add(args[1].toLowerCase());
                        player.sendMessage("§6" + args[1] + " §awas added to the whitelist successful.");
                    } else {
                        player.sendMessage("§6" + args[1] + " §cis already in your whitelist.");
                    }

                }
                break;
            case "remove":
                if (!gameManager.getHost().contains(player.getUniqueId()) || !player.isOp()) {

                    if (!UHC.getInstance().getWhitelist().contains(args[1].toLowerCase())) {
                        player.sendMessage("§cThat is not whitelisted.");
                        return true;
                    }

                    player.sendMessage("§6" + args[1] + " §cwas removed from the whitelist successful.");
                    UHC.getInstance().getWhitelist().remove(args[1]);
                } else {
                    if (!UHC.getInstance().getPlayerWhitelists().get(player.getUniqueId()).contains(args[1].toLowerCase())) {
                        player.sendMessage("§6" + args[1] + " §cis not in your whitelist.");
                        return true;
                    }

                    player.sendMessage("§6" + args[1] + " §cwas removed from your whitelist successful.");
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (offlinePlayer.isOnline()) {
                        if (player.hasPermission("uhc.vip")) return true;
                        offlinePlayer.getPlayer().kickPlayer("§c" + player.getName() + " has unwhitelisted you.");
                    }
                    break;
                }
        }
        return true;
    }
}
