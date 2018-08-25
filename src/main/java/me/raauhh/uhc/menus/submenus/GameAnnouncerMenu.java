package me.raauhh.uhc.menus.submenus;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.events.GameScheduleEvent;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.utils.ItemUtil;
import me.raauhh.uhc.utils.menu.type.ChestMenu;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class GameAnnouncerMenu extends ChestMenu<UHC> {

    private GameManager gameManager;
    private TimeSelectorMenu timeSelectorMenu;

    private Date date;
    private int minutes = 0;

    public GameAnnouncerMenu() {
        super("Game Announcer", 9);
        this.gameManager = UHC.getInstance().getGameManager();
        this.timeSelectorMenu = new TimeSelectorMenu();
    }

    public void open(Player player) {
        if (date == null) {
            date = new Date();
            date.setMinutes(date.getMinutes());
            date.setSeconds(0);
        }

        update(player);
        super.open(player);
    }

    public void update(Player player) {
        inventory.setItem(0, new ItemUtil(Material.WATCH).setName("§eMatch Time §7(§f" + new SimpleDateFormat("HH:mm").format(date) + "§7)").get());
        inventory.setItem(1, new ItemUtil(Material.PAPER).setName("§ePost Preview").get());

        inventory.setItem(4, new ItemUtil(Material.EMERALD).setName("§a§lAnnounce Game").get());
        inventory.setItem(8, new ItemUtil(Material.ARROW).setName("§bBack").get());
        player.updateInventory();
    }

    private String getTitle() {
        return "Game Announcer";
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

            switch (material) {
                case WATCH:
                    this.timeSelectorMenu.open(player);
                    break;
                case EMERALD:
                    if (minutes < 1) {
                        player.sendMessage("§cCouldn't announce the game because is too early");
                        break;
                    }

                    Bukkit.getPluginManager().callEvent(new GameScheduleEvent(date));
                    break;
                case ARROW:
                    UHC.getInstance().getUhcConfigMenu().open(player);
                    break;
            }
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }

    private class TimeSelectorMenu extends ChestMenu<UHC> {

        private GameManager gameManager;

        TimeSelectorMenu() {
            super("Match Time ┃ §7", 9);
            this.gameManager = UHC.getInstance().getGameManager();
        }

        public void open(Player player) {
            minutes = 0;
            update(player);
            super.open(player);
        }

        public void update(Player player) {
            sendTittle(player);

            inventory.setItem(0, new ItemUtil(Material.INK_SACK, 1).setName("§c-5").get());
            inventory.setItem(1, new ItemUtil(Material.INK_SACK, 1).setName("§c-1").get());
            inventory.setItem(3, new ItemUtil(Material.WATCH).setName("§eMatch Time §7(§f" + new SimpleDateFormat("HH:mm").format(date) + "§7)").get());
            inventory.setItem(5, new ItemUtil(Material.INK_SACK, 10).setName("§a+1").get());
            inventory.setItem(6, new ItemUtil(Material.INK_SACK, 10).setName("§a+5").get());
            inventory.setItem(8, new ItemUtil(Material.ARROW).setName("§bBack").get());
            player.updateInventory();
        }

        private String getTitle() {
            date = new Date();
            date.setMinutes(date.getMinutes() + minutes);
            date.setSeconds(0);
            return "Match Time ┃ §7" + new SimpleDateFormat("HH:mm").format(date);
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
                                minutes = minutes + 1;
                                break;
                            case "+5":
                                minutes = minutes + 5;
                                break;
                            case "-1":
                                if ((minutes - 1) > 0) minutes = minutes - 1;
                                break;
                            case "-5":
                                if ((minutes - 5) > 0) minutes = minutes - 5;
                                break;
                        }
                        update(player);
                        break;
                    case ARROW:
                        UHC.getInstance().getUhcConfigMenu().getGameAnnouncerMenu().open(player);
                        break;
                }
            } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR)
                event.setCancelled(true);
        }
    }
}
