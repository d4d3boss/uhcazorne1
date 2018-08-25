package me.raauhh.uhc.manager.visualise;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public enum VisualType {

    BORDER() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.STAINED_GLASS, DyeColor.RED.getData());
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    };

    abstract BlockFiller blockFiller();
}