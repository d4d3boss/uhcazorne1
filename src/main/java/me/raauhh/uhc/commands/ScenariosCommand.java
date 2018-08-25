package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScenariosCommand  implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }
        Player player = (Player) sender;

        if(UHC.getInstance().getGamemodeManager().getEnabledGamemodes().isEmpty()){
            sender.sendMessage("§cThere are no gamemodes enabled in this game");
            return true;
        }

        UHC.getInstance().getGamemodesMenu().open(player);
        return true;
    }
}
