package me.raauhh.uhc.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class BorderShrinkEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private int previusBorder;
    private int newBorder;

    public BorderShrinkEvent(int newBorder, int previusBorder){
        this.previusBorder = previusBorder;
        this.newBorder = newBorder;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
