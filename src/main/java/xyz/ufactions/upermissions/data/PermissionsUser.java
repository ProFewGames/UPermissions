package xyz.ufactions.upermissions.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionsUser {

    private UUID uuid;
    private List<PermissionsGroup> groups = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private String prefix = "";

    public PermissionsUser(UUID uuid) {
        this.uuid = uuid;
    }

    public String buildPrefix() {
        StringBuilder builder = new StringBuilder();
        for(PermissionsGroup group : groups) {
            // FIXME
        }
        return builder.toString();
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public void setGroups(List<PermissionsGroup> groups) {
        this.groups = groups;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<PermissionsGroup> getGroups() {
        return groups;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "PermissionsUser{" +
                "uuid=" + uuid +
                ", groups=" + groups +
                ", permissions=" + permissions +
                '}';
    }
}