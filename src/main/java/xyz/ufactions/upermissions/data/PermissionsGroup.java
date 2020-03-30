package xyz.ufactions.upermissions.data;

import xyz.ufactions.libs.F;
import xyz.ufactions.upermissions.Main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PermissionsGroup {

    private List<String> permissions = new ArrayList<>();
    private List<String> inheritance = new ArrayList<>();
    private boolean def = false;
    private final String name;
    private int weight;

    public PermissionsGroup(String name) {
        this.name = name;
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(F.matchCase(new HashSet<>(permissions), permission));
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setDefault(boolean def) {
        this.def = def;
    }

    public void removeInheritance(String child) {
        inheritance.remove(F.matchCase(new HashSet<>(inheritance), child));
    }

    public void addInheritance(String child) {
        inheritance.add(child);
    }

    public void setInheritance(List<String> inheritance) {
        this.inheritance = inheritance;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getInheritance() {
        return inheritance;
    }

    public boolean isDefault() {
        return def;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getName() {
        return name;
    }
}