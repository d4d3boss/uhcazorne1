package me.raauhh.uhc.listeners;

import me.raauhh.uhc.UHC;
import me.raauhh.uhc.events.BorderShrinkEvent;
import me.raauhh.uhc.events.CombatLoggerDeathEvent;
import me.raauhh.uhc.manager.combatlogger.CombatLogger;
import me.raauhh.uhc.manager.combatlogger.CombatLoggerManager;
import me.raauhh.uhc.events.*;
import me.raauhh.uhc.manager.GameManager;
import me.raauhh.uhc.manager.WorldManager;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.manager.player.UHCPlayerManager;
import me.raauhh.uhc.manager.team.UHCTeam;
import me.raauhh.uhc.manager.team.UHCTeamManager;
import me.raauhh.uhc.utils.Common;
import me.raauhh.uhc.utils.ItemUtil;
import me.raauhh.uhc.utils.LocationUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameListener implements Listener {

    private int currentScatter;

    @EventHandler
    public void onGameSetUpEvent(GameSetUpEvent event) {
        LocationUtil locationUtil = new LocationUtil();
        GameManager gameManager = UHC.getInstance().getGameManager();

        UHCPlayerManager uhcPlayerManager = UHC.getInstance().getUhcPlayerManager();
        UHCTeamManager uhcTeamManager = UHC.getInstance().getUhcTeamManager();

        World world = UHC.getInstance().getWorldManager().getUhcWorld();
        world.setTime(0);
        int worldSize = UHC.getInstance().getWorldManager().getUhcWorldSize();

        gameManager.butcherMobs();
        gameManager.setGameState(GameManager.State.SCATTER);
        gameManager.setWhitelist(true);

        Common.getOnlinePlayers().forEach(player -> {
            UHCPlayer uhcPlayer = uhcPlayerManager.getPlayer(player.getUniqueId());
            if (uhcPlayer.getPlayerState() != null)
                player.teleport(new Location(world, 0, world.getHighestBlockYAt(0, 0) + 5, 0));
            else {
                if (gameManager.isTeams()) {
                    UHCTeam uhcTeam = uhcTeamManager.getAllTeams().get(player.getUniqueId());
                    if (uhcTeam == null) uhcTeamManager.createTeam(player);
                }
            }
        });

        List<UUID> scatterPlayers = new ArrayList<>();
        if (gameManager.isTeams()) {
            uhcTeamManager.getAllTeams().values().forEach(uhcTeam -> {
                Location location = gameManager.getScatterLocations().get(currentScatter);
                uhcTeam.getPlayers().forEach(uhcTeamPlayer -> {
                    UHCPlayer uhcPlayer = uhcPlayerManager.getPlayer(uhcTeamPlayer);
                    if (uhcPlayer.getPlayer() != null && !scatterPlayers.contains(uhcPlayer.getUuid())) {
                        uhcPlayer.setScatterLocation(location);
                        scatterPlayers.add(uhcPlayer.getUuid());
                        currentScatter++;
                    }
                });
            });
        } else {
            uhcPlayerManager.getPlayers().values().forEach(uhcPlayer -> {
                if (uhcPlayer.getPlayerState() == null) {
                    if (uhcPlayer.getPlayer() != null && !scatterPlayers.contains(uhcPlayer.getUuid())) {
                        uhcPlayer.setScatterLocation(gameManager.getScatterLocations().get(currentScatter));
                        scatterPlayers.add(uhcPlayer.getUuid());
                        currentScatter++;
                    }
                }
            });
        }
        currentScatter = 0;
        gameManager.setMaxScatteredPlayers(scatterPlayers.size());
        new BukkitRunnable() {
            @Override
            public void run() {
                gameManager.setScatterCountdown(((scatterPlayers.size() - currentScatter) / 20) + 10);

                if (currentScatter != scatterPlayers.size()) {
                    UHCPlayer uhcPlayer = uhcPlayerManager.getPlayer(scatterPlayers.get(currentScatter));
                    if (uhcPlayer.getPlayer() != null) {
                        uhcPlayer.getPlayer().teleport(uhcPlayer.getScatterLocation());
                        uhcPlayerManager.preparePlayerGame(uhcPlayer);
                        Common.addVehicle(uhcPlayer.getPlayer());
                    }

                    currentScatter++;
                    gameManager.setScatteredPlayers(currentScatter);
                } else {
                    this.cancel();
                    Bukkit.getPluginManager().callEvent(new GameStartEvent());

                    uhcPlayerManager.getPlayers().values().forEach(uhcPlayer -> {
                        if (uhcPlayer.getPlayerState() == null) {
                            if (uhcPlayer.getPlayer() != null && uhcPlayer.getPlayer().getWorld() == UHC.getInstance().getWorldManager().getWorld()) {
                                uhcPlayer.getPlayer().teleport(locationUtil.randomLocation(world, worldSize));
                                UHC.getInstance().getUhcPlayerManager().preparePlayerGame(uhcPlayer);
                                Common.addVehicle(uhcPlayer.getPlayer());
                            }
                        }
                    });
                }
            }
        }.runTaskTimer(UHC.getInstance(), 0, 1);
    }


    @EventHandler
    public void onGameStartEvent(GameStartEvent event) {
        World world = UHC.getInstance().getWorldManager().getUhcWorld();
        new BukkitRunnable() {
            int count = 0;

            public void run() {
                count++;
                UHC.getInstance().getGameManager().setScatterCountdown(11 - count);
                Common.getCounter(count, 11, Sound.NOTE_PLING, "The game will start in §a<time>§f.");

                if (count >= 11) {
                    this.cancel();

                    Bukkit.broadcastMessage("§cThe game has begun.");

                    world.setTime(0);
                    world.setGameRuleValue("randomTickSpeed", "3");

                    Common.makeSound(Sound.EXPLODE);
                    Common.getOnlinePlayers().forEach(targetPlayer -> {
                        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(targetPlayer.getUniqueId());
                        if (uhcPlayer.getPlayerState() == null) {
                            uhcPlayer.setPlayerState(UHCPlayer.State.PLAYING);
                            Common.removeVehicle(targetPlayer);
                        }
                    });

                    UHC.getInstance().getGameManager().setMaxScatteredPlayers(UHC.getInstance().getGameManager().getAlivePlayers().size());
                    UHC.getInstance().getGameManager().setGameState(GameManager.State.GAME);
                    UHC.getInstance().getWorldManager().setUsed(true);
                    UHC.getInstance().getGameTask().runTaskTimer(UHC.getInstance(), 0, 20L);
                }
            }
        }.runTaskTimer(UHC.getInstance(), 0, 20L);
    }

    @EventHandler
    public void onBorderShrinkEvent(BorderShrinkEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        WorldManager worldManager = UHC.getInstance().getWorldManager();

        Bukkit.broadcastMessage("§7[§a§lBorder§7] §cBorder has shrunk to " + event.getNewBorder() + " from " + event.getPreviusBorder() + ".");
        if (gameManager.isNether()) {
            if (event.getNewBorder() == 500) {
                gameManager.setAllowNether(false);
                Bukkit.broadcastMessage("§fAll §6Nether§f players were teleported to §aOverworld");
                UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(uhcPlayer -> {
                    if (uhcPlayer.isAlive()) {
                        if (uhcPlayer.getPlayer().getWorld() == UHC.getInstance().getWorldManager().getUhcWorldNether())
                            new LocationUtil().randomLocation(worldManager.getUhcWorld(), event.getNewBorder());
                    }
                });
            }
        }
        if (event.getNewBorder() == 500 || event.getNewBorder() == 100) {

            //if (event.getNewBorder() == 100) Bukkit.broadcastMessage("§bPoner las reglas del 100x100 ;)");
            if (gameManager.isTeams()) {
                UHC.getInstance().getUhcTeamManager().getAllTeams().values().forEach(uhcTeam -> {
                    if (uhcTeam.isAlive()) {
                        Location teamLocation = new LocationUtil().randomLocation(worldManager.getUhcWorld(), event.getNewBorder());
                        boolean teleport = false;

                        for (UUID uuid : uhcTeam.getPlayers()) {
                            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(uuid);
                            if (uhcPlayer.isAlive()) {
                                if (uhcPlayer.getPlayer() != null) {
                                    int x = Math.abs(uhcPlayer.getPlayer().getLocation().getBlockX());
                                    int z = Math.abs(uhcPlayer.getPlayer().getLocation().getBlockZ());
                                    if (z > event.getNewBorder() || x > event.getNewBorder()) {
                                        teleport = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (teleport) uhcTeam.getPlayers().forEach(player -> {
                            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player);
                            if (uhcPlayer.isAlive()) uhcPlayer.getPlayer().teleport(teamLocation);
                        });
                    }
                });
            } else {
                gameManager.getAlivePlayers().forEach(uhcPlayer -> {
                    if (uhcPlayer.getPlayer() != null) {
                        Player player = uhcPlayer.getPlayer();
                        int x = Math.abs(player.getLocation().getBlockX());
                        int z = Math.abs(player.getLocation().getBlockZ());
                        if (z > event.getNewBorder() || x > event.getNewBorder())
                            player.teleport(new LocationUtil().randomLocation(worldManager.getUhcWorld(), event.getNewBorder()));
                    }
                });
            }
        }

        if (!UHC.getInstance().getCombatLoggerManager().getCombatLoggers().values().isEmpty()) {
            UHC.getInstance().getCombatLoggerManager().getCombatLoggers().values().forEach(combatLogger -> {
                Location location = combatLogger.getEntity().getLocation();
                int x = Math.abs(location.getBlockX());
                int z = Math.abs(location.getBlockZ());
                if (z > event.getNewBorder() || x > event.getNewBorder())
                    combatLogger.getEntity().teleport(new LocationUtil().randomLocation(worldManager.getUhcWorld(), event.getNewBorder()));
            });
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + worldManager.getUhcWorldName() + " set " + event.getNewBorder() + " " + event.getNewBorder() + " 0 0");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + worldManager.getUhcWorldNetherName() + " set " + event.getNewBorder() + " " + event.getNewBorder() + " 0 0");
        worldManager.shrinkBorder(event.getNewBorder(), 5);
    }


    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World world = event.getFrom().getWorld();
        WorldManager worldManager = UHC.getInstance().getWorldManager();

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (world == worldManager.getUhcWorld()) {
                double x = player.getLocation().getX() / 8.0D;
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ() / 8.0D;

                Location to = new Location(worldManager.getUhcWorldNether(), x, y, z);
                event.setTo(event.getPortalTravelAgent().findOrCreate(to));
            } else if (world == worldManager.getUhcWorldNether()) {
                double x = player.getLocation().getX() * 8.0D;
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ() * 8.0D;

                Location to = new Location(worldManager.getUhcWorld(), x, y, z);
                event.setTo(event.getPortalTravelAgent().findOrCreate(to));
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        GameManager gameManager = UHC.getInstance().getGameManager();
        Player player = event.getEntity();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() != UHCPlayer.State.PLAYING) return;
        uhcPlayer.setTotalDeaths(uhcPlayer.getTotalDeaths() + 1);
        uhcPlayer.setArmor(player.getInventory().getArmorContents().clone());
        uhcPlayer.setInventory(player.getInventory().getContents().clone());

        if (!UHC.getInstance().getGamemodeManager().exists("TimeBomb")) Common.placeFence(player, player.getLocation());
        Common.summonFakePlayer(player);
        UHC.getInstance().getUhcPlayerManager().prepareSpecator(player);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(UHC.getInstance(), () -> {
            if (player.isDead()) player.setHealth(20);
        });

        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            UHCPlayer uhcPlayerKiller = UHC.getInstance().getUhcPlayerManager().getPlayer(killer.getUniqueId());
            uhcPlayerKiller.setKills(uhcPlayerKiller.getKills() + 1);
            uhcPlayerKiller.setTotalKills(uhcPlayer.getTotalKills() + 1);
        }

        gameManager.handleDeathMessage(event.getEntity(), player.getKiller());
        player.sendMessage("§eYou're now a spectator: §celiminated§e.");

        if (!gameManager.isEnded()) gameManager.checkWinners();
        if (!player.hasPermission("uhc.spec")) {
            Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> {
                uhcPlayer.setPlayerState(UHCPlayer.State.DEAD);
                if (player.isOnline()) player.kickPlayer("§cYou have died. To spectate, acquires a rank.");
            }, 20 * 30);
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() != UHCPlayer.State.PLAYING) return;
        if (event.getFoodLevel() < player.getFoodLevel() && new Random().nextInt(100) > 4) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (uhcPlayer.getPlayerState() != UHCPlayer.State.PLAYING) return;
        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) event.setCancelled(true);
    }

    @EventHandler
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
        if (!(event.getInventory() instanceof CraftingInventory)) return;

        CraftingInventory inv = event.getInventory();
        ItemStack godApple = new ItemUtil(Material.GOLDEN_APPLE, 1).get();

        if (inv.getResult() == godApple) inv.setResult(new ItemUtil(Material.AIR).get());
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player.getUniqueId());

        if (UHC.getInstance().getGameManager().getGameState() != GameManager.State.GAME) return;
        if (event.getItem().getType() != Material.GOLDEN_APPLE) return;

        if (event.getItem().isSimilar(UHC.getInstance().getGameManager().getGoldenHead())) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1));
            uhcPlayer.setTotalGoldenHeadsEaten(uhcPlayer.getTotalGoldenHeadsEaten() + 1);
        } else uhcPlayer.setTotalGoldenApplesEaten(uhcPlayer.getTotalGoldenApplesEaten() + 1);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityCombustEvent event) {
        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        CombatLogger combatLogger = combatLoggerManager.getByEntity(event.getEntity());
        if (combatLogger == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        CombatLogger combatLogger = combatLoggerManager.getByEntity(event.getEntity());

        if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Player || event.getEntity() instanceof Zombie) {
                if (gameManager.isPvpTimeAlready()) return;
                if (event.getEntity() instanceof Zombie) {
                    if (combatLogger == null) return;
                    event.setCancelled(true);
                }
                event.setCancelled(true);

                Player player = (Player) event.getDamager();
                player.sendMessage("§cYou're not allowed to do this until grace period end.");
            }
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie)) return;

        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        CombatLogger combatLogger = combatLoggerManager.getByEntity(event.getEntity());
        if (combatLogger == null) return;

        event.getDrops().clear();
        event.setDroppedExp(combatLogger.getExperience());

        if (!UHC.getInstance().getGamemodeManager().exists("TimeBomb")) {
            for (ItemStack item : combatLogger.getArmor()) event.getDrops().add(item);
            for (ItemStack item : combatLogger.getInventory()) event.getDrops().add(item);
        }

        Bukkit.getPluginManager().callEvent(new CombatLoggerDeathEvent(combatLogger, event.getEntity().getKiller(), event.getEntity().getLocation()));
    }

    @EventHandler
    public void onCombatLoggerDeathEvent(CombatLoggerDeathEvent event) {
        GameManager gameManager = UHC.getInstance().getGameManager();
        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        CombatLogger combatLogger = event.getCombatLogger();

        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(combatLogger.getUuid());
        uhcPlayer.setTotalDeaths(uhcPlayer.getTotalDeaths() + 1);
        uhcPlayer.setPlayerState(UHCPlayer.State.DEAD);

        if (!UHC.getInstance().getGamemodeManager().exists("TimeBomb"))
            Common.placeFence(combatLogger.getPlayer(), event.getLocation());

        if (event.getKiller() instanceof Player) {
            Player killer = (Player) event.getKiller();
            UHCPlayer uhcPlayerKiller = UHC.getInstance().getUhcPlayerManager().getPlayer(killer.getUniqueId());
            uhcPlayerKiller.setKills(uhcPlayerKiller.getKills() + 1);
            uhcPlayerKiller.setTotalKills(uhcPlayerKiller.getTotalKills() + 1);

            gameManager.handleDeathMessage(combatLogger.getEntity(), (Player) event.getKiller());
        } else {
            gameManager.handleDeathMessage(combatLogger.getEntity(), null);
        }

        combatLoggerManager.remove(combatLogger.getUuid());
        if (!gameManager.isEnded()) gameManager.checkWinners();
    }

    @EventHandler
    public void onTeamWinEvent(TeamWinEvent event) {
        UHCTeam uhcTeam = event.getUhcTeam();

        uhcTeam.getPlayers().forEach(player -> {
            UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(player);
            uhcPlayer.setTotalWins(uhcPlayer.getTotalWins());
        });

        List<String> playersName = new ArrayList<>();
        uhcTeam.getPlayers().forEach(player -> playersName.add(Bukkit.getOfflinePlayer(player).getName()));
        Bukkit.broadcastMessage(uhcTeam.getColor() + StringUtils.join(playersName, "§7, " + uhcTeam.getColor()) + " §awins!");
        UHC.getInstance().getGameManager().saveStats();

        new BukkitRunnable() {
            int launchedFireworks;

            public void run() {
                uhcTeam.getPlayers().forEach(uuid -> {
                    UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    if (uhcPlayer.isAlive()) {
                        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                        FireworkMeta fireworkMeta = firework.getFireworkMeta();
                        fireworkMeta.addEffect(FireworkEffect.builder().flicker(false).trail(true).with(FireworkEffect.Type.BURST).withColor(Color.ORANGE).withFade(Color.YELLOW).build());
                        fireworkMeta.setPower(3);
                        firework.setFireworkMeta(fireworkMeta);
                    }
                });
                if (launchedFireworks++ == 10) this.cancel();
            }
        }.runTaskTimer(UHC.getInstance(), 0, 20);
    }


    @EventHandler
    public void onPlayerWinEvent(PlayerWinEvent event) {
        UHCPlayer uhcPlayer = event.getUhcPlayer();
        uhcPlayer.setTotalWins(uhcPlayer.getTotalWins());

        Player player = uhcPlayer.getPlayer();

        Bukkit.broadcastMessage("§a" + player.getName() + " §awins!");
        UHC.getInstance().getGameManager().saveStats();

        new BukkitRunnable() {
            int launchedFireworks;

            public void run() {
                Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                FireworkMeta fireworkMeta = firework.getFireworkMeta();
                fireworkMeta.addEffect(FireworkEffect.builder().flicker(false).trail(true).with(FireworkEffect.Type.BURST).withColor(Color.ORANGE).withFade(Color.YELLOW).build());
                fireworkMeta.setPower(3);
                firework.setFireworkMeta(fireworkMeta);

                if (launchedFireworks++ == 10) this.cancel();
            }
        }.runTaskTimer(UHC.getInstance(), 0, 20);
    }

}
