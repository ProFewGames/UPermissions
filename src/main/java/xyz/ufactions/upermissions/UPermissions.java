package xyz.ufactions.upermissions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.ufactions.prolib.api.MegaPlugin;
import xyz.ufactions.prolib.libs.C;
import xyz.ufactions.upermissions.command.PermissionsCommand;
import xyz.ufactions.upermissions.hook.VaultHook;
import xyz.ufactions.upermissions.listener.PlayerListener;
import xyz.ufactions.upermissions.manager.PermissionsManager;

public class UPermissions extends MegaPlugin {

    private PermissionsManager permissionsManager;
    private VaultHook vaultHook;
    private boolean debugging = true;

    @Override
    public void enable() {
        getDummy().addCommand(new PermissionsCommand(getDummy()));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        this.permissionsManager = new PermissionsManager(this);
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.vaultHook = new VaultHook(this);
            vaultHook.hook();
        }
    }

    @Override
    public void disable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            permissionsManager.unload(player);
        }
        if (vaultHook != null) vaultHook.unhook();
    }

    public void reload() {
        this.permissionsManager.reload();
        if (this.vaultHook != null) {
            vaultHook.unhook();
            vaultHook.hook();
        }
    }

    public void debug(String message) {
        if (debugging) log(C.mHead + "[DEBUG] " + C.mBody + message);
    }

    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }
}