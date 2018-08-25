package me.raauhh.uhc.manager.player;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.UHC;
import me.raauhh.uhc.manager.combatlogger.CombatLoggerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
@Setter
public class UHCPlayer {

    public enum State {
        PLAYING, SPEC, DEAD;
    }

    private Player player;
    private UUID uuid;
    private String name;

    private int kills = 0;
    private int diamonds = 0;
    private int gold = 0;
    private int iron = 0;

    private State playerState;

    private int totalKills = 0;
    private int totalDeaths = 0;
    private int totalWins = 0;
    private int totalGoldenApplesEaten = 0;
    private int totalGoldenHeadsEaten = 0;

    private ItemStack[] armor;
    private ItemStack[] inventory;
    private Location scatterLocation;

    public UHCPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isAlive() {
        CombatLoggerManager combatLoggerManager = UHC.getInstance().getCombatLoggerManager();
        return (this.playerState == UHCPlayer.State.PLAYING || combatLoggerManager.exists(this.uuid));
    }
}
