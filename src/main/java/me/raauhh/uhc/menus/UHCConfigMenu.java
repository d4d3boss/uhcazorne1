package me.raauhh.uhc.menus;

import lombok.Getter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.menus.submenus.*;
import me.raauhh.uhc.utils.Common;
import me.raauhh.uhc.utils.ItemUtil;
import me.raauhh.uhc.utils.menu.type.ChestMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class UHCConfigMenu extends ChestMenu<UHC> {

    private GameManager gameManager;
    private HealTimeMenu healTimeMenu;
    private GracePeriodMenu gracePeriodMenu;
    private BorderTimeMenu borderTimeMenu;
    private AppleRateMenu appleRateMenu;
    private ServerSlotsMenu serverSlotsMenu;
    private StarterFoodMenu starterFoodMenu;
    private GameAnnouncerMenu gameAnnouncerMenu;

    public UHCConfigMenu() {
        super("UHC Configurator", 9 * 4);

        this.gameManager = UHC.getInstance().getGameManager();
        this.healTimeMenu = new HealTimeMenu();
        this.gracePeriodMenu = new GracePeriodMenu();
        this.borderTimeMenu = new BorderTimeMenu();
        this.appleRateMenu = new AppleRateMenu();
        this.serverSlotsMenu = new ServerSlotsMenu();
        this.starterFoodMenu = new StarterFoodMenu();
        this.gameAnnouncerMenu = new GameAnnouncerMenu();
    }

    public void open(Player player) {
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemUtil(Material.STAINED_GLASS_PANE, 14).get());
            inventory.setItem(i + 27, new ItemUtil(Material.STAINED_GLASS_PANE, 14).get());
        }

        inventory.setItem(9, new ItemUtil(Material.POTION, 8261).setName("§eFinal Heal §7(§f" + Common.calculate(gameManager.getHealTime()) + "§7)").get());
        inventory.setItem(10, new ItemUtil(Material.DIAMOND_SWORD).setName("§eGrace Period §7(§f" + Common.calculate(gameManager.getPvpTime()) + "§7)").get());
        inventory.setItem(11, new ItemUtil(Material.BEDROCK).setName("§eBorder Time §7(§f" + Common.calculate(gameManager.getBorderTime()) + "§7)").get());

        inventory.setItem(13, new ItemUtil(Material.APPLE).setName("§eApple Rate §7(§f" + gameManager.getAppleRatePercent() + "%§7)").get());
        inventory.setItem(14, new ItemUtil(Material.SKULL_ITEM, 3).setName("§eServer Slots §7(§f" + gameManager.getSlots() + "§7)").get());
        inventory.setItem(15, new ItemUtil(Material.COOKED_BEEF).setName("§eStarter Food §7(§f" + gameManager.getStarterFood() + "§7)").get());

        if (gameManager.isScheduled())
            inventory.setItem(17, new ItemUtil(Material.STAINED_GLASS_PANE, 4).setName("§cAnnounce Game").get());
        else inventory.setItem(17, new ItemUtil(Material.EMERALD).setName("§a§lAnnounce Game").get());

        inventory.setItem(18, new ItemUtil(Material.BOOK).setName("§eManage Scenarios").get());
        inventory.setItem(19, new ItemUtil(Material.NETHER_STALK).setName("§eManage Potions").get());
        inventory.setItem(20, new ItemUtil(Material.GOLD_CHESTPLATE).setName("§eManage Teams").get());

        inventory.setItem(22, new ItemUtil(Material.DIAMOND).setName("§eStats §7(§f" + gameManager.isStats() + "§7)").get());
        inventory.setItem(23, new ItemUtil(Material.NETHER_BRICK).setName("§eNether §7(§f" + gameManager.isNether() + "§7)").get());
        inventory.setItem(24, new ItemUtil(Material.BEACON).setName("§eCustom Event §7(§f" + gameManager.isEvent() + "§7)").get());

        inventory.setItem(26, new ItemUtil(Material.CHEST).setName("§c§lStart Inventory").get());

        if (player.getOpenInventory().getTopInventory().getTitle().equalsIgnoreCase(inventory.getTitle())) {
            player.getOpenInventory().getTopInventory().setContents(inventory.getContents());
            player.updateInventory();
        } else {
            super.open(player);
        }
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
                case POTION:
                    this.healTimeMenu.open(player);
                    break;
                case DIAMOND_SWORD:
                    this.gracePeriodMenu.open(player);
                    break;
                case BEDROCK:
                    this.borderTimeMenu.open(player);
                    break;
                case APPLE:
                    this.appleRateMenu.open(player);
                    break;
                case SKULL_ITEM:
                    this.serverSlotsMenu.open(player);
                    break;
                case COOKED_BEEF:
                    this.starterFoodMenu.open(player);
                    break;
                case EMERALD:
                    this.gameAnnouncerMenu.open(player);
                    break;
                case BOOK:
                    // TODO: open scenarios combatlogger menu
                    break;
                case NETHER_STALK:
                    // TODO: open potion combatlogger menu
                    break;
                case GOLD_CHESTPLATE:
                    // TODO: open team combatlogger menu
                    break;
                case DIAMOND:
                    if (gameManager.isStats()) gameManager.setStats(false);
                    else gameManager.setStats(true);
                    open(player);
                    break;
                case NETHER_BRICK:
                    if (gameManager.isNether()) gameManager.setNether(false);
                    else gameManager.setNether(true);
                    open(player);
                    break;
                case BEACON:
                    if (gameManager.isEvent()) gameManager.setEvent(false);
                    else gameManager.setEvent(true);
                    open(player);
                    break;
                case CHEST:
                    // TODO: open start inventory menu
                    break;
            }
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }
}
