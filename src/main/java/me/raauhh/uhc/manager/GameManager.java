package me.raauhh.uhc.manager;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.combatlogger.CombatLoggerManager;
import me.raauhh.uhc.events.PlayerWinEvent;
import me.raauhh.uhc.events.TeamWinEvent;
import me.raauhh.uhc.manager.player.UHCPlayer;
import me.raauhh.uhc.manager.team.UHCTeam;
import me.raauhh.uhc.utils.Common;
import me.raauhh.uhc.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GameManager {

    public enum State {
        GENERATING, LOBBY, SCATTER, GAME
    }

    private ItemStack goldenHead;

    private HashSet<UUID> host = new HashSet<>(), moderators = new HashSet<>();
    private List<Location> scatterLocations = new ArrayList<>();

    private int scatteredPlayers, maxScatteredPlayers, scatterCountdown, gameTime, scheduledTime, startTime, borderCount, currentBorder, slots, teamSize, starterFood, appleRatePercent, globalChat, healTime, pvpTime, borderTime, deathmatchTime, generationPercent;
    private boolean deathmatchRunning, scheduled, globalChatAlready, healTimeAlready, pvpTimeAlready, borderTimeAlready, deathmatchTimeAlready, whitelist, teams, nether, allowNether, stats, event, ended;

    private State gameState;

    public GameManager() {
        this.gameState = State.GENERATING;

        this.whitelist = true;
        this.teams = false;
        this.nether = false;
        this.allowNether = true;
        this.stats = false;
        this.event = false;
        this.globalChatAlready = false;
        this.healTimeAlready = false;
        this.pvpTimeAlready = false;
        this.borderTimeAlready = false;
        this.deathmatchTimeAlready = false;
        this.deathmatchRunning = false;
        this.scheduled = false;
        this.ended = false;

        this.globalChat = 5 * 60;
        this.healTime = 10 * 60;
        this.pvpTime = 20 * 60;
        this.borderTime = 50 * 60;
        this.deathmatchTime = 5 * 60;

        this.slots = 150;
        this.teamSize = 2;
        this.starterFood = 10;
        this.appleRatePercent = 2;

        this.scatteredPlayers = 0;
        this.maxScatteredPlayers = 0;
        this.scatterCountdown = 10;
        this.gameTime = 0;
        this.scheduledTime = 0;
        this.borderCount = 0;

        this.goldenHead = new ItemUtil(Material.GOLDEN_APPLE).setName("§6§lGolden Head").get();
        ShapedRecipe goldenHeadRecipe = new ShapedRecipe(this.goldenHead);
        goldenHeadRecipe.shape("@@@", "@#@", "@@@");
        goldenHeadRecipe.setIngredient('@', Material.GOLD_INGOT);
        goldenHeadRecipe.setIngredient('#', Material.SKULL_ITEM, 3);
        Bukkit.getServer().addRecipe(goldenHeadRecipe);
    }

    public List<UHCPlayer> getAlivePlayers() {
        List<UHCPlayer> alivePlayers = new ArrayList<>();
        UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(players -> {
            CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
            if (players.getPlayerState() == UHCPlayer.State.PLAYING || combatLoggerManager.exists(players.getUuid())) {
                alivePlayers.add(players);
            }
        });
        return alivePlayers;
    }

    public List<UHCTeam> getAliveTeams() {
        List<UHCTeam> uhcTeams = new ArrayList<>();
        UHC.getInstance().getUhcTeamManager().getAllTeams().values().forEach(uhcTeam -> {
            if (uhcTeam.isAlive() && !uhcTeams.contains(uhcTeam)) {
                uhcTeams.add(uhcTeam);
            }
        });
        return uhcTeams;
    }

    public void butcherMobs() {
        UHC.getInstance().getWorldManager().getUhcWorld().getEntities().forEach(entity -> {
            if (entity instanceof Skeleton || entity instanceof Zombie || entity instanceof Spider || entity instanceof Creeper
                    || entity instanceof Sheep || entity instanceof Pig || entity instanceof Squid || entity instanceof Bat) {
                entity.remove();
            }
        });
    }

    public boolean isGameRunning(){
        return this.gameState != State.LOBBY;
    }

    public boolean isInStaffMode(Player player) {
        return moderators.contains(player.getUniqueId()) || host.contains(player.getUniqueId());
    }

    public void checkWinners() {
        if (isTeams()) {
            if (UHC.getInstance().getUhcTeamManager().getAliveTeams() != 1) return;
            UHCTeam uhcTeam = getAliveTeams().get(0);
            Bukkit.getPluginManager().callEvent(new TeamWinEvent(uhcTeam));
        } else {
            if (UHC.getInstance().getUhcPlayerManager().getAlivePlayers() != 1) return;
            UHCPlayer uhcPlayer = getAlivePlayers().get(0);
            Bukkit.getPluginManager().callEvent(new PlayerWinEvent(uhcPlayer));
        }

        this.ended = true;
        UHC.getInstance().getGameTask().cancel();
    }

    public void saveStats() {
        UHC.getInstance().getLogger().info("Attempting to save player's stats");
        Bukkit.getScheduler().runTaskAsynchronously(UHC.getInstance(), () -> {
            UHC.getInstance().getUhcPlayerManager().getPlayers().values().forEach(uhcPlayer -> {
                // TODO: uhcPlayer.saveData();
            });
        });
        UHC.getInstance().getLogger().info("Players stats were saved successful");
    }

    public void formatMessage(Player player, String message) {
        formatMessage(player, null, message);
    }

    public void formatMessage(Player dead, Player killer, String message) {
        String color1 = UHC.getInstance().getConfig().getString("colored-names.teammates");
        String color2 = UHC.getInstance().getConfig().getString("colored-names.enemies");

        Common.getOnlinePlayers().forEach(player -> {
            if (UHC.getInstance().getGameManager().isTeams()) {
                UHCTeam uhcTeam = UHC.getInstance().getUhcTeamManager().getAllTeams().get(dead.getUniqueId());
                if (killer == null) {
                    if (dead == player || uhcTeam.getPlayers().contains(player.getUniqueId()))
                        player.sendMessage(message.replace("%1", color1).replace("%2", color2));
                    else player.sendMessage(message.replace("%1", color2).replace("%2", color2));
                } else {
                    UHCTeam uhcTeamKiller = UHC.getInstance().getUhcTeamManager().getAllTeams().get(killer.getUniqueId());
                    if (uhcTeamKiller.getPlayers().contains(dead.getUniqueId()))
                        player.sendMessage(message.replace("%1", color1).replace("%2", color1));
                    else if (dead == player || uhcTeam.getPlayers().contains(player.getUniqueId()))
                        player.sendMessage(message.replace("%1", color1).replace("%2", color2));
                    else if (killer == player || uhcTeamKiller.getPlayers().contains(player.getUniqueId()))
                        player.sendMessage(message.replace("%1", color2).replace("%2", color1));
                    else player.sendMessage(message.replace("%1", color2).replace("%2", color2));
                }
            } else {
                if (killer == null) {
                    if (dead == player) player.sendMessage(message.replace("%1", color1).replace("%2", color2));
                    else player.sendMessage(message.replace("%1", color2).replace("%2", color2));
                } else {
                    if (dead == killer) player.sendMessage(message.replace("%1", color1).replace("%2", color2));
                    else if (dead == player) player.sendMessage(message.replace("%1", color1).replace("%2", color2));
                    else if (killer == player) player.sendMessage(message.replace("%1", color2).replace("%2", color1));
                    else player.sendMessage(message.replace("%1", color2).replace("%2", color2));
                }
            }
        });
    }

    public void handleDeathMessage(LivingEntity livingEntity, Player killer) {
        String name;
        if (livingEntity instanceof Player) name = ((Player) livingEntity).getName();
        else name = livingEntity.getCustomName();

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        UHCPlayer uhcPlayer = UHC.getInstance().getUhcPlayerManager().getPlayer(offlinePlayer.getUniqueId());
        Player player = uhcPlayer.getPlayer();
        String killed_kills = "§7[§f" + uhcPlayer.getKills() + "§7]";

        UHCPlayer uhcPlayerKiller;
        String killer_kills = null;
        if (killer != null) {
            uhcPlayerKiller = UHC.getInstance().getUhcPlayerManager().getPlayer(killer.getUniqueId());
            killer_kills = "§7[§f" + uhcPlayerKiller.getKills() + "§7]";
        }

        if (livingEntity.getLastDamageCause() == null) {
            formatMessage(player, "%1" + player.getName() + killed_kills + " §edied.");
            return;
        }

        switch (livingEntity.getLastDamageCause().getCause()) {
            case BLOCK_EXPLOSION:
                formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ejust got blown the hell up.");
                break;
            case CONTACT:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §ewas pricked to death.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewalked into a cactus whilst trying to escape %2" + killer.getName() + killer_kills + "§e.");
                break;
            case CUSTOM:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §edied for some reason.");
                break;
            case DROWNING:
                if (killer == null) formatMessage(player, "%1" + player.getName() + killed_kills + " §edrowned.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §edrowned whilst trying to escape %2" + killer.getName() + killer_kills + "§e.");
                break;
            case ENTITY_ATTACK:
                if (killer == null) formatMessage(player, "%1" + player.getName() + killed_kills + " §ewas slain.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewas slain by %2" + killer.getName() + killer_kills + "§e.");
                break;
            case ENTITY_EXPLOSION:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §egot blown the hell up.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §egot blown the hell up by %2" + killer.getName() + killer_kills + "§e.");
                break;
            case FALL:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §efell from a high place.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewas doomed to fall by %2" + killer.getName() + killer_kills + "§e.");
                break;
            case FALLING_BLOCK:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §egot freaking squashed by a block.");
                break;
            case FIRE:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §ewent up in flames.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewalked into a fire whilst fighting %2" + killer.getName() + killer_kills + "§e.");
                break;
            case FIRE_TICK:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §eburned to death.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewas burnt to a crisp whilst fighting %2" + killer.getName() + killer_kills + "§e.");
                break;
            case LAVA:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §etried to swim in lava.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §etried to swim in lava while trying to escape %2" + killer.getName() + killer_kills + "§e.");
                break;
            case LIGHTNING:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §egot lit the hell up by lightning.");
                break;
            case MAGIC:
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §ewas killed by magic.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewas killed by %2" + killer.getName() + killer_kills + "§e.");
                break;
            case POISON:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §ewas poisoned.");
                break;
            case PROJECTILE:
                if (killer == null) formatMessage(player, "%1" + player.getName() + killed_kills + " §ewas shot.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewas shot by %2" + killer.getName() + killer_kills + " §efrom §9" + ((int) player.getLocation().distance(killer.getLocation())) + " blocks§e.");
                break;
            case STARVATION:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §estarved to death.");
                break;
            case SUFFOCATION:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §esuffocated in a wall.");
                break;
            case SUICIDE:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §etook his own life like a peasant.");
                break;
            case THORNS:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §ekilled themself by trying to kill someone LOL.");
                break;
            case VOID:
                player.getLocation().distanceSquared(killer.getLocation());
                if (killer == null)
                    formatMessage(player, "%1" + player.getName() + killed_kills + " §efell out of the world.");
                else
                    formatMessage(player, killer, "%1" + player.getName() + killed_kills + " §ewas knocked into the void by %2" + killer.getName() + killer_kills + "§e.");
                break;
            case WITHER:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §ewithered away.");
                break;
            default:
                formatMessage(player, "%1" + player.getName() + killed_kills + " §edied.");
                break;
        }
    }
}
