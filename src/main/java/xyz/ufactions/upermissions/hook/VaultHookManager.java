package xyz.ufactions.upermissions.hook;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import xyz.ufactions.upermissions.Main;

public class VaultHookManager {

    private final Main main;

    private UPermissionsVaultChat chat = null;

    public VaultHookManager(Main main) {
        this.main = main;
    }

    public void hook() {
        try {
            if (this.chat == null) this.chat = new UPermissionsVaultChat(this.main);
            ServicesManager sm = this.main.getServer().getServicesManager();
            sm.register(Chat.class, this.chat, this.main, ServicePriority.High);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unhook() {
        ServicesManager sm = this.main.getServer().getServicesManager();
        if (this.chat != null) {
            sm.unregister(Chat.class, this.chat);
            this.chat = null;
        }
    }
}