package me.raauhh.uhc.manager.combatlogger;

import lombok.Getter;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class CombatLoggerManager {

    private Map<UUID, CombatLogger> combatLoggers = new HashMap<>();

    public void create(UUID uuid) {
        this.combatLoggers.put(uuid, new CombatLogger(uuid));
    }

    public boolean exists(UUID uuid) {
        return this.combatLoggers.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        combatLoggers.remove(uuid);
    }

    public CombatLogger get(UUID uuid) {
        return this.combatLoggers.get(uuid);
    }

    public CombatLogger getByName(String name) {
        for (CombatLogger combatLogger : this.combatLoggers.values())
            if (name.equals(combatLogger.getName())) return combatLogger;
        return null;
    }
    public CombatLogger getByEntity(Entity entity) {
        for (CombatLogger combatLogger : this.combatLoggers.values())
            if (entity == combatLogger.getEntity()) return combatLogger;
        return null;
    }

}
