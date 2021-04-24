package xyz.ufactions.upermissions.data;

import com.google.common.collect.Sets;
import xyz.ufactions.prolib.libs.F;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final String name;

    private List<String> permissions = new ArrayList<>();
    private List<String> inheritance = new ArrayList<>();
    private boolean isDefault = false;
    private String prefix = "";
    private int weight = 0;

    public Group(String name) {
        this.name = name;
    }

    // Methods

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public void removePermission(String permission) {
        this.permissions.remove(F.matchCase(Sets.newHashSet(this.permissions), permission));
    }

    public void addInheritance(String child) {
        this.inheritance.add(child);
    }

    public void removeInheritance(String child) {
        this.inheritance.remove(F.matchCase(Sets.newHashSet(this.permissions), child));
    }

    // Setters

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public void setInheritance(List<String> inheritance) {
        this.inheritance = inheritance;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    // Getters

    public String getPrefix() {
        return prefix;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getInheritance() {
        return inheritance;
    }

    public String getName() {
        return name;
    }
}