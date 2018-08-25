package main.java.me.raauhh.uhc;

import net.minecraft.util.com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.MessageDigest;
import java.util.*;

@Getter
public class UHC extends JavaPlugin {

    /**
     * Ultra Hardcore Plugin made by Raauhh.
     *
     * Do not forget to thank the creator.
     *
     * Social:
     *  - Twitter: @RAAUHH
     *  - Mail: contact@raauhh.me
     */

    @Getter
    private static UHC instance;

    private RedisWrapper redisWrapper;
    private RedisMessagingHandler redisMessagingHandler;
    private boolean usingRedis;

    private GameManager gameManager;
    private CombatLoggerManager combatLoggerManager;
    private UHCPlayerManager uhcPlayerManager;

    private UHCTeamManager uhcTeamManager;
    private UHCTeamRequestManager uhcTeamRequestManager;

    private GamemodeManager gamemodeManager;
    private RScoreboardManager rScoreboardManager;
    private WorldManager worldManager;
    private VisualiseHandler visualiseHandler;

    private UHCConfigMenu uhcConfigMenu;
    private GamemodesMenu gamemodesMenu;
    private SpectateMenu spectateMenu;

    private GameTask gameTask;
    private ScoreboardTask scoreboardTask;

    private HashSet<String> whitelist = new HashSet<>();
    private Map<UUID, Set<String>> playerWhitelists = new HashMap<>();

    private double startTime;

    @Override
    public void onEnable() {
        startTime = System.currentTimeMillis();
        instance = this;

        getLogger().info("Registering configuration...");
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        getLogger().info("Registering commands...");
        registerCommands();

        this.usingRedis = getConfig().getBoolean("redis.enabled");
        if (this.usingRedis) {
            this.redisWrapper = new RedisWrapper(getConfig().getConfigurationSection("redis"));
            this.redisMessagingHandler = new RedisMessagingHandler();
            getLogger().info("Redis connection was successful.");
        }

        this.uhcPlayerManager = new UHCPlayerManager();
        this.combatLoggerManager = new CombatLoggerManager();
        this.uhcTeamManager = new UHCTeamManager();
        this.uhcTeamRequestManager = new UHCTeamRequestManager();
        this.gamemodeManager = new GamemodeManager();
        this.rScoreboardManager = new RScoreboardManager();
        this.gameManager = new GameManager();
        this.worldManager = new WorldManager();
        this.visualiseHandler = new VisualiseHandler();

        this.uhcConfigMenu = new UHCConfigMenu();
        this.gamemodesMenu = new GamemodesMenu();
        this.spectateMenu = new SpectateMenu();

        this.scoreboardTask = new ScoreboardTask();
        this.gameTask = new GameTask();

        this.gamemodeManager.activate(new CutClean());
        this.gamemodeManager.activate(new TimeBomb());
        //this.gamemodeManager.activate(new BuildUHC());

        getLogger().info("Registering listeners...");
        registerListeners();

        getLogger().info("Loaded in " + (System.currentTimeMillis() - startTime) / 1000 + " ms");
    }

    @Override
    public void onDisable() {
        if (!this.usingRedis) return;

        JsonObject object = new JsonObject();
        object.addProperty("server", Bukkit.getServerName());
        this.redisWrapper.getJedis().publish("uhc:end", object.toString());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getServer().getPluginManager().registerEvents(new GlassBorderListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    private void registerCommands() {
        getCommand("team").setExecutor(new TeamCommand());
        getCommand("teamlist").setExecutor(new TeamListCommand());
        getCommand("scenarios").setExecutor(new ScenariosCommand());
        getCommand("uhc").setExecutor(new UHCCommand());
        getCommand("killtop").setExecutor(new KillTopCommand());
        getCommand("whitelist").setExecutor(new WhitelistCommand());
        getCommand("config").setExecutor(new ConfigCommand());
        getCommand("killcount").setExecutor(new KillCountCommand());
        getCommand("helpop").setExecutor(new HelpOpCommand());
    }
}
