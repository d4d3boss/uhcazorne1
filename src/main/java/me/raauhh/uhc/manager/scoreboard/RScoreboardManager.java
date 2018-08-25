package me.raauhh.uhc.manager.scoreboard;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.manager.team.UHCTeam;
import me.raauhh.uhc.utils.Common;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RScoreboardManager {

    private HashMap<UUID, RScoreboard> players = new HashMap<>();

    public void create(Player player) {
        players.put(player.getUniqueId(), new RScoreboard(player));
    }

    public RScoreboard get(Player player) {
        return players.get(player.getUniqueId());
    }

    public void remove(Player player) {
        players.remove(player.getUniqueId());
    }

    public HashMap<UUID, RScoreboard> getPlayers() {
        return players;
    }

    public void update(Player player) {

        RScoreboard scoreboard = get(player);
        UHC suhc = UHC.getInstance();
        GameManager gameManager = UHC.getInstance().getGameManager();

        UHCPlayer uhcPlayer = suhc.getUhcPlayerManager().getPlayer(player.getUniqueId());

        scoreboard.updateNametags();
        scoreboard.setTitle("§b§lPAINFULL");

        List<String> lines = new ArrayList<>();
        lines.add("§7§m-----------------------");
        switch (UHC.getInstance().getGameManager().getGameState()) {
            case LOBBY:
                lines.add("§aPlayers§7: §f" + Common.getOnlinePlayers().size());
                lines.add("§aTeams§7: §f" + (gameManager.isTeams() ? "To" + gameManager.getTeamSize() : "FFA"));
                lines.add("");
                if (suhc.getGamemodeManager().getEnabledGamemodes().isEmpty()) {
                    lines.add("§a§mGamemodes§7:");
                } else {
                    lines.add("§aGamemodes§7:");
                    suhc.getGamemodeManager().getEnabledGamemodes().forEach(gamemode -> lines.add(" §7-§f " + gamemode.getName()));
                }
                if (gameManager.isScheduled()) {
                    lines.add("");
                    if (gameManager.isWhitelist())
                        lines.add("§aOpens in§7:§f " + Common.calculate(gameManager.getScheduledTime()));
                    else lines.add("§aScattering §ain§7:§f " + Common.calculate(gameManager.getStartTime()));
                } else {
                    lines.add("");
                    lines.add("§cThis game is not scheduled");
                }
                break;
            case SCATTER:
                lines.add("§aPlayers§7: §f" + Common.getOnlinePlayers().size());
                lines.add("§aStarts in§7:§f " + Common.calculate(gameManager.getScatterCountdown()));
                break;
            case GAME:
                lines.add("§aGame Time§7:§f " + Common.calculate(UHC.getInstance().getGameManager().getGameTime()));
                lines.add("§aPlayers§7:§f " + gameManager.getAlivePlayers().size() + "/" + gameManager.getMaxScatteredPlayers());

                if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) {
                    lines.add("§aKills§7:§f " + uhcPlayer.getKills());
                    if (gameManager.isTeams()) {
                        UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());
                        lines.add("§aTeam Kills§7: §f" + uhcTeam.getKills());
                    }
                }else{
                    if (gameManager.isTeams()) {
                        UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());
                        if(uhcTeam.isAlive()) lines.add("§aTeam Kills§7: §f" + uhcTeam.getKills());
                    }
                }

                if (!gameManager.isDeathmatchTimeAlready()) {
                    if (gameManager.isBorderTimeAlready())
                        lines.add("§aBorder§7:§f " + gameManager.getCurrentBorder() + " §7(§9" + Common.simpleCalculate(gameManager.getBorderCount()) + "§7)");
                    else lines.add("§aBorder§7:§f " + gameManager.getCurrentBorder());
                }
                break;
        }
        lines.add("§7§m-----------------------");

        scoreboard.setSlotsFromList(lines);
    }
}
