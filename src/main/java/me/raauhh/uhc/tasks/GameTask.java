package me.raauhh.uhc.tasks;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.events.BorderShrinkEvent;
import me.raauhh.uhc.events.DeathMatchStartEvent;
import me.raauhh.uhc.listeners.gamemodes.TimeBomb;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.combatlogger.CombatLogger;
import me.raauhh.uhc.manager.combatlogger.CombatLoggerManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GameTask extends BukkitRunnable {

    private int currentTime;
    private int borderCounter;
    private int actualBorder;
    private int borderShrinks = 0;

    @Override
    public void run() {
        GameManager gameManager = UHC.getInstance().getGameManager();
        gameManager.setGameTime(gameManager.getGameTime() + 1);

        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        if (!combatLoggerManager.getCombatLoggers().values().isEmpty()) {
            combatLoggerManager.getCombatLoggers().values().forEach(combatLogger -> {
                long createdTime = combatLogger.getCreatedTime() + ((10 * 60) * 1000);
                if (Math.abs((createdTime - System.currentTimeMillis()) / 1000) == 0) {

                    UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(combatLogger.getUuid());
                    uhcPlayer.setPlayerState(UHCPlayer.State.DEAD);
                    uhcPlayer.setArmor(combatLogger.getArmor().clone());
                    uhcPlayer.setInventory(combatLogger.getInventory().clone());

                    if (UHC.getInstance().getGamemodeManager().exists("TimeBomb")) {
                        List<ItemStack> drops = new ArrayList<>();
                        for (ItemStack item : combatLogger.getInventory()) {
                            if (item == null || item.getType() == Material.AIR) continue;
                            drops.add(item);
                        }
                        for (ItemStack item : combatLogger.getArmor()) {
                            if (item == null || item.getType() == Material.AIR) continue;
                            drops.add(item);
                        }

                        TimeBomb.handleDeath(combatLogger, drops);
                    } else Common.placeFence(combatLogger.getPlayer(), combatLogger.getEntity().getLocation());
                    combatLogger.remove(CombatLogger.Remove.DEAD);
                }
            });
        }

        Common.getCounter(gameManager.getGameTime(), gameManager.getHealTime(), "§fFinal Heal will be in §a<time>§f.");
        if (gameManager.getGameTime() >= gameManager.getHealTime() && !gameManager.isHealTimeAlready()) {
            gameManager.setHealTimeAlready(true);
            Bukkit.broadcastMessage("§cFinal Heal given.");
            Common.getOnlinePlayers().forEach(players -> players.setHealth(20));
        }

        Common.getCounter(gameManager.getGameTime(), gameManager.getPvpTime(), "§fGrace period will end in §a<time>§f.");
        if (gameManager.getGameTime() >= gameManager.getPvpTime() && !gameManager.isPvpTimeAlready()) {
            gameManager.setPvpTimeAlready(true);
            Bukkit.getWorlds().forEach(world -> world.setPVP(true));
            Bukkit.broadcastMessage("§cGrace period has ended.");
        }

        if (gameManager.getGameTime() >= (gameManager.getBorderTime() - 300) && !gameManager.isBorderTimeAlready()) {
            gameManager.setBorderTimeAlready(true);

            World world = UHC.getInstance().getWorldManager().getUhcWorld();
            world.setTime(0);
            world.setGameRuleValue("doDaylightCycle", "false");

            String[] timeBorder = UHC.getInstance().getConfig().getStringList("border-shrinks").get(this.borderShrinks).split(",");
            this.borderCounter = Integer.parseInt(timeBorder[0]);
            this.actualBorder = Integer.parseInt(timeBorder[1]);

            Bukkit.broadcastMessage("§cPermanent Day has enabled.");
        }

        if (gameManager.isDeathmatchRunning()) return;
        if (gameManager.isDeathmatchTimeAlready()) {
            Common.getCounter(gameManager.getGameTime(), gameManager.getDeathmatchTime(), "§fDeathmatch will start in §a<time>§f.");
            if (gameManager.getGameTime() >= gameManager.getDeathmatchTime() && !gameManager.isDeathmatchRunning()) {
                gameManager.setDeathmatchRunning(true);
                Bukkit.broadcastMessage("§cDeathmatch has begun");
                Bukkit.getPluginManager().callEvent(new DeathMatchStartEvent());
            }
            return;
        }

        if (gameManager.isBorderTimeAlready()) {
            int newCounter = (((gameManager.getGameTime() - gameManager.getBorderTime()) + 300) - this.currentTime);
            int borderCounter = this.borderCounter - newCounter;

            gameManager.setBorderCount(borderCounter);

            if (borderCounter > 0) {
                if (borderCounter == 10) UHC.getInstance().getWorldManager().shrinkBorder(this.actualBorder, 1);
                Common.getCounter(gameManager.getGameTime(), (gameManager.getGameTime() + borderCounter), "§7[§a§lBorder§7] §fThe border will shrink to §a" + this.actualBorder + " §fin §a<time>§f.");
            } else {
                if (this.borderShrinks == UHC.getInstance().getConfig().getStringList("border-shrinks").size()) {
                    gameManager.setDeathmatchTimeAlready(true);
                    gameManager.setDeathmatchTime((gameManager.getGameTime() + gameManager.getDeathmatchTime()));
                } else {
                    Bukkit.getPluginManager().callEvent(new BorderShrinkEvent(this.actualBorder, gameManager.getCurrentBorder()));
                    gameManager.setCurrentBorder(this.actualBorder);

                    this.borderShrinks++;
                    if (this.borderShrinks != UHC.getInstance().getConfig().getStringList("border-shrinks").size()) {
                        String[] timeBorder = UHC.getInstance().getConfig().getStringList("border-shrinks").get(this.borderShrinks).split(",");
                        this.currentTime = this.currentTime + this.borderCounter;
                        this.borderCounter = Integer.parseInt(timeBorder[0]);
                        this.actualBorder = Integer.parseInt(timeBorder[1]);
                    }
                }
            }
        }
    }
}
