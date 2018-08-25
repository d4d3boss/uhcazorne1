package me.raauhh.uhc.listeners.gamemodes;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.gamemode.Gamemode;
import me.raauhh.uhc.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoClean implements Gamemode {

    private List<UUID> noCleanPlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = event.getEntity().getKiller();
        this.noCleanPlayers.add(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> this.noCleanPlayers.remove(player.getUniqueId()), 20 * 20);
    }


    public String getName() {
        return "No Clean";
    }

    public ItemStack getItem() {
        List<String> lore = new ArrayList<>();
        lore.add("§f- 20s of invincibility when kill");
        return new ItemUtil(Material.FEATHER).setName("§aNo Clan").setLore(lore).get();
    }

    public void deactivate() {
        PlayerDeathEvent.getHandlerList().unregister(this);
    }
}
