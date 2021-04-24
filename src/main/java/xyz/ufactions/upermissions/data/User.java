package xyz.ufactions.upermissions.data;

import com.google.common.collect.Sets;
import xyz.ufactions.prolib.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private final String username;

    private String prefix;
    private List<Group> groups = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    public User(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    // Methods

    public String buildPrefix() {
        StringBuilder builder = new StringBuilder();
        for (Group group : groups) {
            builder.append(group.getPrefix());
        }
        if (prefix != null) builder.append(prefix);
        return builder.toString();
    }

    public void addPermission(String permission) {
        if (permissions.contains(F.matchCase(Sets.newHashSet(permissions), permission))) return;
        permissions.add(permission);
    }

    //Setters

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    //Getters

    public String getPrefix() {
        return prefix;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }
}