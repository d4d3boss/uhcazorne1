package me.raauhh.uhc.menus.submenus;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.utils.ItemUtil;
import me.raauhh.uhc.utils.menu.type.ChestMenu;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutOpenWindow;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ServerSlotsMenu extends ChestMenu<UHC> {

    private GameManager gameManager;

    public ServerSlotsMenu() {
        super("Server Slots ┃ ", 9);
        this.gameManager = UHC.getInstance().getGameManager();
    }

    public void open(Player player) {
        update(player);
        super.open(player);
    }

    public void update(Player player) {
        sendTittle(player);
        inventory.setItem(0, new ItemUtil(Material.INK_SACK, 1).setName("§c-5").get());
        inventory.setItem(1, new ItemUtil(Material.INK_SACK, 1).setName("§c-1").get());
        inventory.setItem(3, new ItemUtil(Material.SKULL_ITEM, 3).setName("§eServer Slots §7(§f" + gameManager.getSlots() + "§7)").get());
        inventory.setItem(5, new ItemUtil(Material.INK_SACK, 10).setName("§a+1").get());
        inventory.setItem(6, new ItemUtil(Material.INK_SACK, 10).setName("§a+5").get());
        inventory.setItem(8, new ItemUtil(Material.ARROW).setName("§bBack").get());
        player.updateInventory();
    }

    private String getTitle() {
        return "Server Slots ┃ §7" +  gameManager.getSlots();
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
                case INK_SACK:
                    switch (itemName) {
                        case "+1":
                            gameManager.setSlots(gameManager.getSlots() + 1);
                            break;
                        case "+5":
                            gameManager.setSlots(gameManager.getSlots() + 5);
                            break;
                        case "-1":
                            if ((gameManager.getSlots() - 1) < 1) return;
                            gameManager.setSlots(gameManager.getSlots() - 1);
                            break;
                        case "-5":
                            if ((gameManager.getSlots() - 5) < 1) return;
                            gameManager.setSlots(gameManager.getSlots() - 5);
                            break;
                    }
                    update(player);
                    break;
                case ARROW:
                    UHC.getInstance().getUhcConfigMenu().open(player);
                    break;
            }
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }
}
