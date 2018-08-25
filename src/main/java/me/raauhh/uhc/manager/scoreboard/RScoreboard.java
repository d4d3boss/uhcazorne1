package me.raauhh.uhc.manager.scoreboard;

import lombok.Getter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.manager.team.UHCTeam;
import me.raauhh.uhc.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

@Getter
class RScoreboard {

    private Player player;
    private Scoreboard scoreboard;
    private Objective sidebar;

    private Team self;
    private Team other;
    private Team spec;
    private Team noclean;

    RScoreboard(Player player) {
        this.player = player;

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        Objective name = scoreboard.registerNewObjective("name", "health");
        name.setDisplaySlot(DisplaySlot.BELOW_NAME);
        name.setDisplayName("§4❤");

        noclean = scoreboard.getTeam("noclean");
        if (noclean == null) noclean = scoreboard.registerNewTeam("noclean");
        noclean.setPrefix(UHC.getInstance().getConfig().getString("colored-names.noclean"));

        spec = scoreboard.getTeam("spec");
        if (spec == null) spec = scoreboard.registerNewTeam("spec");
        spec.setPrefix(UHC.getInstance().getConfig().getString("colored-names.spectators"));
        spec.setCanSeeFriendlyInvisibles(true);

        self = scoreboard.getTeam("self");
        if (self == null) self = scoreboard.registerNewTeam("self");
        self.setPrefix(UHC.getInstance().getConfig().getString("colored-names.teammates"));

        other = scoreboard.getTeam("other");
        if (other == null) other = scoreboard.registerNewTeam("other");
        other.setPrefix(UHC.getInstance().getConfig().getString("colored-names.enemies"));

        player.setScoreboard(scoreboard);

        for (int i = 1; i <= 15; i++) {
            Team team = scoreboard.registerNewTeam("SLOT_" + i);
            team.addEntry(genEntry(i));
        }
    }

    void setTitle(String title) {
        if (title.length() > 32) title = title.substring(0, 32);
        if (!sidebar.getDisplayName().equals(title)) sidebar.setDisplayName(title);
    }

    private void setSlot(int slot, String text) {
        Team team = scoreboard.getTeam("SLOT_" + slot);
        String entry = genEntry(slot);

        if (!scoreboard.getEntries().contains(entry)) sidebar.getScore(entry).setScore(slot);

        String prefix = getFirstSplit(text);
        String suffix = getFirstSplit(ChatColor.getLastColors(prefix) + getSecondSplit(text));

        if (!team.getPrefix().equals(prefix)) team.setPrefix(prefix);
        if (!team.getSuffix().equals(suffix)) team.setSuffix(suffix);
    }

    private void removeSlot(int slot) {
        String entry = genEntry(slot);
        if (scoreboard.getEntries().contains(entry)) scoreboard.resetScores(entry);
    }

    void setSlotsFromList(List<String> list) {
        int slot = list.size();
        if (slot < 15) for (int i = (slot + 1); i <= 15; i++) removeSlot(i);
        for (String line : list) {
            setSlot(slot, line);
            slot--;
        }
    }

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    private String getFirstSplit(String s) {
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private String getSecondSplit(String s) {
        if (s.length() > 32) s = s.substring(0, 32);
        return s.length() > 16 ? s.substring(16, s.length()) : "";
    }

    void updateNametags() {
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());
        UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.SPEC) {
            Common.getOnlinePlayers().forEach(onlinePlayers -> {
                UHCPlayer targetPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(onlinePlayers.getUniqueId());
                if (targetPlayer.getPlayerState() == UHCPlayer.State.SPEC) {
                    if (!spec.hasPlayer(onlinePlayers)) spec.addPlayer(onlinePlayers);
                } else {
                    if (UHC.getInstance().getGameManager().isTeams()) {
                        if (uhcTeam != null) {
                            if (uhcTeam.getPlayers().contains(onlinePlayers.getUniqueId())) {
                                if (!spec.hasPlayer(onlinePlayers))
                                    if (!self.hasPlayer(onlinePlayers)) self.addPlayer(onlinePlayers);
                            }
                        }
                    } else {
                        if (!other.hasPlayer(onlinePlayers)) other.addPlayer(onlinePlayers);
                    }
                }
            });
            return;
        }

        Common.getOnlinePlayers().forEach(onlinePlayers -> {
            if (UHC.getInstance().getGameManager().isTeams()) {
                if (uhcTeam == null) {
                    if (onlinePlayers == player) {
                        if (!self.hasPlayer(onlinePlayers)) self.addPlayer(onlinePlayers);
                    } else {
                        if (!other.hasPlayer(onlinePlayers)) other.addPlayer(onlinePlayers);
                    }
                } else {
                    if (onlinePlayers == player || uhcTeam.getPlayers().contains(onlinePlayers.getUniqueId())) {
                        if (!self.hasPlayer(onlinePlayers)) self.addPlayer(onlinePlayers);
                    } else {
                        if (!other.hasPlayer(onlinePlayers)) other.addPlayer(onlinePlayers);
                    }
                }
            } else {
                if (onlinePlayers == player) {
                    if (!self.hasPlayer(onlinePlayers)) self.addPlayer(onlinePlayers);
                } else {
                    if (!other.hasPlayer(onlinePlayers)) other.addPlayer(onlinePlayers);
                }
            }
        });
    }
}
