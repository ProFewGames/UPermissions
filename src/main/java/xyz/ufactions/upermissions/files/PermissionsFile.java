package xyz.ufactions.upermissions.files;

import xyz.ufactions.api.Module;
import xyz.ufactions.libs.F;
import xyz.ufactions.libs.FileManager;
import xyz.ufactions.upermissions.data.PermissionsGroup;
import xyz.ufactions.upermissions.data.PermissionsUser;

import java.util.*;

public class PermissionsFile extends FileManager {

    public PermissionsFile(Module module) {
        super(module, "groups.yml");
    }

    // Users start

    public PermissionsUser getUser(UUID uuid) {
        PermissionsUser user = new PermissionsUser(uuid);
        if (contains("users." + uuid.toString() + ".groups")) user.setGroups(getStringList("users." + uuid.toString() + ".groups"));
        return user;
    }

    public void setGroup(UUID uuid, PermissionsGroup group) {
        set("users." + uuid.toString() + ".groups", Arrays.asList(group.getName()));
        save();
    }

    public void deleteUser(UUID uuid) {
        set("users." + uuid.toString(), null);
        save();
    }

    // Users end

    // Groups start

    public List<PermissionsGroup> getGroups() {
        List<PermissionsGroup> list = new ArrayList<>();
        for (String g : getConfigurationSection("groups").getKeys(false)) {
            String path = "groups." + g;
            PermissionsGroup group = new PermissionsGroup(g);
            group.setPermissions(getStringList(path + ".permissions"));
            if (contains(path + ".weight")) group.setWeight(getInt(path + ".weight"));
            if (contains(path + ".default")) group.setDefault(getBoolean(path + ".default"));
            if (contains(path + ".inheritance")) group.setInheritance(getStringList(path + ".inheritance"));
            list.add(group);
        }
        return list;
    }

    public void removeInheritance(String parent, String child) {
        List<String> inheritance = getInheritance(parent);
        child = F.matchCase(new HashSet<>(inheritance), child);
        inheritance.remove(child);
        set("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), parent) + ".inheritance", inheritance);
        save();
    }

    public void addInheritance(String parent, String child) {
        List<String> inheritance = getInheritance(parent);
        inheritance.add(child);
        set("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), parent) + ".inheritance", inheritance);
        save();
    }

    public void setInheritance(String parent, String child) {
        set("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), parent) + ".inheritance", Arrays.asList(child));
        save();
    }

    public List<String> getInheritance(String group) {
        List<String> inheritance = getStringList("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), group) + ".inheritance");
        if (inheritance == null) inheritance = new ArrayList<>();
        return inheritance;
    }

    public void addPermission(String group, String permission) {
        List<String> permissions = getPermissions(group);
        permissions.add(permission);
        set("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), group) + ".permissions", permissions);
        save();
    }

    public void removePermission(String group, String permission) {
        List<String> permissions = getPermissions(group);
        permission = F.matchCase(new HashSet<>(permissions), permission);
        permissions.remove(permission);
        set("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), group) + ".permissions", permissions);
        save();
    }

    public List<String> getPermissions(String group) {
        List<String> permissions = getStringList("groups." + F.matchCase(getConfigurationSection("groups").getKeys(false), group) + ".permissions");
        if (permissions == null) permissions = new ArrayList<>();
        return permissions;
    }

    public void saveGroup(PermissionsGroup group) {
        String path = "groups." + group.getName();
        if (!group.isDefault()) set(path + ".default", group.isDefault());
        if (group.getWeight() != 0) set(path + ".weight", group.getWeight());
        if (!group.getInheritance().isEmpty()) set(path + ".inheritance", group.getInheritance());
        set(path + ".permissions", group.getPermissions());
        save();
    }

    public void deleteGroup(String group) {
        group = F.matchCase(getConfigurationSection("groups").getKeys(false), group);
        set("groups." + group, null);
        save();
    }

    // Groups end

    @Override
    public void create() {
        set("groups.default.default", true);
        set("groups.default.permissions", Arrays.asList("essentials.spawn"));
        super.create();
    }
}