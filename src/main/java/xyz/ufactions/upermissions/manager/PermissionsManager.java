package xyz.ufactions.upermissions.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.ufactions.prolib.libs.Callback;
import xyz.ufactions.prolib.libs.F;
import xyz.ufactions.prolib.libs.UtilTime;
import xyz.ufactions.upermissions.UPermissions;
import xyz.ufactions.upermissions.data.Group;
import xyz.ufactions.upermissions.data.User;
import xyz.ufactions.upermissions.repository.GroupsRepository;
import xyz.ufactions.upermissions.repository.UsersRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

// XXX - Add Synchronization
public class PermissionsManager {

    private final List<Group> groups = new ArrayList<>();
    private final Map<UUID, User> users = new HashMap<>();
    private final Map<UUID, PermissionAttachment> data = new HashMap<>();

    private final UPermissions plugin;
    private final GroupsRepository groupsRepository;
    private final UsersRepository usersRepository;

    public PermissionsManager(UPermissions plugin) {
        this.plugin = plugin;
        this.groupsRepository = new GroupsRepository(plugin);
        this.usersRepository = new UsersRepository(plugin);

        reload();
    }

    // General - Start

    public void reload() {
        loadGroups().whenComplete((groups1, throwable) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                reload(player);
            }
        });
    }

    // General - End
    // Users - Start

    public void reload(Player player) {
        unload(player);
        load(user -> injectPermissions(player), player.getUniqueId(), true);
    }

    private void load(final Callback<User> callback, final UUID uuid, final boolean cache) {
        new BukkitRunnable() {

            @Override
            public void run() {
                long epoch = System.currentTimeMillis();
                plugin.debug("Loading " + uuid.toString() + ", Caching " + cache + "...");
                User user;
                user = usersRepository.getUser(uuid);
                if (cache)
                    if (!users.containsKey(uuid))
                        users.put(uuid, user);
                final List<String> groupNames = usersRepository.getGroups(uuid);
                final List<Group> groups = new ArrayList<>();
                for (String name : groupNames) {
                    Group group = getGroup(name);
                    if (group == null) {
                        plugin.warning("Attempted to add user \"" + user.getUsername() + "\" to group \"" + name + "\" but it does not exist!");
                        continue;
                    }
                    groups.add(group);
                }
                if (groups.isEmpty()) {
                    plugin.debug("User has no groups");
                    if (getDefaultGroup() != null) {
                        plugin.debug("Added default group");
                        groups.add(getDefaultGroup());
                    } else {
                        plugin.debug("Could not add default group");
                    }
                }
                user.setGroups(groups);
                if (callback != null)
                    callback.run(user);
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) usersRepository.updateUsername(player);
                plugin.debug("Loading of " + uuid.toString() + " took " + UtilTime.convertString(System.currentTimeMillis() - epoch, 3, UtilTime.TimeUnit.FIT) + ".");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void unload(Player player) {
        UUID uuid = player.getUniqueId();
        if (data.containsKey(uuid)) data.get(uuid).remove();
        data.remove(uuid);
        users.remove(uuid);
    }

    public void setPrefix(UUID uuid, String prefix) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) getUser(player).setPrefix(prefix);
        new BukkitRunnable() {

            @Override
            public void run() {
                usersRepository.setPrefix(uuid, prefix);
            }
        }.runTaskAsynchronously(plugin);
    }

    public String buildPrefix(Player player) {
        return getUser(player).buildPrefix();
    }

    public boolean hasPermission(UUID uuid, String permission) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        User user = getUser(player);
        for (String p : user.getPermissions()) {
            if (p.equalsIgnoreCase(permission)) return true;
        }
        for (Group group : user.getGroups()) {
            if (hasPermission(group, true, permission)) return true;
        }
        return false;
    }

    public void addPermission(UUID uuid, String permission) {
        new BukkitRunnable() {

            @Override
            public void run() {
                usersRepository.addPermission(uuid, permission);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null)
                        reload(player);
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removePermission(UUID uuid, String permission) {
        new BukkitRunnable() {

            @Override
            public void run() {
                usersRepository.removePermission(uuid, permission);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null)
                        reload(player);
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    private void injectPermissions(Player player) {
        User user = users.get(player.getUniqueId());
        for (Group group : user.getGroups()) {
            injectPermissions(player, group);
        }
        for (String permission : user.getPermissions()) {
            injectPermission(player, permission);
        }
    }

    private void injectPermissions(Player player, Group group) {
        for (String childName : group.getInheritance()) {
            Group child = getGroup(childName);
            injectPermissions(player, child);
        }
        for (String permission : group.getPermissions()) {
            injectPermission(player, permission);
        }
    }

    private void injectPermission(Player player, String permission) {
        boolean value = !permission.startsWith("-");
        if (!value) permission = permission.substring(1);
        getAttachment(player).setPermission(permission, value);
        plugin.debug((value ? "Injected" : "Revoked") + " " + player.getName() + " with permission " + permission);
    }

    private PermissionAttachment getAttachment(Player player) {
        UUID uuid = player.getUniqueId();
        if (!data.containsKey(uuid)) data.put(uuid, player.addAttachment(plugin));
        return data.get(uuid);
    }

    public User getUser(Player player) {
        if (!users.containsKey(player.getUniqueId()))
            player.kickPlayer(F.error(plugin.getName(), "Failed to fetch user, try connecting again or contact administration."));
        return users.get(player.getUniqueId());
    }

    public void getUser(Callback<User> callback, UUID uuid) {
        if (users.containsKey(uuid)) {
            callback.run(users.get(uuid));
            return;
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                load(callback, uuid, false);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void deleteUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) unload(player);
        new BukkitRunnable() {

            @Override
            public void run() {
                usersRepository.deleteUser(uuid);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) reload(player);
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    public boolean playerInGroup(UUID uuid, Group group, boolean includeChildren) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        for (Group parent : getGroups(player)) {
            if (parent.equals(group)) return true;
            if (includeChildren) {
                for (String childName : parent.getInheritance()) {
                    if (playerInGroup(player.getUniqueId(), getGroup(childName), true)) return true;
                }
            }
        }
        return false;
    }

    public void setGroup(UUID uuid, Group group) {
        new BukkitRunnable() {

            @Override
            public void run() {
                usersRepository.setGroup(uuid, group);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null)
                        reload(player);
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    public void addGroup(UUID uuid, Group group) {
        new BukkitRunnable() {
            @Override
            public void run() {
                usersRepository.addGroup(uuid, group);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null)
                        reload(player);
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removeGroup(UUID uuid, Group group) {
        new BukkitRunnable() {

            @Override
            public void run() {
                usersRepository.removeGroup(uuid, group);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) reload(player);
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    public List<Group> getGroups(Player player) {
        return getUser(player).getGroups();
    }

    // Users - End
    // Groups - Start

    private void reload(Group group) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerInGroup(player.getUniqueId(), group, true)) {
                reload(player);
            }
        }
    }

    private CompletableFuture<List<Group>> loadGroups() {
        CompletableFuture<List<Group>> future = new CompletableFuture<>();
        new BukkitRunnable() {

            @Override
            public void run() {
                plugin.debug("Loading groups...");
                groups.clear();
                groups.addAll(groupsRepository.getGroups());
                plugin.debug("Groups loaded!");
                future.complete(groups);
            }
        }.runTaskAsynchronously(plugin);
        return future;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Group getGroup(String name) {
        for (Group group : groups) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public boolean groupExists(String name) {
        return getGroup(name) != null;
    }

    public void createGroup(String name) {
        if (groupExists(name)) return;
        groups.add(new Group(name));
        new BukkitRunnable() {

            @Override
            public void run() {
                groupsRepository.createGroup(getGroup(name));
            }
        }.runTaskAsynchronously(plugin);
    }

    public void deleteGroup(Group group) {
        groups.remove(group);
        new BukkitRunnable() {

            @Override
            public void run() {
                groupsRepository.deleteGroup(group);
            }
        }.runTaskAsynchronously(plugin);
    }

    public Set<Group> searchGroup(String start) {
        Set<Group> matches = new HashSet<>();
        for (Group group : getGroups()) {
            if (group.getName().toLowerCase().equalsIgnoreCase(start)) return Collections.singleton(group);
            if (group.getName().toLowerCase().startsWith(start.toLowerCase()))
                matches.add(group);
        }
        return matches;
    }

    public boolean isChild(Group parent, Group child) {
        for (String childName : parent.getInheritance()) {
            if (getGroup(childName).equals(child)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(Group group, boolean includeChildren, String permission) {
        for (String p : group.getPermissions()) {
            if (p.equalsIgnoreCase(permission)) return true;
        }
        if (includeChildren) {
            for (String childName : group.getInheritance()) {
                if (hasPermission(getGroup(childName), true, permission)) return true;
            }
        }
        return false;
    }

    public void addPermission(Group group, String permission) {
        group.addPermission(permission);
        reload(group);
        new BukkitRunnable() {

            @Override
            public void run() {
                groupsRepository.addPermission(group, permission);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removePermission(Group group, String permission) {
        group.removePermission(permission);
        reload(group);
        new BukkitRunnable() {

            @Override
            public void run() {
                groupsRepository.removePermission(group, permission);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void setInheritance(Group parent, Group child) {
        for (String childName : parent.getInheritance()) {
            removeInheritance(parent, getGroup(childName));
        }
        addInheritance(parent, child);
    }

    public void addInheritance(Group parent, Group child) {
        parent.addInheritance(child.getName());
        reload(parent);
        new BukkitRunnable() {

            @Override
            public void run() {
                groupsRepository.addInheritance(parent, child);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removeInheritance(Group parent, Group child) {
        parent.removeInheritance(child.getName());
        reload(parent);
        new BukkitRunnable() {
            @Override
            public void run() {
                groupsRepository.removeInheritance(parent, child);
            }
        }.runTaskAsynchronously(plugin);
    }

    public Group getDefaultGroup() {
        for (Group group : getGroups()) {
            if (group.isDefault()) return group;
        }
        return null;
    }

    public void setDefault(Group group, boolean isDefault) {
        group.setDefault(isDefault);
        new BukkitRunnable() {

            @Override
            public void run() {
                groupsRepository.setDefault(group, isDefault);
                Bukkit.getScheduler().runTask(plugin, () -> reload());
            }
        }.runTaskAsynchronously(plugin);
    }

    public void setPrefix(Group group, String prefix) {
        group.setPrefix(prefix);
        new BukkitRunnable(){

            @Override
            public void run() {
                groupsRepository.setPrefix(group, prefix);
            }
        }.runTaskAsynchronously(plugin);
    }

    // Groups - End
}