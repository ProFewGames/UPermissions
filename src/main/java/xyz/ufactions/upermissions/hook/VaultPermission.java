package xyz.ufactions.upermissions.hook;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import xyz.ufactions.upermissions.UPermissions;
import xyz.ufactions.upermissions.data.Group;

final class VaultPermission extends Permission {

    final UPermissions plugin;

    VaultPermission(UPermissions plugin) {
        this.plugin = plugin;
    }

    @Deprecated
    OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }

    @Override
    public String[] getGroups() {
        return plugin.getPermissionsManager().getGroups().stream().map(Group::getName).toArray(String[]::new);
    }

    @Override
    public boolean playerHas(String world, String player, String permission) {
        return plugin.getPermissionsManager().hasPermission(getOfflinePlayer(player).getUniqueId(), permission);
    }

    @Override
    public boolean playerAdd(String world, String player, String permisson) {
        plugin.getPermissionsManager().addPermission(getOfflinePlayer(player).getUniqueId(), permisson);
        return true;
    }

    @Override
    public boolean playerRemove(String world, String player, String permission) {
        plugin.getPermissionsManager().removePermission(getOfflinePlayer(player).getUniqueId(), permission);
        return true;
    }

    @Override
    public boolean groupHas(String world, String group, String permission) {
        if (!plugin.getPermissionsManager().groupExists(group)) return false;
        return plugin.getPermissionsManager().hasPermission(plugin.getPermissionsManager().getGroup(group), false, permission);
    }

    @Override
    public boolean groupAdd(String world, String group, String permission) {
        if (!plugin.getPermissionsManager().groupExists(group)) return false;
        plugin.getPermissionsManager().addPermission(plugin.getPermissionsManager().getGroup(group), permission);
        return false;
    }

    @Override
    public boolean groupRemove(String world, String group, String permission) {
        if (!plugin.getPermissionsManager().groupExists(group)) return false;
        plugin.getPermissionsManager().removePermission(plugin.getPermissionsManager().getGroup(group), permission);
        return false;
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        if (!plugin.getPermissionsManager().groupExists(group)) return false;
        return plugin.getPermissionsManager().playerInGroup(getOfflinePlayer(player).getUniqueId(),
                plugin.getPermissionsManager().getGroup(group), true);
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        if (!plugin.getPermissionsManager().groupExists(group)) return false;
        plugin.getPermissionsManager().addGroup(getOfflinePlayer(player).getUniqueId(),
                plugin.getPermissionsManager().getGroup(group));
        return false;
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        if (!plugin.getPermissionsManager().groupExists(group)) return false;
        plugin.getPermissionsManager().removeGroup(getOfflinePlayer(player).getUniqueId(),
                plugin.getPermissionsManager().getGroup(group));
        return false;
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(player);
        if (offlinePlayer.getPlayer() == null) return new String[0];
        return plugin.getPermissionsManager().getGroups(offlinePlayer.getPlayer()).stream().map(Group::getName).toArray(String[]::new);
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        String[] groups = getPlayerGroups(world, player);
        if(groups.length==0) return "";
        return groups[0];
    }
}