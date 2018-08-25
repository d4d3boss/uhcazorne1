package me.raauhh.uhc.manager.team;

import lombok.Getter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.player.UHCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

@Getter
public class UHCTeamManager {

    private Map<UUID, UHCTeam> allTeams = new HashMap<>();

    private int createdTeams = 0;

    public void createTeam(Player player) {
        this.createdTeams++;
        UHCTeam uhcTeam = this.allTeams.get(player.getUniqueId());

        if (uhcTeam != null) {
            player.sendMessage("§cYou're already in a team");
            return;
        }

        player.sendMessage("§aYour team has been created");
        addPlayerToTeam(player, new UHCTeam(player, this.createdTeams));
    }


    public void disbandTeam(UHCTeam uhcTeam) {
        uhcTeam.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage("§cYour team has been disband");
            this.allTeams.remove(uuid);
        });
    }

    public void addPlayerToTeam(Player player, UHCTeam uhcTeam) {
        if (uhcTeam.getPlayers().size() == UHC.getInstance().getGameManager().getTeamSize()) {
            player.sendMessage("§cThe team has reach the game team size");
            return;
        }

        this.allTeams.put(player.getUniqueId(), uhcTeam);
        uhcTeam.addPlayer(player);
    }

    public void removePlayerFromTeam(UUID uuid) {
        this.allTeams.get(uuid).removePlayer(Bukkit.getOfflinePlayer(uuid));
        this.allTeams.remove(uuid);
    }


    public void getTeamList(Player player, OfflinePlayer target) {

        UHCTeam uhcTeam = this.allTeams.get(target.getUniqueId());

        player.sendMessage(uhcTeam.getPrefix() + " §e" + uhcTeam.getOwner().getName() + "'s Team");
        for (UUID uuid : uhcTeam.getPlayers()) {
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(uuid);
            if (uhcPlayer.isAlive()) {
                Damageable damageable = Bukkit.getPlayer(uuid);
                double health = damageable.getHealth() / 2.0;
                DecimalFormat decimalFormat = new DecimalFormat("#.#");

                player.sendMessage(" §7- §a" + uhcPlayer.getPlayer().getName() + "§7: §6" + decimalFormat.format(health) + " §4❤");
                continue;
            }
            player.sendMessage(" §7- §c" + uhcPlayer.getName() + " §4§m ❤");
        }
    }

    public void removeAllTeams() {
        this.createdTeams = 0;
        //this.allTeams.keySet().forEach(this::removePlayerFromTeam);
        this.allTeams.clear();
    }


    public int getAliveTeams() {
        List<UHCTeam> uhcTeams = new ArrayList<>();
        this.allTeams.values().forEach(uhcTeam -> {
            if (uhcTeam.isAlive() && !uhcTeams.contains(uhcTeam)) {
                uhcTeams.add(uhcTeam);
            }
        });
        return uhcTeams.size();
    }

}
