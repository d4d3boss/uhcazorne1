package me.raauhh.uhc.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InventoryUtil {

    public static ItemStack[] deepClone(ItemStack[] origin) {
        Preconditions.checkNotNull((Object) origin, "Origin cannot be null");
        ItemStack[] cloned = new ItemStack[origin.length];
        for (int i = 0; i < origin.length; ++i) {
            ItemStack next = origin[i];
            cloned[i] = next == null ? null : next.clone();
        }
        return cloned;
    }

    public static int getSafestInventorySize(int initialSize) {
        return (initialSize + 8) / 9 * 9;
    }

    public static int repairItem(ItemStack item) {
        if (item == null) {
            return 0;
        }
        Material material = Material.getMaterial(item.getTypeId());
        if (material.isBlock() || material.getMaxDurability() < 1) {
            return 0;
        }
        if (item.getDurability() <= 0) {
            return 0;
        }
        item.setDurability((short) 0);
        return 1;
    }

    public static void removeItem(Inventory inventory, Material type, short data, int quantity) {
        ItemStack[] contents = inventory.getContents();
        boolean compareDamage = type.getMaxDurability() == 0;
        block0:
        for (int i = quantity; i > 0; --i) {
            for (ItemStack content : contents) {
                if (content == null || content.getType() != type || compareDamage && content.getData().getData() != data)
                    continue;
                if (content.getAmount() <= 1) {
                    inventory.removeItem(content);
                    continue block0;
                }
                content.setAmount(content.getAmount() - 1);
                continue block0;
            }
        }
    }

    public static int countAmount(Inventory inventory, Material type, short data) {
        ItemStack[] contents = inventory.getContents();
        boolean compareDamage = type.getMaxDurability() == 0;
        int counter = 0;
        for (ItemStack item : contents) {
            if (item == null || item.getType() != type || compareDamage && item.getData().getData() != data) continue;
            counter += item.getAmount();
        }
        return counter;
    }

    public static boolean isEmpty(Inventory inventory) {
        return isEmpty(inventory, true);
    }

    public static boolean isEmpty(Inventory inventory, boolean checkArmour) {
        ItemStack[] contents;
        boolean result = true;
        for (ItemStack content : contents = inventory.getContents()) {
            if (content == null || content.getType() == Material.AIR) continue;
            result = false;
            break;
        }
        if (!result) return false;
        if (checkArmour && inventory instanceof PlayerInventory) {
            for (ItemStack content : contents = ((PlayerInventory) inventory).getArmorContents()) {
                if (content == null || content.getType() == Material.AIR) continue;
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean clickedTopInventory(InventoryDragEvent event) {
        InventoryView view = event.getView();
        Inventory topInventory = view.getTopInventory();
        if (topInventory == null) {
            return false;
        }
        boolean result = false;
        int size = topInventory.getSize();
        for (Integer entry : event.getNewItems().keySet()) {
            if (entry >= size) continue;
            result = true;
            break;
        }
        return result;
    }
}
