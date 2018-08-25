package me.raauhh.uhc.events;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.manager.team.UHCTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class TeamWinEvent  extends Event {

    private static HandlerList handlers = new HandlerList();

    private UHCTeam uhcTeam;

    public TeamWinEvent(UHCTeam uhcTeam){
        this.uhcTeam = uhcTeam;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
