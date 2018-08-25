package me.raauhh.uhc.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    private ItemStack stack;

    public ItemUtil(Material material) {
        stack = new ItemStack(material);
    }

    public ItemUtil(Material material, int damage) {
        stack = new ItemStack(material, 1, (short)damage);
    }

    public ItemUtil(Material material, int amount, int damage) {
        stack = new ItemStack(material, amount, (short)damage);
    }

    public ItemUtil setName(String name) {
        if(name != null) {
            name = ChatColor.translateAlternateColorCodes('&', name);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemUtil setLore(List<String> lore) {
        if(lore != null) {
            List<String> list = new ArrayList<>();
            lore.forEach(line -> list.add(ChatColor.translateAlternateColorCodes('&', line)));
            ItemMeta meta = stack.getItemMeta();
            meta.setLore(list);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemUtil addEnchants(List<String> enchants) {
        if(enchants != null) {
            enchants.forEach(enchant -> {
                String[] arr = enchant.replace(" ", "").split(",");
                Enchantment enchantment = Enchantment.getByName(arr[0]);
                Integer level = Integer.valueOf(arr[1]);
                stack.addUnsafeEnchantment(enchantment, level);
            });
        }
        return this;
    }

    public ItemStack get() {
        return stack;
    }

}
