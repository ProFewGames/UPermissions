package xyz.ufactions.upermissions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.ufactions.prolib.api.MegaPlugin;
import xyz.ufactions.prolib.libs.FileHandler;
import xyz.ufactions.prolib.pluginupdater.ProUpdater;
import xyz.ufactions.upermissions.command.PermissionsCommand;
import xyz.ufactions.upermissions.hook.VaultHook;
import xyz.ufactions.upermissions.listener.PlayerListener;
import xyz.ufactions.upermissions.manager.PermissionsManager;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class UPermissions extends MegaPlugin {

    private PermissionsManager permissionsManager;
    private VaultHook vaultHook;

    @Override
    public void enable() {
        addCommand(new PermissionsCommand(this));

        registerEvents(new PlayerListener(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        this.permissionsManager = new PermissionsManager(this);
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.vaultHook = new VaultHook(this);
            vaultHook.hook();
        }

        startUpdater();
    }

    private void startRemoval() {
        log("Starting Removal...");
        World world = Bukkit.getWorld("world");
        for (int x = -256; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = -256; z < 256; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.getMaterial("ENDER_PORTAL")) {
                        block.setType(Material.SPONGE);
                    }
                }
            }
        }
        log("Removal Completed");
    }


    private void startUpdater() {
        FileHandler<?> updates = FileHandler.instance(this, this.getDataFolder(), "updater.yml");
        if (!updates.getBoolean("enable", false)) return;
        try {
            Authenticator authenticator = null;
            if (!updates.getString("username", "").isEmpty() && !updates.getString("password", "").isEmpty()) {
                authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(updates.getString("username"), updates.getString("password").toCharArray());
                    }
                };
            }
            ProUpdater updater = new ProUpdater(this, updates.getString("url"), authenticator);
            updater.scheduleUpdater();
        } catch (Exception e) {
            e.printStackTrace();
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

    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }
}