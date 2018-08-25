package me.raauhh.uhc.tasks;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.gamemode.GamemodeManager;
import net.minecraft.util.com.google.gson.JsonObject;
import me.raauhh.uhc.utils.Common;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardTask extends BukkitRunnable {
    @Override
    public void run() {
        if (!UHC.getInstance().getRScoreboardManager().getPlayers().keySet().isEmpty()) {
            UHC.getInstance().getRScoreboardManager().getPlayers().keySet().forEach(players -> {
                Player player = Bukkit.getPlayer(players);
                if (player != null) UHC.getInstance().getRScoreboardManager().update(player);
            });
        }

        if (UHC.getInstance().isUsingRedis()) {
            GameManager gameManager = UHC.getInstance().getGameManager();
            List<String> gamemodes = new ArrayList<>();
            if (!UHC.getInstance().getGamemodeManager().getEnabledGamemodes().isEmpty()) {
                gamemodes = new ArrayList<>(GamemodeManager.getUhcGamemodes().keySet());
            }

            JsonObject object = new JsonObject();
            object.addProperty("server", Bukkit.getServerName());
            object.addProperty("scheduledCounter", Common.calculate(gameManager.getScheduledTime()));
            object.addProperty("startCounter", Common.calculate(gameManager.getStartTime()));
            object.addProperty("state", gameManager.getGameState().toString());
            object.addProperty("gameTime", Common.calculate(gameManager.getGameTime()));
            object.addProperty("scenarios", (gamemodes.isEmpty() ? "null" : StringUtils.join(gamemodes, ",")));
            object.addProperty("playersOnline", Common.getOnlinePlayers().size());
            object.addProperty("maxPlayersAlive", gameManager.getMaxScatteredPlayers());
            object.addProperty("playersAlive", gameManager.getAlivePlayers().size());
            object.addProperty("currentBorder", gameManager.getCurrentBorder());
            object.addProperty("teams", (gameManager.isTeams() ? "To" + gameManager.getTeamSize() : "FFA"));
            object.addProperty("scheduled", gameManager.isScheduled());
            object.addProperty("whitelist", gameManager.isWhitelist());
            UHC.getInstance().getRedisMessagingHandler().sendMessage("uhc:serverinfo", object.toString());
        }
    }
}
