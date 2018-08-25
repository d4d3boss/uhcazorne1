package me.raauhh.uhc.manager.team;

import me.raauhh.uhc.UHC;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UHCTeamRequestManager {

    private Map<UUID, UHCTeam> teamRequest = new HashMap<>();

    public void sendRequest(Player target, UHCTeam uhcTeam) {

        Player player = uhcTeam.getOwner();
        UHCTeam targetUhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(target.getUniqueId());
        if (targetUhcTeam != null){
            player.sendMessage("§cThat player is already in a Team");
            return;
        }

        if (teamRequest.containsKey(target.getUniqueId()) && teamRequest.get(target.getUniqueId()) == uhcTeam) {
            player.sendMessage("§cThat player has already a Team request from you");
            return;
        }

        teamRequest.put(target.getUniqueId(), uhcTeam);

        player.sendMessage("§aYou've invited §6" + target.getName() + " §ato your Team");
        target.sendMessage("§6" + player.getName() + " §ahas invited you to their Team");

        TextComponent accept = new TextComponent("§a[Accept]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aAccept the Team request").create()));

        TextComponent denied = new TextComponent(" §c[Deny]");
        denied.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team deny"));
        denied.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cDeny the Team request").create()));

        accept.addExtra(denied);
        target.spigot().sendMessage(accept);

        requestTeamTimer(target, uhcTeam);
    }

    public void requestTeamTimer(final Player target, final UHCTeam team) {
        new BukkitRunnable() {
            public void run() {
                if (teamRequest.get(target.getUniqueId()) != null) {

                    OfflinePlayer offlinePlayer = team.getOwner().getPlayer();
                    target.sendMessage("§cTeam request from §6" + offlinePlayer.getName() + "§c has expired");

                    if (offlinePlayer.isOnline()) {
                        offlinePlayer.getPlayer().sendMessage("§6" + target.getName() + " §cTeam invitation has expired");
                    }
                    removeTeamRequest(target.getUniqueId());
                }
            }
        }.runTaskLater(UHC.getInstance(), 20 * 60);
    }

    public UHCTeam getTeamRequest(UUID uuid) {
        return teamRequest.get(uuid);
    }

    public void removeTeamRequest(UUID uuid) {
        teamRequest.remove(uuid);
    }
}
