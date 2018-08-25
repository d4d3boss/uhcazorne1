package me.raauhh.uhc.menus;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.utils.menu.type.ChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GamemodesMenu extends ChestMenu<UHC> {

    public GamemodesMenu() {
        super("Match Gamemodes", 9);
    }

    public void open(Player player) {
        inventory.clear();
        UHC.getInstance().getGamemodeManager().getEnabledGamemodes().forEach(gamemode -> inventory.addItem(gamemode.getItem()));
        super.open(player);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        if (!topInventory.equals(inventory)) return;
        if (topInventory.equals(clickedInventory)) {
            event.setCancelled(true);
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }
}
