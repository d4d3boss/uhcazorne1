package me.raauhh.uhc.utils;

import me.raauhh.uhc.UHC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LocationUtil {

    public String toString(Location loc) {
        String world = loc.getWorld().getName();

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        return world + "|" + x + "|" + y + "|" + z + "|" + yaw + "|" + pitch;
    }

    public Location fromString(String str) {

        String[] array = str.split("|");
        World world = Bukkit.getWorld(array[0]);

        double x = Double.parseDouble(array[1]);
        double y = Double.parseDouble(array[2]);
        double z = Double.parseDouble(array[3]);

        float yaw = Float.parseFloat(array[4]);
        float pitch = Float.parseFloat(array[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public List<Location> getLocations(Location pos1, Location pos2) {

        List<Location> locations = new ArrayList<>();
        World world = pos1.getWorld();

        int x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());

        int y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());

        int z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    locations.add(new Location(world, x, y, z));
                }
            }
        }
        return locations;
    }

    public boolean isBetweenTwoLocations(Location target, Location pos1, Location pos2) {
        int x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());

        int y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());

        int z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        return ((target.getBlockX() >= x1)
                && (target.getBlockX() <= x2))
                && ((target.getBlockY() >= y1)
                && (target.getBlockY() <= y2))
                && ((target.getBlockZ() >= z1)
                && (target.getBlockZ() <= z2));
    }

    public void saveLocation(String path, Location loc) {
        UHC.getInstance().getConfig().set(path, toString(loc));
        UHC.getInstance().saveConfig();
    }

    public Location randomLocation(World world, int borderSize) {
        int sizepositivex = borderSize - 5;
        int sizenegativex = borderSize + 5;
        int sizepositivez = borderSize - 5;
        int sizenegativez = borderSize + 5;
        int randomNumber = getRandom(sizenegativez, sizepositivez);

        int x = getRandom(sizenegativex, sizepositivex);
        int z = -randomNumber;
        int y = world.getHighestBlockYAt(x, z);

        return new Location(world, x, y, z);
    }

    public int getRandom(int low, int up) {
        return new Random().nextInt((up + 1 + low) - low);
    }

}
