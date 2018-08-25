package me.raauhh.uhc.listeners;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.combatlogger.CombatLogger;
import me.raauhh.uhc.manager.combatlogger.CombatLoggerManager;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.manager.player.UHCPlayerManager;
import me.raauhh.uhc.manager.scoreboard.RScoreboardManager;
import me.raauhh.uhc.manager.team.UHCTeam;
import me.raauhh.uhc.utils.Common;
import me.raauhh.uhc.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.text.DecimalFormat;
import java.util.Iterator;

public class PlayerListener implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) return;

        UHCPlayerManager uhcPlayerManager = UHC.getInstance().getUhcPlayerManager();
        if (!uhcPlayerManager.existsPlayer(event.getUniqueId())) uhcPlayerManager.createPlayer(event.getUniqueId());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();

        Player player = event.getPlayer();

        UHCPlayerManager uhcPlayerManager = UHC.getInstance().getUhcPlayerManager();
        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();

        UHCPlayer uhcPlayer = uhcPlayerManager.getPlayer(player.getUniqueId());
        uhcPlayer.setPlayer(player);
        uhcPlayer.setName(player.getName());
        uhcPlayer.setUuid(player.getUniqueId());

        switch (gameManager.getGameState()) {
            case GENERATING:
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cYou're not allowed to join while world generation is running: " + gameManager.getGenerationPercent() + "%");
                break;
            case LOBBY:
                if (player.hasPermission("uhc.staff")) break;

                if (!gameManager.isScheduled()) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cThis game is not scheduled.");
                    break;
                }

                if (player.hasPermission("uhc.vip")) break;
                if (gameManager.isWhitelist()) {
                    if (UHC.getInstance().getWhitelist().contains(player.getName().toLowerCase())) break;
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cThis game is now only available for VIP players.");
                } else {
                    if (gameManager.getSlots() >= Common.getOnlinePlayers().size()) break;
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cThis game is full. Acquires a rank to bypass the limit");
                }
                break;
            case SCATTER:
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cYou're not allowed to join while scattering");
                break;
            case GAME:
                if (uhcPlayer.getPlayerState() == null) {
                    if (gameManager.isPvpTimeAlready()) {
                        if (player.hasPermission("uhc.spec")) break;
                        if (player.hasPermission("uhc.vip"))
                            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cYour late scatter time period has ended. To spectate, acquires a rank.");
                        else
                            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cThe game has already begun.");
                    } else {
                        if (player.hasPermission("uhc.vip")) break;
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cThis game has already begun.");
                    }
                    return;
                }

                switch (uhcPlayer.getPlayerState()) {
                    case SPEC:
                        break;
                    case PLAYING:
                        CombatLogger combatLogger = combatLoggerManager.get(player.getUniqueId());
                        if (combatLogger == null) {
                            if (player.hasPermission("uhc.vip")) break;
                            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cYou have died. To spectate, acquires a rank");
                        }
                        break;
                    case DEAD:
                        if (player.hasPermission("uhc.spec")) break;
                        // TODO: detect if he get respawned and allow to join
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cYou have died. To spectate, acquires a rank");
                        break;
                }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        GameManager gameManager = UHC.getInstance().getGameManager();

        player.setMaximumNoDamageTicks(19);

        RScoreboardManager scoreboardManager = UHC.getInstance().getRScoreboardManager();
        scoreboardManager.create(player);

        UHCPlayerManager uhcPlayerManager = UHC.getInstance().getUhcPlayerManager();
        UHCPlayer uhcPlayer = uhcPlayerManager.getPlayer(player.getUniqueId());

        if (gameManager.getGameState() == GameManager.State.GAME) {
            if (uhcPlayer.getPlayerState() == null) {
                if (gameManager.isPvpTimeAlready()) {
                    Location location = new Location(UHC.getInstance().getWorldManager().getUhcWorld(), 0, UHC.getInstance().getWorldManager().getUhcWorld().getHighestBlockYAt(0, 0) + 5, 0);
                    player.teleport(location);
                    player.sendMessage("§cCouldn't get late scattered because the grace period has ended.");
                    uhcPlayerManager.prepareSpecator(player);
                } else {
                    gameManager.setMaxScatteredPlayers(gameManager.getMaxScatteredPlayers() + 1);
                    if (gameManager.isTeams()) {
                        UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());
                        if (uhcTeam == null) UHC.getInstance().getUhcTeamManager().createTeam(player);
                    }

                    uhcPlayer.setPlayerState(UHCPlayer.State.PLAYING);
                    uhcPlayerManager.preparePlayerGame(uhcPlayer);

                    player.teleport(new LocationUtil().randomLocation(UHC.getInstance().getWorldManager().getUhcWorld(), gameManager.getCurrentBorder()));
                    player.sendMessage("§eYou've been late scattered.");
                }
                return;
            }

            switch (uhcPlayer.getPlayerState()) {
                case PLAYING:
                    break;
                case SPEC:
                    Location location = new Location(UHC.getInstance().getWorldManager().getUhcWorld(), 0, UHC.getInstance().getWorldManager().getUhcWorld().getHighestBlockYAt(0, 0) + 5, 0);
                    player.teleport(location);
                    uhcPlayerManager.prepareSpecator(player);
                    break;
                case DEAD:
                    // TODO: if(isRespawned) respawn & return

                    if (player.hasPermission("uhc.spec")) {
                        location = new Location(UHC.getInstance().getWorldManager().getUhcWorld(), 0, UHC.getInstance().getWorldManager().getUhcWorld().getHighestBlockYAt(0, 0) + 5, 0);
                        player.teleport(location);
                        player.sendMessage("§cYou have died because your Combat Logger was killed.");
                        uhcPlayerManager.prepareSpecator(player);
                    }
                    break;
            }
        }

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) {
            for (Player target : Common.getOnlinePlayers()) {
                UHCPlayer targetPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(target.getUniqueId());
                if (targetPlayer.getPlayerState() == UHCPlayer.State.SPEC) {
                    player.hidePlayer(targetPlayer.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();

        GameManager gameManager = UHC.getInstance().getGameManager();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (gameManager.getGameState() == GameManager.State.GAME && uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) {
            CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
            CombatLogger combatLogger = combatLoggerManager.get(player.getUniqueId());

            event.setSpawnLocation(combatLogger.getEntity().getLocation());
            player.setHealth(combatLogger.getEntity().getHealth());
            combatLogger.remove(CombatLogger.Remove.JOIN);
        }
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() == UHCPlayer.State.SPEC) {
            if (gameManager.getHost().contains(player.getUniqueId())) {
                event.setFormat("§7[§c§lHost§7] " + event.getFormat());
                return;
            }

            if (gameManager.isEnded()) {
                event.setFormat("§7[Spectator] " + event.getFormat());
                return;
            }

            for (Iterator<Player> it = event.getRecipients().iterator(); it.hasNext(); ) {
                UHCPlayer targetUhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(it.next().getUniqueId());
                if (targetUhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) it.remove();
            }

            event.setFormat("§7[Spectator] " + event.getFormat());
        } else {
            if (gameManager.isTeams()) {
                UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(player.getUniqueId());
                if (uhcTeam == null) return;
                event.setFormat(uhcTeam.getPrefix() + " " + event.getFormat());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        Player player = (Player) arrow.getShooter();
        Damageable damageable = (Damageable) event.getEntity();
        if (!(damageable instanceof Player)) return;

        Player victim = (Player) damageable;

        if (damageable.isDead()) return;
        if (damageable.getHealth() < 0) return;

        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        String hearts = decimalFormat.format(Common.roundToHalf(damageable.getHealth() / 2.0));

        if (player == victim) player.sendMessage("§fYou're now at §c" + hearts + " §4❤");
        else player.sendMessage("§6" + victim.getName() + " §fis now at §c" + hearts + " §4❤");
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        RScoreboardManager scoreboardManager = UHC.getInstance().getRScoreboardManager();
        scoreboardManager.remove(player);

        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());
        if (uhcPlayer.getPlayerState() == UHCPlayer.State.PLAYING) combatLoggerManager.create(player.getUniqueId());
    }

}
