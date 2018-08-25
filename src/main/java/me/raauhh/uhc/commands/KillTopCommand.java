package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class KillTopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        GameManager gameManager = UHC.getInstance().getGameManager();
        Player player = (Player) sender;

        if (!gameManager.isGameRunning()) {
            player.sendMessage("§cThe game is not running.");
            return true;
        }

        Map<String, Integer> kills = new HashMap<>();
        UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(uhcPlayer -> {
            if (uhcPlayer.getKills() != 0) kills.put(uhcPlayer.getName(), uhcPlayer.getKills());
        });

        if (kills.keySet().isEmpty()) {
            player.sendMessage("§cNobody has killed anyone in this game.");
            return true;
        }

        Map<String, Integer> orderedKills = order(kills);
        int current = 1;

        player.sendMessage("§7§m----------------------");
        player.sendMessage("§fTop §610 Killers§f:");
        player.sendMessage("");
        for (String name : orderedKills.keySet()) {
            if (current++ == 11) break;
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(name);
            if (uhcPlayer.isAlive()) player.sendMessage("§6#" + current + " §a" + name + "§7: §f" + orderedKills.get(name));
            else player.sendMessage("§6#" + current + " §c" + name + "§7: §f" + orderedKills.get(name));
        }
        player.sendMessage("§7§m----------------------");
        return true;
    }

    private <K, V extends Comparable<? super V>> Map<K, V> order(Map<K, V> map) {
        LinkedList<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        list.forEach(kvEntry -> result.put(kvEntry.getKey(), kvEntry.getValue()));
        return result;
    }
}
