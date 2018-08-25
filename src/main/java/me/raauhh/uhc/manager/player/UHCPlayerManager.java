package me.raauhh.uhc.manager.player;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.listeners.gamemodes.BuildUHC;
import me.raauhh.uhc.manager.combatlogger.CombatLoggerManager;
import me.raauhh.uhc.utils.Common;
import me.raauhh.uhc.utils.ItemUtil;
import me.raauhh.uhc.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.*;

public class UHCPlayerManager {

    private Map<UUID, UHCPlayer> uhcPlayers = new HashMap<>();

    public void createPlayer(UUID uuid) {
        this.uhcPlayers.put(uuid, new UHCPlayer(uuid));
    }

    public boolean existsPlayer(UUID uuid) {
        return this.uhcPlayers.containsKey(uuid);
    }

    public Map<UUID, UHCPlayer> getPlayers() {
        return this.uhcPlayers;
    }

    public UHCPlayer getPlayer(UUID uuid) {
        return this.uhcPlayers.get(uuid);
    }

    public UHCPlayer getPlayer(String name) {
        for (UHCPlayer uhcPlayer : uhcPlayers.values()) {
            if (uhcPlayer.getName() != null && uhcPlayer.getName().equals(name)) return uhcPlayer;
        }
        return null;
    }

    public int getAlivePlayers() {
        List<UUID> alivePlayers = new ArrayList<>();
        UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(players -> {
            CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
            if (players.getPlayerState() == UHCPlayer.State.PLAYING || combatLoggerManager.exists(players.getUuid())) {
                alivePlayers.add(players.getUuid());
            }
        });
        return alivePlayers.size();
    }

    public void prepareSpecator(Player player) {
        getPlayer(player.getUniqueId()).setPlayerState(UHCPlayer.State.SPEC);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());

        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);
        player.setCanPickupItems(false);
        player.spigot().setCollidesWithEntities(false);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setItemOnCursor(null);

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
            player.getInventory().setItem(0, new ItemUtil(Material.ITEM_FRAME).setName("§eSpectate Menu").get());
            player.getInventory().setItem(1, new ItemUtil(Material.DIAMOND).setName("§dRandom Teleport").get());
            player.getInventory().setItem(8, new ItemUtil(Material.COMPASS).setName("§bNavigation Compass").get());
            player.updateInventory();
        }, 2);

        Common.getOnlinePlayers().forEach(target -> {
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(target.getUniqueId());
            if (uhcPlayer.getPlayerState() == UHCPlayer.State.SPEC) player.showPlayer(target);
            else target.hidePlayer(player);
        });
    }

    public void preparePlayerGame(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        if (player.isDead()) player.spigot().respawn();

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());

        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setItemOnCursor(null);

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        if (player.getOpenInventory() != null && player.getOpenInventory().getType() == InventoryType.CRAFTING) player.getOpenInventory().getTopInventory().clear();
        player.getInventory().addItem(new ItemUtil(Material.COOKED_BEEF, UHC.getInstance().getGameManager().getStarterFood(), 0).get());
    }

    public void preparePlayerLobby(Player player) {
        if (player.isDead()) player.spigot().respawn();

        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.setItemOnCursor(null);
        player.updateInventory();

        // TODO: remove later
        player.teleport(new LocationUtil().randomLocation(UHC.getInstance().getWorldManager().getWorld(), 120));
    }

}

