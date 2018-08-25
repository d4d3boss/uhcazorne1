package me.raauhh.uhc.manager.gamemode;

import lombok.Getter;
import me.raauhh.uhc.UHC;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GamemodeManager {

    @Getter
    private static Map<String, Gamemode> uhcGamemodes = new HashMap<>();

    public void activate(Gamemode gamemode) {
        uhcGamemodes.put(gamemode.getName(), gamemode);
        Bukkit.getPluginManager().registerEvents(gamemode, UHC.getInstance());
    }

    public void deactivate(String name) {
        uhcGamemodes.get(name).deactivate();
        uhcGamemodes.remove(name);
    }

    public boolean exists(String name) {
        for (Gamemode gamemode : uhcGamemodes.values()) if (gamemode.getName().equalsIgnoreCase(name)) return true;
        return false;
    }

    public Gamemode getGamemode(String name) {
        for (Gamemode gamemode : uhcGamemodes.values()) {
            if (gamemode.getName().equalsIgnoreCase(name)) return gamemode;
        }
        return null;
    }

    public List<Gamemode> getEnabledGamemodes() {
        return new ArrayList<>(uhcGamemodes.values());
    }

    public List<String> getGamemodes(){
        List<String> gamemodes = new ArrayList<>();
        gamemodes.add("CutClean");
        gamemodes.add("TimeBomb");
        gamemodes.add("No Clean");
        gamemodes.add("Build UHC");
        return gamemodes;
    }
}
