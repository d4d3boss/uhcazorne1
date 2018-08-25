package me.raauhh.uhc.listeners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.WorldManager;
import me.raauhh.uhc.manager.visualise.VisualType;
import me.raauhh.uhc.manager.visualise.packet.BlockDigAdapter;
import me.raauhh.uhc.manager.visualise.packet.BlockPlaceAdapter;
import me.raauhh.uhc.utils.Common;
import me.raauhh.uhc.utils.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GlassBorderListener extends BukkitRunnable implements Listener {

    private ConcurrentMap<Player, Location> previous = new ConcurrentHashMap<>();

    public GlassBorderListener() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new BlockDigAdapter(UHC.getInstance()));
        manager.addPacketListener(new BlockPlaceAdapter(UHC.getInstance()));

        Common.getOnlinePlayers().forEach(player -> previous.put(player, player.getLocation()));
        runTaskTimerAsynchronously(UHC.getInstance(), 0, 1);
    }

    @Override
    public void run() {
        if(UHC.getInstance().getGameManager().getGameState() != GameManager.State.GAME) return;

        for (Map.Entry<Player, Location> entry : previous.entrySet()) {
            Player player = entry.getKey();
            if (player.isOnline()) {
                if(player.getWorld() != UHC.getInstance().getWorldManager().getUhcWorld()) return;

                Location from = entry.getValue();

                Location to = player.getLocation();
                int toX = to.getBlockX();
                int toY = to.getBlockY();
                int toZ = to.getBlockZ();

                if (from.getX() != toX || from.getY() != toY || from.getZ() != toZ) {
                    handlePositionChanged(player, to.getWorld(), toX, toY, toZ);
                    previous.replace(player, player.getLocation());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        previous.remove(player);
        UHC.getInstance().getVisualiseHandler().clearVisualBlocks(player, null, null, false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        previous.put(player, player.getLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UHC.getInstance().getVisualiseHandler().clearVisualBlocks(player, null, null, false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();

        Location to = event.getTo();
        int toX = to.getBlockX();
        int toY = to.getBlockY();
        int toZ = to.getBlockZ();

        if (from.getX() != toX || from.getY() != toY || from.getZ() != toZ)
            handlePositionChanged(event.getPlayer(), to.getWorld(), toX, toY, toZ);
    }

    private void handlePositionChanged(Player player, World toWorld, int toX, int toY, int toZ) {
        VisualType visualType;
        visualType = VisualType.BORDER;

        int minHeight = toY - 4;
        int maxHeight = toY + 5;

        GameManager gameManager = UHC.getInstance().getGameManager();
        WorldManager worldManager = UHC.getInstance().getWorldManager();

        Collection<Vector> edges = new Cuboid(worldManager.getUhcWorld(), gameManager.getCurrentBorder(), 100, gameManager.getCurrentBorder(), -gameManager.getCurrentBorder() - 1, 0, -gameManager.getCurrentBorder() - 1).edges();
        for (Vector edge : edges) {
            if (Math.abs(edge.getBlockX() - toX) > 7) continue;
            if (Math.abs(edge.getBlockZ() - toZ) > 7) continue;

            Location location = edge.toLocation(toWorld);
            if (location != null) {
                Location first = location.clone();
                Location second = location.clone();
                first.setY(minHeight);
                second.setY(maxHeight);
                UHC.getInstance().getVisualiseHandler().generate(player, new Cuboid(first, second), visualType, false).size();
            }
        }

        UHC.getInstance().getVisualiseHandler().clearVisualBlocks(player, visualType, visualBlock -> {
            Location other = visualBlock.getLocation();
            return other.getWorld().equals(toWorld) && (Math.abs(toX - other.getBlockX()) > 7 || Math.abs(toY - other.getBlockY()) > 7 || Math.abs(toZ - other.getBlockZ()) > 7);
        });
    }
}