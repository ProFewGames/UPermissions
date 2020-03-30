package xyz.ufactions.upermissions.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.ufactions.upermissions.data.PermissionsUser;

import java.util.UUID;

public class PlayerPermissionsLoginEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private UUID uuid;
    private PermissionsUser user;

    public PlayerPermissionsLoginEvent(UUID uuid, PermissionsUser user) {
        this.uuid = uuid;
        this.user = user;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PermissionsUser getUser() {
        return user;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}