package xyz.ufactions.upermissions.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.ufactions.upermissions.UPermissions;

public class PlayerListener implements Listener {

    private final UPermissions plugin;

    public PlayerListener(UPermissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        plugin.getPermissionsManager().reload(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getPermissionsManager().unload(e.getPlayer());
    }
}