package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class HelpOpCommand implements CommandExecutor {

    private HashMap<UUID, Long> helpOpCooldown = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        GameManager gameManager = UHC.getInstance().getGameManager();
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage("§cError. Please use /helpop <message>");
            return true;
        }

        if (helpOpCooldown.get(player.getUniqueId()) == null)
            helpOpCooldown.put(player.getUniqueId(), System.currentTimeMillis());

        if (helpOpCooldown.get(player.getUniqueId()) <= System.currentTimeMillis()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : args) {
                stringBuilder.append(str);
                stringBuilder.append(" ");
            }

            Bukkit.getConsoleSender().sendMessage("§e[HelpOp] " + player.getName() + "§7: §e" + stringBuilder.toString());
            UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(uhcPlayer -> {
                if (uhcPlayer.getPlayer() != null) {
                    if (gameManager.getHost().contains(uhcPlayer.getUuid()) || gameManager.getModerators().contains(uhcPlayer.getUuid())) {
                        uhcPlayer.getPlayer().sendMessage("§e[HelpOp] " + player.getName() + "§7: §e" + stringBuilder.toString());
                    }
                }
            });

            player.sendMessage("§aYour message has been sent.");
            this.helpOpCooldown.put(player.getUniqueId(), System.currentTimeMillis() + (10 * 1000));
        } else player.sendMessage("§cYou can only use helpop every 10 seconds");
        return true;
    }
}
