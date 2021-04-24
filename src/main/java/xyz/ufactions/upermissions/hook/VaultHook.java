package xyz.ufactions.upermissions.hook;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import xyz.ufactions.upermissions.UPermissions;

public class VaultHook {

    private final UPermissions plugin;

    private Permission permission;
    private Chat chat;

    public VaultHook(UPermissions plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        hookPermission();
        hookChat();
    }

    private void hookPermission() {
        if (this.permission == null) this.permission = new VaultPermission(plugin);
        ServicesManager serviceManager = this.plugin.getServer().getServicesManager();
        serviceManager.register(Permission.class, this.permission, this.plugin, ServicePriority.Highest);
    }

    private void hookChat() {
        if (this.chat == null) this.chat = new VaultChat(this.permission, plugin);
        ServicesManager serviceManager = this.plugin.getServer().getServicesManager();
        serviceManager.register(Chat.class, this.chat, this.plugin, ServicePriority.Highest);
    }

    public void unhook() {
        unhookPermission();
        unhookChat();
    }

    private void unhookPermission() {
        ServicesManager servicesManager = this.plugin.getServer().getServicesManager();
        if (this.permission != null) {
            servicesManager.unregister(Permission.class, this.permission);
            this.permission = null;
        }
    }

    public void unhookChat() {
        ServicesManager servicesManager = this.plugin.getServer().getServicesManager();
        if (this.chat != null) {
            servicesManager.unregister(Permission.class, this.chat);
            this.chat = null;
        }
    }
}