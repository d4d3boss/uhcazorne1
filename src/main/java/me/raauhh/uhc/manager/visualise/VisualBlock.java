package me.raauhh.uhc.manager.visualise;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@Getter
@AllArgsConstructor
public class VisualBlock {

    private final VisualType visualType;

    private final VisualBlockData blockData;
    private final Location location;
}