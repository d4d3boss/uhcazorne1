package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillCountCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        Player player = (Player) sender;

        GameManager gameManager = UHC.getInstance().getGameManager();
        if (!gameManager.isGameRunning()) {
            player.sendMessage("§cThe game is not running.");
            return true;
        }

        if (args.length < 1) {
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());
            player.sendMessage("§fYour kills§7:§c " + uhcPlayer.getKills());
            return true;
        }

        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(args[0]);
        if (uhcPlayer != null) player.sendMessage("§6" + uhcPlayer.getName() + "§f's kills§7:§c " + uhcPlayer.getKills());
        else player.sendMessage("§6" + args[0] + " §chasn't played the game.");
        return true;
    }
}
