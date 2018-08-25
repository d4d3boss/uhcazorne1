package me.raauhh.uhc.listeners;

import com.wimbli.WorldBorder.Events.WorldBorderFillFinishedEvent;
import com.wimbli.WorldBorder.Events.WorldBorderFillStartEvent;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.WorldManager;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class WorldListener implements Listener {

    private BukkitTask worldBorderTask;

    @EventHandler
    public void onWorldBorderFillStart(WorldBorderFillStartEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        worldBorderTask = Bukkit.getScheduler().runTaskTimer(UHC.getInstance(), () -> gameManager.setGenerationPercent((int) event.getFillTask().getPercentageCompleted()), 1, 1);
    }

    @EventHandler
    public void onWorldBorderFillFinished(WorldBorderFillFinishedEvent event) {
        worldBorderTask.cancel();

        WorldManager worldManager = UHC.getInstance().getWorldManager();
        switch (event.getWorld().getName()) {
            case "uhc_world":
                worldManager.shrinkBorder(worldManager.getUhcWorldSize(), 6);

                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
                    World world = Bukkit.createWorld(new WorldCreator(worldManager.getUhcWorldNetherName()).environment(World.Environment.NETHER).type(WorldType.NORMAL));
                    world.setPVP(false);
                    world.setDifficulty(Difficulty.HARD);
                    worldManager.setUhcWorldNether(world);

                    int size = worldManager.getUhcWorldNetherSize();
                    Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb shape square");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + worldManager.getUhcWorldNetherName() + " set " + size + " " + size + " 0 0");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + worldManager.getUhcWorldNetherName() + " fill 1000");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm");
                    }, 20 * 5);
                }, 20 * 2);
                break;
            case "uhc_world_nether":
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
                }, 20 * 5);
                break;
            default:
                break;
        }

    }
}
