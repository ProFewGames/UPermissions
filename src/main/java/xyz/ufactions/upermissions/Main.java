package xyz.ufactions.upermissions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.ufactions.api.Module;
import xyz.ufactions.upermissions.commands.PermissionsCommand;
import xyz.ufactions.upermissions.events.PlayerPermissionsLoginEvent;
import xyz.ufactions.upermissions.files.ConfigFile;
import xyz.ufactions.upermissions.hook.VaultHookManager;
import xyz.ufactions.upermissions.managers.PermissionsManager;

public class Main extends JavaPlugin implements Listener {

    private PermissionsManager manager;

    private static ConfigFile config;

    private VaultHookManager vaultHookManager;

    @Override
    public void onEnable() {
        getCommand("upermissions").setExecutor(new PermissionsCommand(this));

        Module dummy = new Module("UPermissions", this) {
        };

        config = new ConfigFile(dummy);

        debug("debugging enabled");

        Bukkit.getPluginManager().registerEvents(this, this);

        manager = new PermissionsManager(dummy);

        manager.loadGroups();

        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.login(player.getUniqueId());
        }

        tryVaultHook();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.unload(player);
        }
        if (this.vaultHookManager != null)
            this.vaultHookManager.unhook();
    }

    private void tryVaultHook() {
        try {
            if (getServer().getPluginManager().isPluginEnabled("Vault")) {
                this.vaultHookManager = new VaultHookManager(this);
                this.vaultHookManager.hook();
                System.out.println("Registered Vault permission hook");
            }
        } catch (Exception e) {
            this.vaultHookManager = null;
            System.err.println("Error occurred whilst hooking into Vault.");
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        Main.debug("Async Login for : " + e.getName());
        manager.login(e.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.unload(e.getPlayer());
    }

    @EventHandler
    public void onPermissionsLogin(PlayerPermissionsLoginEvent e) {
        Main.debug("Permissions Login heard");
        Player player = Bukkit.getPlayer(e.getUniqueId());
        if (player != null) {
            Main.debug("Injecting permissions");
            manager.injectPermissions(player);
        } else {
            Main.debug("? false call player offline : " + e.getUniqueId() + ":?");
        }
    }

    public void reload() {
        Module dummy = new Module("UPermissions", this) {
        };
        config.reload();
        debug("debugging enabled");
        manager.reload();
    }

    public PermissionsManager getManager() {
        return manager;
    }

    public static void debug(Object o) {
        if (config.debug()) System.out.println("[DEBUGGING] " + o);
    }
}