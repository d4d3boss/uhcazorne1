package me.raauhh.uhc.manager.team;

import lombok.Getter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.UUID;

@Getter
public class UHCTeam {

    private HashSet<UUID> players = new HashSet<>();
    private Player owner;

    private int number;
    private ChatColor color;
    private String prefix;

    private Inventory backPack;

    public UHCTeam(Player player, int number) {
        this.owner = player;
        this.number = number;
        this.color = Common.randomChatColor();
        this.prefix = "§7[" + this.color + "#" + number + "§7]";
        this.backPack = Bukkit.createInventory(null, 27, "§eTeam #" + number + " BackPack");
    }

    public int getKills() {
        int currentKills = 0;
        for (UUID player : players) {
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player);
            currentKills = currentKills + uhcPlayer.getKills();
        }
        return currentKills;
    }

    public boolean isAlive() {
        for (UUID player : players) {
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player);
            if (uhcPlayer.isAlive()) return true;
        }
        return false;
    }

    public void removePlayer(OfflinePlayer offlinePlayer) {
        this.players.remove(offlinePlayer.getUniqueId());
    }

    public void addPlayer(Player player) {
        this.players.add(player.getUniqueId());
    }

}
