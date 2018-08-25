package me.raauhh.uhc.listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.util.com.google.gson.JsonObject;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.events.GameScheduleEvent;
import me.raauhh.uhc.events.GameSetUpEvent;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.WorldManager;
import me.raauhh.uhc.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;

public class LobbyListener implements Listener {

    private GameManager gameManager;
    private WorldManager worldManager;

    private BukkitTask schedulerTask;

    public LobbyListener() {
        this.gameManager = UHC.getInstance().getGameManager();
        this.worldManager = UHC.getInstance().getWorldManager();
    }

    @EventHandler
    public void onGameScheduleEvent(GameScheduleEvent event) {
        JsonObject object = new JsonObject();
        UHC.getInstance().getGameManager().setScheduled(true);

        new BukkitRunnable() {
            public void run() {
                Date date = new Date();
                int dateSeconds = Math.abs((date.getMinutes() * 60) + date.getSeconds());
                int scheduledDateSeconds = Math.abs(event.getScheduledDate().getMinutes() * 60);

                if (gameManager.isGameRunning()) this.cancel();
                Common.getCounter(dateSeconds, scheduledDateSeconds, null, "The game will be unwhitelisted in §a<time>§f.");
                if (dateSeconds != scheduledDateSeconds)
                    gameManager.setScheduledTime(scheduledDateSeconds - dateSeconds);
                else {
                    this.cancel();
                    Bukkit.broadcastMessage("§cThe game has been unwhitelisted.");

                    if (UHC.getInstance().isUsingRedis())
                        UHC.getInstance().getRedisMessagingHandler().sendMessage("uhc:open", object.toString());
                    gameManager.setWhitelist(false);

                    Date startDate = new Date();
                    startDate.setMinutes(startDate.getMinutes() + 5);
                    startDate.setSeconds(0);
                    new BukkitRunnable() {
                        public void run() {
                            Date date = new Date();
                            int dateSeconds = Math.abs((date.getMinutes() * 60) + date.getSeconds());
                            int scheduledDateSeconds = Math.abs(startDate.getMinutes() * 60);

                            if (gameManager.isGameRunning()) this.cancel();

                            Common.getCounter(dateSeconds, scheduledDateSeconds, null, "The scatter will start in §a<time>§f.");

                            if (dateSeconds != scheduledDateSeconds)
                                gameManager.setStartTime(Math.abs(scheduledDateSeconds - dateSeconds));
                            else {
                                this.cancel();
                                gameManager.setWhitelist(true);
                                Bukkit.broadcastMessage("§cScatter has begun.");

                                if (UHC.getInstance().isUsingRedis()) {
                                    JsonObject object = new JsonObject();
                                    object.addProperty("server", Bukkit.getServerName());
                                    UHC.getInstance().getRedisMessagingHandler().sendMessage("uhc:start", object.toString());
                                }
                                Bukkit.getPluginManager().callEvent(new GameSetUpEvent());
                            }
                        }
                    }.runTaskTimer(UHC.getInstance(), 0, 20);
                }
            }
        }.runTaskTimer(UHC.getInstance(), 0, 20);

        Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
            object.addProperty("server", Bukkit.getServerName());
            if (UHC.getInstance().isUsingRedis())
                UHC.getInstance().getRedisMessagingHandler().sendMessage("uhc:schedule", object.toString());
        }, 20);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (gameManager.getGameState() != GameManager.State.LOBBY) return;
        Player player = event.getPlayer();

        UHC.getInstance().getUhcPlayerManager().preparePlayerLobby(player);

        TextComponent accept = new TextComponent("§fFor more info of this game. Type §6/config §for §6Click Here");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/config"));
        player.spigot().sendMessage(accept);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        World blockWorld = event.getBlock().getWorld();

        if (blockWorld == worldManager.getWorld()) event.setCancelled(true);
        if (gameManager.getGameState() == GameManager.State.SCATTER) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        World blockWorld = event.getBlock().getWorld();

        if (blockWorld == worldManager.getWorld()) event.setCancelled(true);
        if (gameManager.getGameState() == GameManager.State.SCATTER) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        World world = event.getPlayer().getLocation().getBlock().getWorld();

        if (world == worldManager.getWorld()) event.setCancelled(true);
        if (gameManager.getGameState() == GameManager.State.SCATTER) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        World world = event.getEntity().getLocation().getBlock().getWorld();

        if (world == worldManager.getWorld()) event.setCancelled(true);
        if (gameManager.getGameState() == GameManager.State.SCATTER) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEvent(FoodLevelChangeEvent event) {
        World world = event.getEntity().getLocation().getBlock().getWorld();

        if (world == worldManager.getWorld()) event.setCancelled(true);
        if (gameManager.getGameState() == GameManager.State.SCATTER) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEvent(WeatherChangeEvent event) {
        event.setCancelled(true);
    }
}
