package me.raauhh.uhc.listeners.gamemodes;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.events.GameStartEvent;
import me.raauhh.uhc.manager.gamemode.Gamemode;
import me.raauhh.uhc.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildUHC implements Gamemode {

    @EventHandler
    public void onGameStartEvent(GameStartEvent event) {
        UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(uhcPlayer -> {
            if(uhcPlayer.getPlayer() != null) giveKit(uhcPlayer.getPlayer());
        });
    }

    public static void giveKit(Player player){
        player.getInventory().addItem(new ItemUtil(Material.DIAMOND_SWORD).addEnchants(Collections.singletonList("DAMAGE_ALL, 3")).get());
        player.getInventory().addItem(new ItemUtil(Material.FISHING_ROD).get());
        player.getInventory().addItem(new ItemUtil(Material.BOW).addEnchants(Collections.singletonList("ARROW_DAMAGE, 3")).get());
        player.getInventory().addItem(new ItemUtil(Material.COBBLESTONE, 64, 0).get());

        player.getInventory().addItem(new ItemUtil(Material.WATER_BUCKET, 1, 0).get());
        player.getInventory().addItem(new ItemUtil(Material.LAVA_BUCKET, 1, 0).get());
        player.getInventory().addItem(new ItemUtil(Material.GOLDEN_APPLE, 6, 0).get());

        player.getInventory().addItem(UHC.getInstance().getGameManager().getGoldenHead());
        player.getInventory().addItem(UHC.getInstance().getGameManager().getGoldenHead());
        player.getInventory().addItem(UHC.getInstance().getGameManager().getGoldenHead());

        player.getInventory().addItem(new ItemUtil(Material.WOOD, 64, 0).get());
        player.getInventory().addItem(new ItemUtil(Material.LAVA_BUCKET, 1, 0).get());
        player.getInventory().addItem(new ItemUtil(Material.WATER_BUCKET, 1, 0).get());
        player.getInventory().addItem(new ItemUtil(Material.ARROW, 64, 0).get());

        player.getInventory().setHelmet(new ItemUtil(Material.DIAMOND_HELMET).addEnchants(Collections.singletonList("PROTECTION_ENVIRONMENTAL, 2")).get());
        player.getInventory().setChestplate(new ItemUtil(Material.DIAMOND_CHESTPLATE).addEnchants(Collections.singletonList("PROTECTION_ENVIRONMENTAL, 2")).get());
        player.getInventory().setLeggings(new ItemUtil(Material.DIAMOND_LEGGINGS).addEnchants(Collections.singletonList("PROTECTION_ENVIRONMENTAL, 2")).get());
        player.getInventory().setBoots(new ItemUtil(Material.DIAMOND_BOOTS).addEnchants(Collections.singletonList("PROTECTION_ENVIRONMENTAL, 2")).get());
        player.updateInventory();
    }

    public String getName() {
        return "BuildUHC";
    }

    public ItemStack getItem() {
        List<String> lore = new ArrayList<>();
        return new ItemUtil(Material.LAVA_BUCKET).setName("§aBuildUHC").setLore(lore).get();
    }

    public void deactivate() {
        GameStartEvent.getHandlerList().unregister(this);
    }
}
