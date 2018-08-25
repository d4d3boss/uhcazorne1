package me.raauhh.uhc.utils;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.player.UHCPlayer;
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Common {

    private static HashMap<Player, Integer> vehicles = new HashMap<>();

    public static void addVehicle(Player player) {
        Location location = player.getLocation();
        WorldServer worldServer = ((CraftWorld) player.getLocation().getWorld()).getHandle();

        EntityBat bat = new EntityBat(worldServer);
        bat.setLocation(location.getX() + 0.5, location.getY() + 2.0, location.getZ() + 0.5, 0.0f, 0.0f);
        bat.setHealth(bat.getMaxHealth());
        bat.setInvisible(true);
        bat.d(0);
        bat.setAsleep(true);
        bat.setAirTicks(10);
        bat.setSneaking(false);

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(bat);
        PacketPlayOutAttachEntity attach = new PacketPlayOutAttachEntity(0, ((CraftPlayer) player).getHandle(), bat);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(attach);

        vehicles.put(player, bat.getId());
    }

    public static void removeVehicle(Player player) {
        if (vehicles.get(player) != null) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(vehicles.get(player));
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            vehicles.put(player, null);
        }
    }

    public static void summonFakePlayer(Player player) {
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());
        Location location = player.getLocation();

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
        EntityPlayer npc = new EntityPlayer(server, world, new GameProfile(uhcPlayer.getUuid(), uhcPlayer.getName()), new PlayerInteractManager(world));
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        Common.getOnlinePlayers().forEach(players -> {
            PlayerConnection connection = ((CraftPlayer) players).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            connection.sendPacket(PacketPlayOutPlayerInfo.addPlayer(npc));
        });

        Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Common.getOnlinePlayers().forEach(players -> {
            PlayerConnection connection = ((CraftPlayer) players).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutEntityStatus(npc, (byte) 3));

            UHCPlayer targetUhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(players.getUniqueId());
            if (targetUhcPlayer.getPlayerState() == UHCPlayer.State.SPEC)
                connection.sendPacket(PacketPlayOutPlayerInfo.addPlayer(((CraftPlayer) player).getHandle()));
        }), 2);
    }

    public static void makeSound(Sound sound) {
        Common.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1.0F, 1.0F));
    }

    public static void getCounter(Integer currentTime, Integer toTime, String message) {
        getCounter(currentTime, toTime, null, message);
    }

    public static void getCounter(Integer currentTime, Integer toTime, Sound sound, String message) {
        int menos1min = toTime - 60, menos2min = toTime - 120, menos3min = toTime - 180, menos4min = toTime - 240, menos5min = toTime - 300, menos6min = toTime - 360, menos7min = toTime - 420, menos8min = toTime - 480, menos9min = toTime - 540, menos10min = toTime - 600;
        if (menos10min == currentTime || menos9min == currentTime || menos8min == currentTime || menos7min == currentTime || menos6min == currentTime || menos5min == currentTime || menos4min == currentTime || menos3min == currentTime || menos2min == currentTime || menos1min == currentTime || toTime - 30 == currentTime || toTime - 15 == currentTime || toTime - 10 == currentTime || toTime - 5 == currentTime || toTime - 4 == currentTime || toTime - 3 == currentTime || toTime - 2 == currentTime || toTime - 1 == currentTime) {
            if (sound != null) makeSound(sound);
            if (menos1min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "1 minute"));
            else if (menos2min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "2 minutes"));
            else if (menos3min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "3 minutes"));
            else if (menos4min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "4 minutes"));
            else if (menos5min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "5 minutes"));
            else if (menos6min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "6 minutes"));
            else if (menos7min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "7 minutes"));
            else if (menos8min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "8 minutes"));
            else if (menos9min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "9 minutes"));
            else if (menos10min == currentTime) Bukkit.broadcastMessage(message.replace("<time>", "10 minutes"));
            else if (toTime - currentTime < 2) Bukkit.broadcastMessage(message.replace("<time>", (toTime - currentTime) + " second"));
            else Bukkit.broadcastMessage(message.replace("<time>", (toTime - currentTime) + " seconds"));
        }
    }

    public static String getMemory() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1048576L + "/" + runtime.totalMemory() / 1048576L;
    }

    public static String getTPS() {
        String name1 = Bukkit.getServer().getClass().getPackage().getName();
        String version = name1.substring(name1.lastIndexOf('.') + 1);

        DecimalFormat format = new DecimalFormat("##.##");
        Object si;
        Field tpsField;

        double[] tps = new double[0];
        try {
            Class<?> mcsclass = Class.forName("net.minecraft.server." + version + ".MinecraftServer");
            si = mcsclass.getMethod("getServer").invoke(null);
            tpsField = si.getClass().getField("recentTps");
            tps = ((double[]) Objects.requireNonNull(tpsField).get(si));
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return format.format(Objects.requireNonNull(tps)[0]);
    }

    public static double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
    }

    public static ChatColor randomChatColor() {
        ChatColor[] chatColors = new ChatColor[]{
                ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.GOLD,
                ChatColor.DARK_GRAY, ChatColor.DARK_RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.RED,
                ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE, ChatColor.AQUA,
        };

        Random random = new Random();
        return chatColors[random.nextInt(chatColors.length)];
    }

    public static String simpleCalculate(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(day) - TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.DAYS.toSeconds(day) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minute);
        return (minute > 0 ? minute + "m" : second + "s");
    }

    public static String fullCalculate(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(day) - TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.DAYS.toSeconds(day) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minute);

        String toReturn = day + " days, " + hours + " hours, " + minute + " minutes and " + second + " seconds";

        if (minute == 0) toReturn = second + " seconds";
        else if (hours == 0) toReturn = minute + " minutes and " + second + " seconds";
        else if (day == 0) toReturn = hours + " hours, " + minute + " minutes and " + second + " seconds";
        return toReturn;
    }


    public static String calculate(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(day) - TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.DAYS.toSeconds(day) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minute);

        String hour_text = String.valueOf(hours), minute_text = String.valueOf(minute), second_text = String.valueOf(second);

        if (hours < 10) hour_text = "0" + hour_text;
        if (minute < 10) minute_text = "0" + minute_text;
        if (second < 10) second_text = "0" + second_text;
        return (hours == 0 ? minute_text + ":" + second_text : hour_text + ":" + minute_text + ":" + second_text);
    }

    public static String calculateNoShort(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(day) - TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.DAYS.toSeconds(day) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minute);

        String hour_text = String.valueOf(hours), minute_text = String.valueOf(minute), second_text = String.valueOf(second);

        if (hours < 10) hour_text = "0" + hour_text;
        if (minute < 10) minute_text = "0" + minute_text;
        if (second < 10) second_text = "0" + second_text;
        return hour_text + ":" + minute_text + ":" + second_text;
    }

    public static void placeFence(Player player, Location location) {
        Block block = location.getBlock();
        block.setType(Material.FENCE);
        block.getState().update(false);

        placeHead(player, location);
    }

    private static void placeHead(Player player, Location location) {
        Block block = location.add(0, 1, 0).getBlock();
        block.setType(Material.SKULL);
        block.setData((byte) 1);

        Skull skull = (Skull) block.getState();
        skull.setSkullType(SkullType.PLAYER);
        skull.setOwner(player.getName());
        skull.update();
    }

    public static int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    public static List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) players.add(player);
        return players;
    }
}
