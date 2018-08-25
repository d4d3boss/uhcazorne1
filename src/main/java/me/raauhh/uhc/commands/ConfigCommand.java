package me.raauhh.uhc.commands;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.utils.Common;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConfigCommand implements CommandExecutor {

    private HashMap<UUID, Long> helpOpCooldown = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cError. You must be a player to execute this command");
            return true;
        }

        GameManager gameManager = UHC.getInstance().getGameManager();
        Player player = (Player) sender;

        String gamemodes = "";
        if(!UHC.getInstance().getGamemodeManager().getEnabledGamemodes().isEmpty()){
            List<String> name = new ArrayList<>();
            UHC.getInstance().getGamemodeManager().getEnabledGamemodes().forEach(gamemode -> name.add(gamemode.getName()));
            gamemodes = StringUtils.join(name, "§7, §f");
        }

        player.sendMessage("§7§m----------------------------------------");
        player.sendMessage("§e§lUHC Configuration");
        player.sendMessage("");
        player.sendMessage((gamemodes.equals("") ? "§6§mGamemodes§7:" : "§6Gamemodes§7:§f " + gamemodes));
        player.sendMessage("§6Teams§7:§f " + (gameManager.isTeams() ? "To" + gameManager.getTeamSize() : "FFA"));
        player.sendMessage("");
        player.sendMessage("§aFinal Heal§7:§f " + Common.calculate(gameManager.getHealTime()) + " §8┃ §aPvP§7:§f " + Common.calculate(gameManager.getPvpTime()) + " §8┃ §aBorder§7:§f " + Common.calculate(gameManager.getBorderTime()));
        player.sendMessage("§aApple Rate§7:§f " + gameManager.getAppleRatePercent() + "% §8┃ §aSlots§7:§f " + gameManager.getSlots() + " §8┃ §aStarter Food§7:§f " + gameManager.getStarterFood());
        player.sendMessage("§aNether§7:§f " + gameManager.isNether() + " §8┃ §aShears§7:§f true §8┃ §aStats§7:§f " + gameManager.isStats());
        player.sendMessage("§7§m----------------------------------------");
        return true;
    }
}
