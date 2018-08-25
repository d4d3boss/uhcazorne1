package me.raauhh.uhc.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Date;

@Getter
public class GameScheduleEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private Date scheduledDate;

    public GameScheduleEvent(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
