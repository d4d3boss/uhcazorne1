package me.raauhh.uhc.commands.team;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.team.UHCTeam;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        Player player = (Player) sender;

        if (!UHC.getInstance().getGameManager().isTeams()) {
            player.sendMessage("§cThe game must be a Team game.");
            return true;
        }

        if (UHC.getInstance().getGameManager().isGameRunning()) {
            player.sendMessage("§cThe game is currently running");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cPlease use /team <create, invite, accept, leave, disband>");
            return true;
        }

        UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());
        UHCTeam newUhcTeam = UHC.getInstance().getUhcTeamRequestManager().getTeamRequest(player.getUniqueId());

        switch (args[0]) {

            case "create":
                if (uhcTeam != null) {
                    player.sendMessage("§aYou're already in a Team");
                    return true;
                }

                UHC.getInstance().getUhcTeamManager().createTeam(player);
                break;
            case "invite":

                if (args.length < 2) {
                    player.sendMessage("§cPlease use /team invite <player>");
                    return true;
                }

                if (uhcTeam == null) {
                    player.sendMessage("§cYou must be in a team to invite that player");
                    return true;
                }

                if (uhcTeam.getOwner() != player) {
                    player.sendMessage("§cYou must be the owner of your Team");
                    return true;
                }

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!offlinePlayer.isOnline()) {
                    player.sendMessage("§cThat player is offline");
                    return true;
                }

                UHC.getInstance().getUhcTeamRequestManager().sendRequest(offlinePlayer.getPlayer(), uhcTeam);
                break;
            case "leave":

                if (uhcTeam == null) {
                    player.sendMessage("§cYou must be in a Team to leave them");
                    return true;
                }

                if (uhcTeam.getOwner() == player) {
                    UHC.getInstance().getUhcTeamManager().disbandTeam(uhcTeam);
                    return true;
                }

                UHC.getInstance().getUhcTeamManager().removePlayerFromTeam(player.getUniqueId());
                break;
            case "disband":

                if (uhcTeam == null) {
                    player.sendMessage("§cYou must be in a Team to disband them");
                    return true;
                }

                if (uhcTeam.getOwner() != player) {
                    player.sendMessage("§cYou must be the owner of your Team");
                    return true;
                }

                UHC.getInstance().getUhcTeamManager().disbandTeam(uhcTeam);
                break;

            case "accept":

                if (UHC.getInstance().getUhcTeamRequestManager().getTeamRequest(player.getUniqueId()) == null) {
                    player.sendMessage("§cYou don't have any Team request");
                    return true;
                }

                if (uhcTeam != null) {
                    player.sendMessage("§cYou're already in a Team");
                    return true;
                }

                newUhcTeam.getPlayers().forEach(teamPlayers -> Bukkit.getPlayer(teamPlayers).sendMessage("§6" + player.getName() + " §7has §ajoined §7your Team"));
                player.sendMessage("§aYou joined the " + newUhcTeam.getOwner().getName() + "'s Team");

                UHC.getInstance().getUhcTeamManager().addPlayerToTeam(player, newUhcTeam);
                UHC.getInstance().getUhcTeamRequestManager().removeTeamRequest(player.getUniqueId());
                break;

            case "deny":

                if (UHC.getInstance().getUhcTeamRequestManager().getTeamRequest(player.getUniqueId()) == null) {
                    player.sendMessage("§cYou don't have any Team request");
                    return true;
                }

                UHC.getInstance().getUhcTeamRequestManager().removeTeamRequest(player.getUniqueId());

                OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(newUhcTeam.getOwner().getName());
                if (!targetOfflinePlayer.isOnline()) {
                    return true;
                }

                targetOfflinePlayer.getPlayer().sendMessage("§6" + player.getName() + " §7denied your Team request");
                break;

            default:
                player.sendMessage("§cPlease use /team <create, invite, accept, leave, disband>");
                break;

        }

        return true;
    }

}
