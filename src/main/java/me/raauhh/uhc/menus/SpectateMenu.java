package me.raauhh.uhc.menus;

import lombok.Getter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.utils.ItemUtil;
import me.raauhh.uhc.utils.menu.Menu;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SpectateMenu implements Menu {

    private int page;
    @Getter
    private final Inventory inventory;

    public SpectateMenu() {
        page = getTotalPages();
        inventory = Bukkit.createInventory(this, 9 * 2, getTitle());
    }

    public void open(Player player) {
        update(player);
        player.openInventory(inventory);
    }

    public void update(Player player) {
        sendTittle(player);

        inventory.clear();
        inventory.setItem(0, new ItemUtil(Material.CARPET, page == 1 ? 7 : 14)
                .setName((page == 1 ? ChatColor.GRAY : ChatColor.RED) + "Previous Page")
                .get());
        inventory.setItem(8, new ItemUtil(Material.CARPET, page + 1 > getTotalPages() ? 7 : 13)
                .setName((page + 1 > getTotalPages() ? ChatColor.GRAY : ChatColor.GREEN) + "Next Page")
                .get());

        int slot = 9;
        int index = page * 36 - 36;
        while (slot < inventory.getSize() && UHC.getInstance().getGameManager().getAlivePlayers().size() > index) {
            UHCPlayer uhcPlayer = UHC.getInstance().getGameManager().getAlivePlayers().get(index);
            inventory.setItem(slot++, new ItemUtil(Material.SKULL_ITEM, 3).setName("§e" + uhcPlayer.getName()).get());
            index++;
        }

        player.updateInventory();
    }

    private String getTitle() {
        return "Spectate Menu ┃ " + page + '/' + getTotalPages();
    }

    private int getTotalPages() {
        return UHC.getInstance().getGameManager().getAlivePlayers().size() / 36 + 1;
    }

    private void sendTittle(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        int windowId = entityPlayer.activeContainer.windowId;
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(windowId, 0, (getTitle().length() > 32) ? getTitle().substring(0, 32) : getTitle(), player.getOpenInventory().getTopInventory().getSize(), true);
        entityPlayer.playerConnection.sendPacket(packet);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        if (!topInventory.equals(inventory)) return;
        if (topInventory.equals(clickedInventory)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            Material material = event.getCurrentItem().getType();
            String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

            switch (material) {
                case CARPET:
                    switch (itemName) {
                        case "Previous Page":
                            if (page == 1) return;
                            page--;
                            update(player);
                            break;
                        case "Next Page":
                            if (page + 1 > getTotalPages()) return;
                            page++;
                            update(player);
                            break;
                    }
                    update(player);
                    break;
                case SKULL_ITEM:
                    player.sendMessage("teleport to " + itemName);
                    break;
            }

        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }
}
