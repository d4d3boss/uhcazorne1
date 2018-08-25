package me.raauhh.uhc.manager;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.UHC;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.google.gson.JsonObject;
import me.raauhh.uhc.utils.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class WorldManager {

    private int uhcWorldSize, uhcWorldNetherSize, uhcWorldDeathmatchSize;
    private String uhcWorldName, uhcWorldNetherName, uhcWorldDeathmatchName;
    private World world, uhcWorld, uhcWorldNether, uhcWorldDeathMatch;

    private Set<Integer> materialBypass = new HashSet<>(Arrays.asList(17, 162, 18, 161, 0, 81, 175, 31, 37, 38, 175, 39, 40));

    public WorldManager() {
        this.uhcWorldSize = UHC.getInstance().getConfig().getInt("worlds.overworld.size");
        this.uhcWorldNetherSize = UHC.getInstance().getConfig().getInt("worlds.nether.size");
        this.uhcWorldDeathmatchSize = UHC.getInstance().getConfig().getInt("worlds.deathmatch.size");

        this.uhcWorldName = UHC.getInstance().getConfig().getString("worlds.overworld.name");
        this.uhcWorldNetherName = UHC.getInstance().getConfig().getString("worlds.nether.name");
        this.uhcWorldDeathmatchName = UHC.getInstance().getConfig().getString("worlds.deathmatch.name");

        this.world = Bukkit.getWorlds().get(0);
        this.world.setTime(0L);
        this.world.setGameRuleValue("doDaylightCycle", "false");
        this.world.setWeatherDuration(0);
        this.world.setMonsterSpawnLimit(0);
        this.world.setAnimalSpawnLimit(0);
        this.world.setPVP(false);

        checkWorlds();
        Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
            String path = UHC.getInstance().getServer().getWorldContainer().getAbsolutePath().replace(".", "");
            File serverFile = new File(path + uhcWorldName);
            if (!serverFile.exists()) {
                this.uhcWorld = Bukkit.createWorld(new WorldCreator(uhcWorldName).environment(World.Environment.NORMAL).type(WorldType.NORMAL));
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb shape square");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + this.uhcWorldName + " set " + this.uhcWorldSize + " " + this.uhcWorldSize + " 0 0");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + this.uhcWorldName + " fill 1000");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm");
                }, 20L);
                return;
            }

            Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
                this.uhcWorld = new WorldCreator(uhcWorldName).createWorld();
                this.uhcWorldNether = new WorldCreator(uhcWorldNetherName).environment(World.Environment.NETHER).createWorld();
                //this.uhcWorldDeathMatch = new WorldCreator(uhcWorldDeathMatchName).createWorld();

                for (WorldServer worldServer : MinecraftServer.getServer().worlds) {
                    if (worldServer == null) continue;
                    worldServer.setFrozenMobs(true);
                }

                Bukkit.getWorlds().forEach(worlds -> {
                    worlds.setPVP(false);
                    worlds.setDifficulty(Difficulty.HARD);
                });

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb shape square");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + this.uhcWorldName + " set " + this.uhcWorldSize + " " + this.uhcWorldSize + " 0 0");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + this.uhcWorldNetherName + " set " + this.uhcWorldNetherSize + " " + this.uhcWorldNetherSize + " 0 0");

                UHC.getInstance().getScoreboardTask().runTaskTimerAsynchronously(UHC.getInstance(), 0L, 5L);
                UHC.getInstance().getGameManager().setCurrentBorder(uhcWorldSize);
                UHC.getInstance().getGameManager().setGameState(GameManager.State.LOBBY);

                if (UHC.getInstance().isUsingRedis()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("server", Bukkit.getServerName());
                    UHC.getInstance().getRedisMessagingHandler().sendMessage("uhc:generation", object.toString());
                }
                makeScatterSpawns();
                UHC.getInstance().getGameManager().setGameState(GameManager.State.LOBBY);
            }, 20L);
        }, 10L);
    }

    private void checkWorlds() {
        try {
            String path = UHC.getInstance().getServer().getWorldContainer().getAbsolutePath().replace(".", "");
            File file = new File(path + "/" + uhcWorldName + "/used");
            if (file.exists()) {
                Runtime.getRuntime().exec("rm -rf " + path + "/" + uhcWorldName);
                Runtime.getRuntime().exec("rm -rf " + path + "/" + uhcWorldNetherName);
            }
        } catch (IOException e) {
            Bukkit.shutdown();
        }
    }

    private void makeScatterSpawns() {
        for (int i = 0; i < 400; ++i) addSpawn();
    }

    private void addSpawn() {
        Location location = new LocationUtil().randomLocation(this.uhcWorld, this.uhcWorldSize);
        location.getWorld().loadChunk(location.getChunk());
        location.getWorld().loadChunk(location.add(50.0, 0.0, 50.0).getChunk());
        location.getWorld().loadChunk(location.add(-50.0, 0.0, 50.0).getChunk());
        location.getWorld().loadChunk(location.add(-50.0, 0.0, -50.0).getChunk());
        location.getWorld().loadChunk(location.add(50.0, 0.0, -50.0).getChunk());
        location.getWorld().loadChunk(location.add(50.0, 0.0, 0.0).getChunk());
        location.getWorld().loadChunk(location.add(-50.0, 0.0, 0.0).getChunk());
        location.getWorld().loadChunk(location.add(0.0, 0.0, -50.0).getChunk());
        location.getWorld().loadChunk(location.add(0.0, 0.0, 50.0).getChunk());
        UHC.getInstance().getGameManager().getScatterLocations().add(location);
    }

    public void setUsed(boolean used) {
        String path = UHC.getInstance().getServer().getWorldContainer().getAbsolutePath().replace(".", "");
        File file = new File(path + "/" + uhcWorldName + "/used");
        if (used) file.mkdirs();
        else file.delete();
    }

    private void figureBlock(int x, int z) {
        Block block = uhcWorld.getHighestBlockAt(x, z);
        Block below = block.getRelative(BlockFace.DOWN);
        while (materialBypass.contains(below.getTypeId()) && below.getY() > 5)
            below = below.getRelative(BlockFace.DOWN);
        below.getRelative(BlockFace.UP).setType(Material.BEDROCK);
        below.getRelative(BlockFace.UP).getState().update(false);
    }

    public void shrinkBorder(int radius, int high) {
        for (int i = 0; i < high; i++)
            Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> addBorder(radius), i);
    }

    private void addBorder(int radius) {
        new BukkitRunnable() {

            int count = -radius - 1;
            int maxCounter;
            int x;

            boolean phase1 = false;
            boolean phase2 = false;
            boolean phase3 = false;

            @Override
            public void run() {
                if (!phase1) {
                    maxCounter = count + 500;
                    x = -radius - 1;
                    for (int z = count; z <= radius && count <= maxCounter; z++, count++) figureBlock(x, z);
                    if (count >= radius) {
                        count = -radius - 1;
                        phase1 = true;
                    }
                    return;
                }
                if (!phase2) {
                    maxCounter = count + 500;
                    x = radius;
                    for (int z = count; z <= radius && count <= maxCounter; z++, count++) figureBlock(x, z);
                    if (count >= radius) {
                        count = -radius - 1;
                        phase2 = true;
                    }
                    return;
                }
                if (!phase3) {
                    maxCounter = count + 500;
                    int z = -radius - 1;
                    for (int x = count; x <= radius && count <= maxCounter; x++, count++) {
                        if (x == radius || x == -radius - 1) continue;
                        figureBlock(x, z);
                    }
                    if (count >= radius) {
                        count = -radius - 1;
                        phase3 = true;
                    }
                    return;
                }

                maxCounter = count + 500;
                int z = radius;
                for (int x = count; x <= radius && count <= maxCounter; x++, count++) {
                    if (x == radius || x == -radius - 1) continue;
                    figureBlock(x, z);
                }
                if (count >= radius) this.cancel();
            }
        }.runTaskTimer(UHC.getInstance(), 0, 5);
    }
}
