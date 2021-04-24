package xyz.ufactions.upermissions.repository;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.ufactions.prolib.database.DBPool;
import xyz.ufactions.prolib.database.RepositoryBase;
import xyz.ufactions.prolib.database.SourceType;
import xyz.ufactions.prolib.database.column.ColumnBoolean;
import xyz.ufactions.prolib.database.column.ColumnInt;
import xyz.ufactions.prolib.database.column.ColumnVarChar;
import xyz.ufactions.upermissions.data.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GroupsRepository extends RepositoryBase {

    public GroupsRepository(JavaPlugin plugin) {
        super(plugin, DBPool.getSource(SourceType.NETWORK));
    }

    public List<Group> getGroups() {
        AtomicReference<List<Group>> groups = new AtomicReference<>(new ArrayList<>());
        String GET = "SELECT * FROM `groups`;";
        executeQuery(GET, resultSet -> {
            while (resultSet.next()) {
                Group group = new Group(resultSet.getString("name"));
                group.setPrefix(resultSet.getString("prefix"));
                group.setDefault(resultSet.getBoolean("default"));
                group.setWeight(resultSet.getInt("weight"));
                group.setPermissions(getGroupPermissions(group.getName()));
                group.setInheritance(getGroupInheritance(group.getName()));
                groups.get().add(group);
            }
        });
        return groups.get();
    }

    private List<String> getGroupPermissions(String name) {
        AtomicReference<List<String>> permissions = new AtomicReference<>(new ArrayList<>());
        String GET = "SELECT * FROM `group_permissions` WHERE `group`=?;";
        executeQuery(GET, resultSet -> {
            while (resultSet.next()) {
                permissions.get().add(resultSet.getString("permission"));
            }
        }, new ColumnVarChar("group", 16, name));
        return permissions.get();
    }

    private List<String> getGroupInheritance(String name) {
        AtomicReference<List<String>> inheritance = new AtomicReference<>(new ArrayList<>());
        String GET = "SELECT * FROM `group_inheritances` WHERE `group`=?;";
        executeQuery(GET, resultSet -> {
            while (resultSet.next()) {
                inheritance.get().add(resultSet.getString("child"));
            }
        }, new ColumnVarChar("group", 16, name));
        return inheritance.get();
    }

    public void createGroup(Group group) {
        String INSERT = "INSERT INTO `groups`(`name`, `prefix`, `default`, `weight`) VALUES (?, ?, ?, ?);";
        executeUpdate(INSERT, new ColumnVarChar("name", 16, group.getName()),
                new ColumnVarChar("prefix", 50, group.getPrefix()),
                new ColumnBoolean("default", group.isDefault()),
                new ColumnInt("weight", group.getWeight()));
        String INSERT_PERMISSION = "INSERT INTO `group_permissions`(`group`, `permission`) VALUES (?, ?);";
        for (String permission : group.getPermissions()) {
            executeUpdate(INSERT_PERMISSION, new ColumnVarChar("group", 16, group.getName()),
                    new ColumnVarChar("permission", 255, permission));
        }
        String INSERT_INHERITANCE = "INSERT INTO `group_inheritances`(`group`, `child`) VALUES (?, ?);";
        for (String inheritance : group.getInheritance()) {
            executeUpdate(INSERT_INHERITANCE, new ColumnVarChar("group", 16, group.getName()),
                    new ColumnVarChar("child", 16, inheritance));
        }
    }

    public void deleteGroup(Group group) {
        String DELETE_GROUP = "DELETE FROM `groups` WHERE `name`=?;";
        executeUpdate(DELETE_GROUP, new ColumnVarChar("name", 16, group.getName()));
        String DELETE_GROUP_PERMISSIONS = "DELETE FROM `group_permissions` WHERE `group`=?;";
        executeUpdate(DELETE_GROUP_PERMISSIONS, new ColumnVarChar("group", 16, group.getName()));
        String DELETE_GROUP_INHERITANCE = "DELETE FROM `group_inheritances` WHERE `group`=? OR `child`=VALUE(`group`);";
        executeUpdate(DELETE_GROUP_INHERITANCE, new ColumnVarChar("group", 16, group.getName()));
    }

    public void addPermission(Group group, String permission) {
        String INSERT = "INSERT INTO `group_permissions`(`group`, `permission`) VALUES (?, ?);";
        executeUpdate(INSERT, new ColumnVarChar("group", 16, group.getName()), new ColumnVarChar("permission", 255, permission));
    }

    public void removePermission(Group group, String permission) {
        String DELETE = "DELETE FROM `group_permissions` WHERE `group`=? AND `permission`=?;";
        executeUpdate(DELETE, new ColumnVarChar("group", 16, group.getName()), new ColumnVarChar("permission", 255, permission));
    }

    public void addInheritance(Group parent, Group child) {
        String INSERT = "INSERT INTO `group_inheritances`(`group`, `child`) VALUES (?, ?);";
        executeUpdate(INSERT, new ColumnVarChar("group", 16, parent.getName()), new ColumnVarChar("child", 16, child.getName()));
    }

    public void removeInheritance(Group parent, Group child) {
        String DELETE = "DELETE FROM `group_inheritances` WHERE `group`=? AND `child`=?";
        executeUpdate(DELETE, new ColumnVarChar("group", 16, parent.getName()), new ColumnVarChar("child", 16, child.getName()));
    }

    public void setDefault(Group group, boolean isDefault) {
        String SELECT = "SELECT * FROM `groups` WHERE `name`=?;";
        executeQuery(SELECT, resultSet -> {
            if (!resultSet.next()) createGroup(group);
        }, new ColumnVarChar("name", 16, group.getName()));
        String UPDATE = "UPDATE `groups` SET `default`=? WHERE `name`=?;";
        executeUpdate(UPDATE, new ColumnBoolean("default", isDefault), new ColumnVarChar("name", 16, group.getName()));
    }

    public void setPrefix(Group group, String prefix) {
        String SELECT = "SELECT * FROM `groups` WHERE `name`=?;";
        executeQuery(SELECT, resultSet -> {
            if (!resultSet.next()) createGroup(group);
        }, new ColumnVarChar("name", 16, group.getName()));
        String UPDATE = "UPDATE `groups` SET `prefix`=? WHERE `name`=?;";
        executeUpdate(UPDATE, new ColumnVarChar("prefix", 50, prefix), new ColumnVarChar("name", 16, group.getName()));
    }

    @Override
    protected void initialize() {
        String CREATE_GROUP = "CREATE TABLE IF NOT EXISTS `groups` (`id` INT AUTO_INCREMENT NOT NULL, `name` VARCHAR(16) NOT NULL, " +
                "`prefix` VARCHAR(50) NOT NULL, `default` BOOLEAN NOT NULL, `weight` INT, PRIMARY KEY(`id`));";
        executeUpdate(CREATE_GROUP);
        String CREATE_GROUP_PERMISSIONS = "CREATE TABLE IF NOT EXISTS `group_permissions` (`id` INT AUTO_INCREMENT NOT NULL, " +
                "`group` VARCHAR(16) NOT NULL, `permission` VARCHAR(255) NOT NULL, PRIMARY KEY(`id`));";
        executeUpdate(CREATE_GROUP_PERMISSIONS);
        String CREATE_GROUP_INHERITANCE = "CREATE TABLE IF NOT EXISTS `group_inheritances` (`id` INT AUTO_INCREMENT NOT NULL, " +
                "`group` VARCHAR(16) NOT NULL, `child` VARCHAR(16) NOT NULL, PRIMARY KEY(`id`));";
        executeUpdate(CREATE_GROUP_INHERITANCE);
    }
}