package me.raauhh.uhc.listeners.gamemodes;

import me.raauhh.uhc.manager.gamemode.Gamemode;
import me.raauhh.uhc.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CutClean implements Gamemode {

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        switch (event.getEntity().getType()) {
            case COW:
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(Material.COOKED_BEEF, 3));
                event.getDrops().add(new ItemStack(Material.LEATHER));
                break;
            case CHICKEN:
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(Material.COOKED_CHICKEN, 3));
                event.getDrops().add(new ItemStack(Material.FEATHER));
                break;
            case PIG:
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(Material.GRILLED_PORK, 3));
                break;
            case HORSE:
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(Material.LEATHER));
                break;
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        switch (block.getType()) {
            case GOLD_ORE:
                if(player.getItemInHand() == null) return;
                if(player.getItemInHand().getType() == Material.DIAMOND_PICKAXE
                        || player.getItemInHand().getType() == Material.IRON_PICKAXE) {
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                    block.getState().update();
                    block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(1);
                    block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
                }
                break;
            case IRON_ORE:
                if(player.getItemInHand() == null) return;
                if(player.getItemInHand().getType() == Material.DIAMOND_PICKAXE
                        || player.getItemInHand().getType() == Material.IRON_PICKAXE) {
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                    block.getState().update();
                    block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(1);
                    block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
                }
                break;
            case GRAVEL:
                event.setCancelled(true);
                block.setType(Material.AIR);
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.FLINT));
                break;
        }
    }

    public String getName() {
        return "CutClean";
    }

    public ItemStack getItem() {
        List<String> lore = new ArrayList<>();
        lore.add("§f- Ores are pre-smelted");
        lore.add("§f- Food is pre-cooked");
        lore.add("§f- Flint/Leather/Feathers drop rates are 100%");
        return new ItemUtil(Material.IRON_INGOT).setName("§aCutClean").setLore(lore).get();
    }

    public void deactivate() {
        EntityDeathEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
    }
}
