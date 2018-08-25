package me.raauhh.uhc.listeners.gamemodes;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.gamemode.Gamemode;
import me.raauhh.uhc.events.CombatLoggerDeathEvent;
import me.raauhh.uhc.manager.combatlogger.CombatLogger;
import me.raauhh.uhc.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TimeBomb implements Gamemode {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        if (gameManager.getGameState() != GameManager.State.GAME) return;

        Player player = event.getEntity();

        player.getLocation().getBlock().setType(Material.CHEST);
        player.getLocation().getBlock().getRelative(BlockFace.EAST).setType(Material.CHEST);
        player.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
        player.getLocation().add(0, 1, 0).getBlock().getRelative(BlockFace.EAST).setType(Material.AIR);

        Chest chest = (Chest) player.getLocation().getBlock().getState();

        for (ItemStack item : event.getDrops()) {
            if (item == null || item.getType() == Material.AIR) continue;
            chest.getInventory().addItem(item);
        }
        chest.getInventory().addItem(gameManager.getGoldenHead());
        chest.update();
        event.getDrops().clear();

        player.getLocation().getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(player.getTotalExperience() * 7);
        new BoomChest(player.getName(), chest.getLocation()).runTaskTimer(UHC.getInstance(), 20, 20);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCombatLoggerDeathEvent(CombatLoggerDeathEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        if (gameManager.getGameState() != GameManager.State.GAME) return;

        CombatLogger combatLogger = event.getCombatLogger();

        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack item : combatLogger.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;
            drops.add(item);
        }
        for (ItemStack item : combatLogger.getArmor()) {
            if (item == null || item.getType() == Material.AIR) continue;
            drops.add(item);
        }

        handleDeath(combatLogger, drops);
    }

    @EventHandler
    public void onBedrockExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.getType() == Material.BEDROCK);
    }

    public static void handleDeath(CombatLogger combatLogger, List<ItemStack> drops) {
        GameManager gameManager = UHC.getInstance().getGameManager();

        combatLogger.getEntity().getLocation().getBlock().setType(Material.CHEST);
        combatLogger.getEntity().getLocation().getBlock().getRelative(BlockFace.EAST).setType(Material.CHEST);
        combatLogger.getEntity().getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
        combatLogger.getEntity().getLocation().add(0, 1, 0).getBlock().getRelative(BlockFace.EAST).setType(Material.AIR);

        Chest chest = (Chest) combatLogger.getEntity().getLocation().getBlock().getState();

        for (ItemStack item : drops) {
            if (item == null || item.getType() == Material.AIR) continue;
            chest.getInventory().addItem(item);
        }

        chest.getInventory().addItem(gameManager.getGoldenHead());
        combatLogger.getEntity().getLocation().getWorld().spawn(combatLogger.getEntity().getLocation(), ExperienceOrb.class).setExperience(combatLogger.getExperience() * 7);
        new BoomChest(combatLogger.getName(), chest.getLocation()).runTaskTimer(UHC.getInstance(), 20, 20);
    }

    private static class BoomChest extends BukkitRunnable {

        private int count = 30;
        private Location location;
        private String name;
        private Hologram hologram;

        BoomChest(String name, Location location) {
            this.name = name;
            this.location = location;
            this.hologram = HologramAPI.createHologram(location.add(1, 1, 0.5), "§6" + count + "s");
            this.hologram.spawn();
        }

        public void run() {
            hologram.setText("§6" + count + "s");
            if (count-- == 0) {
                this.cancel();
                HologramAPI.removeHologram(hologram);

                this.location.getWorld().spigot().strikeLightning(this.location, true);
                this.location.getWorld().createExplosion(this.location, 10f);
                Bukkit.broadcastMessage("§7[§6TimeBomb§7] §f" + this.name + "'s corpse has exploded!");
            }
        }
    }

    public String getName() {
        return "TimeBomb";
    }

    public ItemStack getItem() {
        List<String> lore = new ArrayList<>();
        lore.add("§f- All items on death are put into a double chest");
        lore.add("§f- The body/chest blow up 30 seconds after dying");
        return new ItemUtil(Material.TNT).setName("§aTimeBomb").setLore(lore).get();
    }

    public void deactivate() {
        PlayerDeathEvent.getHandlerList().unregister(this);
    }
}
