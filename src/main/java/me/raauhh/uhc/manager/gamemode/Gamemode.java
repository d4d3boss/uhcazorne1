package me.raauhh.uhc.manager.gamemode;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public interface Gamemode extends Listener {

    /**
     * |=========================================|
     *  Gamemodes inspired from Badlion UHC code
     *     Credits @Badlion & @BadlionUHC
     * |=========================================|
     */

    String getName();

    ItemStack getItem();

    void deactivate();
}