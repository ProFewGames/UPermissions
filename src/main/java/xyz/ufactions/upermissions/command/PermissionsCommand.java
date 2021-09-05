package xyz.ufactions.upermissions.command;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import xyz.ufactions.prolib.command.CommandBase;
import xyz.ufactions.prolib.libs.C;
import xyz.ufactions.prolib.libs.DummyModule;
import xyz.ufactions.prolib.libs.F;
import xyz.ufactions.prolib.libs.UtilPlayer;
import xyz.ufactions.upermissions.UPermissions;
import xyz.ufactions.upermissions.data.Group;
import xyz.ufactions.upermissions.gui.UPermissionsGUI;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsCommand extends CommandBase<UPermissions> {

    public PermissionsCommand(UPermissions plugin) {
        super(plugin, "upermissions", "uperms", "perms", "pex", "permissions");

        setPermission("upermissions.admin", PermissionDefault.OP); // TODO FIX -> FALSE
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("editor")) {
                if (!isPlayer(sender)) return;
                new UPermissionsGUI(plugin).openInventory((Player) sender);
                return;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                UtilPlayer.message(sender, F.main(plugin.getName(), "Reloading artifacts..."));
                plugin.reload();
                UtilPlayer.message(sender, F.main(plugin.getName(), "Artifacts reloaded!"));
                return;
            }
            if (args[0].equalsIgnoreCase("group") || args[0].equalsIgnoreCase("groups")) {
                UtilPlayer.message(sender, F.line());
                UtilPlayer.message(sender, C.mBody + "Available Groups:");
                for (Group group : plugin.getPermissionsManager().getGroups()) {
                    UtilPlayer.message(sender, " " + group.getName() + " #" + group.getWeight() + " " + group.getInheritance());
                }
                UtilPlayer.message(sender, F.line());
                return;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("group")) {
                Group group = searchGroup(sender, args[1], true);
                if (group == null) return;
                UtilPlayer.message(sender, F.line(C.cWhite + "[" + F.elem(group.getName()) + C.cWhite + "]"));
                UtilPlayer.message(sender, "Inheritance:");
                if (group.getInheritance().isEmpty()) {
                    UtilPlayer.message(sender, "");
                } else {
                    for (String inheritance : group.getInheritance()) {
                        UtilPlayer.message(sender, "* " + inheritance);
                    }
                }
                UtilPlayer.message(sender, "Permissions:");
                if (group.getPermissions().isEmpty()) {
                    UtilPlayer.message(sender, "");
                } else {
                    for (String permission : group.getPermissions()) {
                        UtilPlayer.message(sender, "* " + permission);
                    }
                }
                UtilPlayer.message(sender, "Options:");
                UtilPlayer.message(sender, "default = " + group.isDefault());
                UtilPlayer.message(sender, "weight = " + group.getWeight());
                UtilPlayer.message(sender, "prefix = " + group.getPrefix());
                UtilPlayer.message(sender, F.line());
                return;
            }
            if (args[0].equalsIgnoreCase("user")) {
                UtilPlayer.searchOffline(players -> {
                    if (players.size() != 1) return;
                    OfflinePlayer target = players.get(0);
                    UtilPlayer.message(sender, F.line(C.cWhite + "[" + F.elem(target.getName()) + C.cWhite + "]"));
                    plugin.getPermissionsManager().getUser(user -> {
                        UtilPlayer.message(sender, "Groups:");
                        if (user.getGroups().isEmpty()) {
                            UtilPlayer.message(sender, "");
                        } else {
                            for (Group group : user.getGroups()) {
                                UtilPlayer.message(sender, "* " + group.getName());
                            }
                        }
                        UtilPlayer.message(sender, "Permissions:");
                        if (user.getPermissions().isEmpty()) {
                            UtilPlayer.message(sender, "");
                        } else {
                            for (String permission : user.getPermissions()) {
                                UtilPlayer.message(sender, "* " + permission);
                            }
                        }
                        UtilPlayer.message(sender, "Options:");
                        UtilPlayer.message(sender, "prefix = " + user.getPrefix());
                        UtilPlayer.message(sender, F.line());
                    }, target.getUniqueId());
                }, sender, args[1], true);
                return;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("delete")) {
                    UtilPlayer.searchOffline(offlinePlayers -> {
                        if (offlinePlayers.size() != 1) return;
                        OfflinePlayer target = offlinePlayers.get(0);
                        plugin.getPermissionsManager().deleteUser(target.getUniqueId());
                        UtilPlayer.message(sender, F.main(plugin.getName(), "User " + F.elem(target.getName()) + " deleted!"));
                    }, sender, args[1], true);
                    return;
                }
            }
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("create")) {
                    if (plugin.getPermissionsManager().groupExists(args[1])) {
                        UtilPlayer.message(sender, F.error(plugin.getName(), "Group " + F.elem(args[1]) + C.mError + " already exists."));
                        return;
                    }
                    plugin.getPermissionsManager().createGroup(args[1]);
                    UtilPlayer.message(sender, "Group " + args[1] + " created!");
                    return;
                }
                if (args[2].equalsIgnoreCase("delete")) {
                    if (!plugin.getPermissionsManager().groupExists(args[1])) {
                        UtilPlayer.message(sender, F.error(plugin.getName(), "Group " + args[1] + " does not exist."));
                        return;
                    }
                    plugin.getPermissionsManager().deleteGroup(plugin.getPermissionsManager().getGroup(args[1]));
                    UtilPlayer.message(sender, "Group " + args[1] + " deleted.");
                    return;
                }
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")) {
                Group group = searchGroup(sender, args[1], true);
                if (group == null) return;
                if (args[2].equalsIgnoreCase("default")) {
                    boolean isDefault = args[3].equalsIgnoreCase("true");
                    plugin.getPermissionsManager().setDefault(group, isDefault);
                    UtilPlayer.message(sender, F.main(plugin.getName(), "Set " + F.elem(group.getName()) + " as default to " + F.elem(String.valueOf(isDefault)) + "."));
                    return;
                }
                if (args[2].equalsIgnoreCase("add")) {
                    plugin.getPermissionsManager().addPermission(group, args[3]);
                    UtilPlayer.message(sender, "'" + args[3] + "' added to group '" + group.getName() + "'");
                    return;
                }
                if (args[2].equalsIgnoreCase("remove")) {
                    plugin.getPermissionsManager().removePermission(group, args[3]);
                    UtilPlayer.message(sender, "'" + args[3] + "' removed from group '" + group.getName() + "'");
                    return;
                }
            }
        }
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("child")) {
                    Group parent = searchGroup(sender, args[1], true);
                    Group child = searchGroup(sender, args[4], true);
                    if (parent == null || child == null) return;
                    if (args[3].equalsIgnoreCase("add")) {
                        plugin.getPermissionsManager().addInheritance(parent, child);
                        UtilPlayer.message(sender, F.main(plugin.getName(), F.elem(child.getName()) + " is now a child of " + F.elem(parent.getName()) + "."));
                        return;
                    }
                    if (args[3].equalsIgnoreCase("remove")) {
                        plugin.getPermissionsManager().removeInheritance(parent, child);
                        UtilPlayer.message(sender, F.main(plugin.getName(), F.elem(child.getName()) + " is no longer a child of " + F.elem(parent.getName()) + "."));
                        return;
                    }
                    if (args[3].equalsIgnoreCase("set")) {
                        plugin.getPermissionsManager().setInheritance(parent, child);
                        UtilPlayer.message(sender, F.main(plugin.getName(), F.elem(child.getName()) + " is now the only child of " + F.elem(parent.getName()) + "."));
                        return;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("group")) {
                    if (args[3].equalsIgnoreCase("set")) {
                        Group group = searchGroup(sender, args[4], true);
                        if (group == null) return;
                        UtilPlayer.searchOffline(offlinePlayers -> {
                            if (offlinePlayers.size() != 1) return;
                            OfflinePlayer target = offlinePlayers.get(0);
                            plugin.getPermissionsManager().setGroup(target.getUniqueId(), group);
                            UtilPlayer.message(sender, F.main(plugin.getName(), F.elem(target.getName()) + "'s group has been set to " + F.elem(group.getName()) + "."));
                        }, sender, args[1], true);
                        return;
                    }
                }
            }
        }
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("prefix")) {
                    Group group = searchGroup(sender, args[1], true);
                    if (group == null) return;
                    if (args.length > 3) {
                        String prefix = F.concatenate(3, " ", args);
                        if (prefix.startsWith("\"")) prefix = prefix.substring(1);
                        if (prefix.endsWith("\"")) prefix = prefix.substring(0, prefix.length() - 1);
                        plugin.getPermissionsManager().setPrefix(group, prefix);
                        UtilPlayer.message(sender, F.elem(group.getName()) + "'s prefix has been set to \"" + prefix + ChatColor.RESET + "\"");
                        return;
                    }
                    UtilPlayer.message(sender, F.elem(group.getName()) + "'s prefix is \"" + group.getPrefix() + ChatColor.RESET + "\"");
                }
            }
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("prefix")) {
                    UtilPlayer.searchOffline(offlinePlayers -> {
                        if (offlinePlayers.size() != 1) return;
                        OfflinePlayer target = offlinePlayers.get(0);
                        if (args.length > 3) {
                            String prefix = F.concatenate(3, " ", args);
                            if (prefix.startsWith("\"")) prefix = prefix.substring(1);
                            if (prefix.endsWith("\"")) prefix = prefix.substring(0, prefix.length() - 1);
                            plugin.getPermissionsManager().setPrefix(target.getUniqueId(), prefix);
                            UtilPlayer.message(sender, F.elem(target.getName()) + "'s prefix has been set to \"" + prefix + ChatColor.RESET + "\"");
                            return;
                        }
                        plugin.getPermissionsManager().getUser(user ->
                                        UtilPlayer.message(sender, F.elem(target.getName()) + "'s prefix is \"" + user.getPrefix() + ChatColor.RESET + "\""),
                                target.getUniqueId());
                    }, sender, args[1], true);
                    return;
                }
            }
        }
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " reload", "Reload all plugin artifacts (users, groups, configurations)"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " editor", "UPermissions graphical editor"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group [group]", "View group specific information"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <group> create", "Create group"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <group> delete", "Remove group"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <group> default <true/false>", "Set the group as default"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <group> prefix [prefix...]", "Set the group's prefix"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <group> add <permission>", "Add permission to group"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <group> remove <permission>", "Remove permission from group"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <parent> child set <child>", "Set <parent> to inherit all from <child>"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <parent> child add <child>", "Add <child>'s inheritance to <parent>"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " group <parent> child remove <child>", "Remove <child>'s inheritance from <parent>"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " user <user>", "List user permissions"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " user <user> prefix [prefix...]", "Sets the user's prefix"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " user <user> delete", "Remove <user>"));
        UtilPlayer.message(sender, F.help("/" + AliasUsed + " user <user> group set <group>", "Set <group> for <user>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return getMatches(args[0], Arrays.asList("reload", "group", "user", "editor"));
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("group")) {
                return getMatches(args[1], plugin.getPermissionsManager().getGroups().stream().map(Group::getName).collect(Collectors.toList()));
            }
            if (args[0].equalsIgnoreCase("user")) {
                return getOfflineMatches(args[1]);
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group")) {
                return getMatches(args[2], Arrays.asList("create", "delete", "default", "prefix", "add", "remove", "child"));
            }
            if (args[0].equalsIgnoreCase("user")) {
                return getMatches(args[2], Arrays.asList("prefix", "delete", "group"));
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("default")) {
                    return getMatches(args[3], Arrays.asList("true", "false"));
                }
                if (args[2].equalsIgnoreCase("child")) {
                    return getMatches(args[3], Arrays.asList("remove", "add", "set"));
                }
            }
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("group")) {
                    return getMatches(args[3], Collections.singletonList("set"));
                }
            }
        }
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("child")) {
                    return getMatches(args[4], plugin.getPermissionsManager().getGroups().stream().map(Group::getName).collect(Collectors.toList()));
                }
            }
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("group")) {
                    return getMatches(args[4], plugin.getPermissionsManager().getGroups().stream().map(Group::getName).collect(Collectors.toList()));
                }
            }
        }
        return Collections.emptyList();
    }

    private Group searchGroup(CommandSender sender, String group, boolean inform) {
        Set<Group> matches = plugin.getPermissionsManager().searchGroup(group);
        if (matches.size() == 1) return matches.stream().findFirst().get();
        if (matches.isEmpty()) {
            if (inform)
                UtilPlayer.message(sender, F.error(plugin.getName(), "No available groups for " + F.elem(group) + C.mError + "."));
            return null;
        }
        if (inform)
            UtilPlayer.message(sender, F.main(plugin.getName(), "Available groups for '" + F.elem(group) + "': " + C.cWhite +
                    F.concatenate(", ", matches.stream().map(Group::getName).collect(Collectors.toList()))
            ));
        return null;
    }
}