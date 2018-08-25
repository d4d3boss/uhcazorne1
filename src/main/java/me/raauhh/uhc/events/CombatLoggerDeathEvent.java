package me.raauhh.uhc.events;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.manager.combatlogger.CombatLogger;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class CombatLoggerDeathEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private CombatLogger combatLogger;
    private Entity killer;
    private Location location;

    public CombatLoggerDeathEvent(CombatLogger combatLogger, Entity killer, Location location) {
        this.killer = killer;
        this.combatLogger = combatLogger;
        this.location = location;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
