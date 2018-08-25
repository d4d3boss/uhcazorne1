package me.raauhh.uhc.events;

import lombok.Getter;
import lombok.Setter;
import me.raauhh.uhc.manager.player.UHCPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PlayerWinEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private UHCPlayer uhcPlayer;

    public PlayerWinEvent(UHCPlayer uhcPlayer) {
        this.uhcPlayer = uhcPlayer;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
