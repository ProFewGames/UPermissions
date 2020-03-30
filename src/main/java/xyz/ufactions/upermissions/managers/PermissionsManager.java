package xyz.ufactions.upermissions.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import xyz.ufactions.api.Module;
import xyz.ufactions.libs.Callback;
import xyz.ufactions.libs.UtilTime;
import xyz.ufactions.upermissions.Main;
import xyz.ufactions.upermissions.data.PermissionsGroup;
import xyz.ufactions.upermissions.data.PermissionsUser;
import xyz.ufactions.upermissions.events.PlayerPermissionsLoginEvent;
import xyz.ufactions.upermissions.files.PermissionsFile;

import java.util.*;

public class PermissionsManager {

    private List<PermissionsGroup> groups = new ArrayList<>();
    private HashMap<UUID, PermissionsUser> users = new HashMap<>();
    private HashMap<UUID, PermissionAttachment> data = new HashMap<>();

    private Module module;
    private PermissionsFile file;

    public PermissionsManager(Module module) {
        this.module = module;
        this.file = new PermissionsFile(module);
    }

    // General start

    public void reload() {
        groups = new ArrayList<>();
        file.reload();
        loadGroups();
        for (Player player : Bukkit.getOnlinePlayers()) {
            unload(player);
            login(player.getUniqueId());
        }
    }

    public void reload(Player player) {
        Main.debug("Reloading " + player.getName() + "...");
        unload(player);
        login(player.getUniqueId());
        Main.debug(player.getName() + " reloaded!");
    }

    public void login(UUID uuid) {
        long epoch = System.currentTimeMillis();
        Main.debug(uuid.toString() + " logging in...");
        PermissionsUser user = loadUser(uuid);
        module.getPluginManager().callEvent(new PlayerPermissionsLoginEvent(uuid, user));
        Main.debug(uuid.toString() + " logged in!");
        Main.debug("login for user : " + uuid.toString() + " : took : " + UtilTime.convertString(System.currentTimeMillis() - epoch, 1, UtilTime.TimeUnit.FIT) + ".");
    }

    public void unload(Player player) {
        UUID uuid = player.getUniqueId();
        if (data.containsKey(uuid)) data.get(uuid).remove();
        data.remove(uuid);
        users.remove(uuid);
    }

    public String buildPrefix(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(!player.isOnline()) {
            throw new UnsupportedOperationException("OfflinePlayer prefix building unsupported!");
        } else {
            String prefix = getUser(player.getUniqueId()).buildPrefix();
        }
    }

    public final void injectPermissions(Player player) {
        long epoch = System.currentTimeMillis();
        PermissionsUser user = users.get(player.getUniqueId());
        Main.debug("Injecting : " + player.getName() + " : with group permissions");
        for (String group : user.getGroups()) {
            Main.debug("[LOGIN] concatenating : " + group);
            concatenateGroupInjection(player, getGroup(group));
        }
        Main.debug("Injecting : " + player.getName() + " : with personal permissions");
        for (String permission : user.getPermissions()) {
            inject(player, permission);
        }
        Main.debug("[LOGIN] DONE INJECTING PERMISSIONS");
        Main.debug("total injection for : " + player.getName() + " : took : " + UtilTime.convertString(System.currentTimeMillis() - epoch, 1, UtilTime.TimeUnit.FIT) + ".");
    }

    private final PermissionAttachment getAttachment(Player player) {
        UUID uuid = player.getUniqueId();
        if (!data.containsKey(uuid)) {
            Main.debug("Added new PermissionAttachment for : " + uuid);
            data.put(uuid, player.addAttachment(module.getPlugin()));
        }
        Main.debug("Fetched attachment for : " + uuid + " : " + data.get(uuid));
        return data.get(uuid);
    }

    private final void concatenateGroupInjection(Player player, PermissionsGroup group) {
        Main.debug("Concatenating group : " + group.getName());
        for (String childName : group.getInheritance()) {
            Main.debug("Concatenating child : " + childName + " :...");
            PermissionsGroup child = getGroup(childName);
            concatenateGroupInjection(player, child);
        }
        Main.debug("Injecting permissions from group : " + group.getName() + " : to : " + player.getName());
        for (String permission : group.getPermissions()) {
            inject(player, permission);
        }
    }

    private final void inject(Player player, String permission) {
        Main.debug("Injecting : " + player.getName() + " : with permission : " + permission);
        boolean value = !permission.startsWith("-");
        Main.debug("Permission value : " + value);
        if (!value) {
            Main.debug("Contains negate character - : " + permission);
            permission = permission.substring(1);
            Main.debug("Negated character from string : " + permission);
        }
        getAttachment(player).setPermission(permission, value);
        Main.debug("Set permission : " + permission + " : to : " + value + " : from user : " + player.getName());
    }

    // General end

    // User start

    private PermissionsUser loadUser(UUID uuid) {
        Main.debug("Fetching : " + uuid.toString());
        if (!users.containsKey(uuid)) {
            Main.debug("Adding new user");
            users.put(uuid, file.getUser(uuid));
        }
        Main.debug(uuid.toString() + " : fetched and loaded!");
        return users.get(uuid);
    }

    public PermissionsUser getUser(UUID uuid) {
        return users.get(uuid);
    }

    public void getUser(Callback<PermissionsUser> callback, UUID uuid) {
        if (users.containsKey(uuid)) {
            Main.debug("Returning user from map : " + uuid);
            callback.run(users.get(uuid));
        } else {
            Main.debug("Returning user from file : " + uuid);
            callback.run(file.getUser(uuid));
        }
    }

    public void deleteUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) unload(player);
        file.deleteUser(uuid);
        if (player != null) login(player.getUniqueId());
    }

    public void setGroup(UUID uuid, PermissionsGroup group) {
        file.setGroup(uuid, group);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            unload(player);
            login(uuid);
        }
    }

    public PermissionsUser getGroup(UUID uuid) {
        PermissionsUser user = users.get(uuid);
        Main.debug("Fetched user for : " + uuid.toString() + " : " + user);
        return user;
    }

    public boolean isInGroup(UUID uuid, PermissionsGroup group, boolean includeChildren) {
        boolean val = false;
        for (String g : getGroup(uuid).getGroups()) {
            PermissionsGroup parent = getGroup(g);
            if (parent.getName().equalsIgnoreCase(group.getName())) {
                val = true;
                break;
            }
            if (includeChildren) {
                for (String childName : parent.getInheritance()) {
                    PermissionsGroup child = getGroup(childName);
                    val = isChild(parent, child);
                    if (val) break;
                }
            }
        }
        return val;
    }

    // User end

    // Groups start

    public void loadGroups() {
        groups = file.getGroups();
    }

    public void removeInheritance(PermissionsGroup parent, PermissionsGroup child) {
        parent.removeInheritance(child.getName());
        file.removeInheritance(parent.getName(), child.getName());
    }

    public void addInheritance(PermissionsGroup parent, PermissionsGroup child) {
        parent.addInheritance(child.getName());
        file.addInheritance(parent.getName(), child.getName());
    }

    public void setInheritance(PermissionsGroup parent, PermissionsGroup child) {
        parent.setInheritance(Arrays.asList(child.getName()));
        file.setInheritance(parent.getName(), child.getName());
    }

    public void addPermission(PermissionsGroup group, String permission) {
        group.addPermission(permission);
        file.addPermission(group.getName(), permission);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInGroup(player.getUniqueId(), group, true)) {
                reload(player);
            }
        }
    }

    public void removePermission(PermissionsGroup group, String permission) {
        group.removePermission(permission);
        file.removePermission(group.getName(), permission);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInGroup(player.getUniqueId(), group, true)) {
                reload(player);
            }
        }
    }

    public void createGroup(String group) {
        if (getGroup(group) != null) return;
        PermissionsGroup g = new PermissionsGroup(group);
        file.saveGroup(g);
        groups.add(g);
    }

    public void deleteGroup(PermissionsGroup group) {
        file.deleteGroup(group.getName());
        groups.remove(group);
    }

    public boolean isChild(PermissionsGroup parent, PermissionsGroup child) {
        for (String childName : parent.getInheritance())
            if (childName.equalsIgnoreCase(child.getName())) return true;
        return false;
    }

    public PermissionsGroup getGroup(String group) {
        for (PermissionsGroup g : groups) {
            if (g.getName().equalsIgnoreCase(group)) {
                return g;
            }
        }
        return null;
    }

    public List<PermissionsGroup> searchGroup(String start) {
        List<PermissionsGroup> matches = new ArrayList<>();
        for (PermissionsGroup group : getGroups()) {
            if (group.getName().toLowerCase().equalsIgnoreCase(start)) return Arrays.asList(group);
            if (group.getName().toLowerCase().startsWith(start.toLowerCase())) {
                matches.add(group);
            }
        }
        return matches;
    }

    public List<PermissionsGroup> getGroups() {
        return groups;
    }

    // Groups end

}