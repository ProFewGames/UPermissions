package xyz.ufactions.upermissions.repository;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.ufactions.prolib.database.DBPool;
import xyz.ufactions.prolib.database.RepositoryBase;
import xyz.ufactions.prolib.database.SourceType;
import xyz.ufactions.prolib.database.column.ColumnVarChar;
import xyz.ufactions.upermissions.data.Group;
import xyz.ufactions.upermissions.data.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class UsersRepository extends RepositoryBase {

    public UsersRepository(JavaPlugin plugin) {
        super(plugin, DBPool.getSource(SourceType.NETWORK));
    }

    public User getUser(UUID uuid) {
        AtomicReference<User> user = new AtomicReference<>();
        String GET = "SELECT * FROM `users` WHERE `uuid`=?;";
        executeQuery(GET, resultSet -> {
            resultSet.next();
            User u = new User(uuid, resultSet.getString("username"));
            u.setPrefix(resultSet.getString("prefix"));
            u.setPermissions(getPermissions(uuid));
            user.set(u);
        }, new ColumnVarChar("uuid", 36, uuid.toString()));
        if (user.get() == null) {
            String INSERT = "INSERT INTO `users`(`uuid`, `username`) VALUES (?, ?);";
            executeInsert(INSERT, resultSet -> {
                        User u = new User(uuid, resultSet.getString("username"));
                        user.set(u);
                    }, new ColumnVarChar("uuid", 36, uuid.toString()),
                    new ColumnVarChar("username", 16, Bukkit.getPlayer(uuid) == null ? "" : Bukkit.getPlayer(uuid).getName()));
        }
        return user.get();
    }

    public List<String> getGroups(UUID uuid) {
        AtomicReference<List<String>> groups = new AtomicReference<>(new ArrayList<>());
        String GET = "SELECT * FROM `user_groups` WHERE `user`=?;";
        executeQuery(GET, resultSet -> {
            while (resultSet.next()) {
                groups.get().add(resultSet.getString("group"));
            }
        }, new ColumnVarChar("user", 36, uuid.toString()));
        return groups.get();
    }

    public void addPermission(UUID uuid, String permission) {
        String INSERT = "INSERT INTO `user_permissions`(`user`, `permission`) VALUES (?, ?);";
        executeUpdate(INSERT, new ColumnVarChar("uuid", 36, uuid.toString()), new ColumnVarChar("permission", 255, permission));
    }

    public void removePermission(UUID uuid, String permission) {
        String DELETE = "DELETE FROM `user_permissions` WHERE `user`=? AND `permission`=?;";
        executeUpdate(DELETE, new ColumnVarChar("user", 36, uuid.toString()), new ColumnVarChar("permission", 255, permission));
    }

    private List<String> getPermissions(UUID uuid) {
        AtomicReference<List<String>> permissions = new AtomicReference<>(new ArrayList<>());
        String GET = "SELECT * FROM `user_permissions` WHERE `user`=?;";
        executeQuery(GET, resultSet -> {
            while (resultSet.next()) {
                permissions.get().add(resultSet.getString("permission"));
            }
        }, new ColumnVarChar("user", 36, uuid.toString()));
        return permissions.get();
    }

    public void deleteUser(UUID uuid) {
        String DELETE_USER = "DELETE FROM `users` WHERE `uuid`=?;";
        executeUpdate(DELETE_USER, new ColumnVarChar("uuid", 36, uuid.toString()));
        String DELETE_USER_GROUPS = "DELETE FROM `user_groups` WHERE `user`=?;";
        executeUpdate(DELETE_USER_GROUPS, new ColumnVarChar("user", 36, uuid.toString()));
        String DELETE_USER_PERMISSIONS = "DELETE FROM `user_permissions` WHERE `user`=?;";
        executeUpdate(DELETE_USER_PERMISSIONS, new ColumnVarChar("user", 36, uuid.toString()));
    }

    public void setGroup(UUID uuid, Group group) {
        String DELETE = "DELETE FROM `user_groups` WHERE `user`=?;";
        executeUpdate(DELETE, new ColumnVarChar("user", 36, uuid.toString()));
        addGroup(uuid, group);
    }

    public void addGroup(UUID uuid, Group group) {
        String INSERT = "INSERT INTO `user_groups`(`user`, `group`) VALUES (?, ?);";
        executeUpdate(INSERT, new ColumnVarChar("user", 36, uuid.toString()), new ColumnVarChar("group", 16, group.getName()));
    }

    public void removeGroup(UUID uuid, Group group) {
        String DELETE = "DELETE FROM `user_groups` WHERE `user`=? AND `group`=?;";
        executeUpdate(DELETE, new ColumnVarChar("user", 36, uuid.toString()), new ColumnVarChar("group", 16, group.getName()));
    }

    public void setPrefix(UUID uuid, String prefix) {
        String INSERT_UPDATE = "INSERT INTO `users`(`uuid`, `username`, `prefix`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=VALUE(`prefix`);";
        executeUpdate(INSERT_UPDATE, new ColumnVarChar("uuid", 36, uuid.toString()),
                new ColumnVarChar("username", 16, Bukkit.getPlayer(uuid) == null ? "" : Bukkit.getPlayer(uuid).getName()),
                new ColumnVarChar("prefix", 32, prefix));
    }

    public void updateUsername(Player player) {
        String INSERT_UPDATE = "INSERT INTO `users`(`uuid`, `username`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `username`=VALUE(`username`);";
        executeUpdate(INSERT_UPDATE, new ColumnVarChar("uuid", 36, player.getUniqueId().toString()),
                new ColumnVarChar("username", 16, player.getName()));
    }

    @Override
    protected void initialize() {
        String CREATE_USER = "CREATE TABLE IF NOT EXISTS `users` (`uuid` VARCHAR(36) NOT NULL, `username` VARCHAR(16) NOT NULL, " +
                "`prefix` VARCHAR(32), PRIMARY KEY(`uuid`));";
        executeUpdate(CREATE_USER);
        String CREATE_USER_GROUPS = "CREATE TABLE IF NOT EXISTS `user_groups` (`id` INT AUTO_INCREMENT NOT NULL, " +
                "`user` VARCHAR(36) NOT NULL, `group` VARCHAR(16) NOT NULL, PRIMARY KEY(`id`));";
        executeUpdate(CREATE_USER_GROUPS);
        String CREATE_USER_PERMISSIONS = "CREATE TABLE IF NOT EXISTS `user_permissions` (`id` INT AUTO_INCREMENT NOT NULL, " +
                "`user` VARCHAR(36) NOT NULL, `permission` VARCHAR(255) NOT NULL, PRIMARY KEY(`id`));";
        executeUpdate(CREATE_USER_PERMISSIONS);
    }
}