package me.raauhh.uhc.commands.team;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.team.UHCTeam;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        Player player = (Player) sender;

        if (!UHC.getInstance().getGameManager().isTeams()) {
            player.sendMessage("§cThe game must be a Team game.");
            return true;
        }

        if (args.length < 1) {
            UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());
            if (uhcTeam == null) {
                player.sendMessage("§cYou must be in a Team");
                return true;
            }

            UHC.getInstance().getUhcTeamManager().getTeamList(player, player);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(target.getUniqueId());
        if (uhcTeam == null) {
            player.sendMessage("§cThat player is not in a Team");
            return true;
        }

        UHC.getInstance().getUhcTeamManager().getTeamList(player, target);
        return true;
    }

}
