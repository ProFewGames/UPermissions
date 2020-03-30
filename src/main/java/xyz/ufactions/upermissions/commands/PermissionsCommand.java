package xyz.ufactions.upermissions.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.ufactions.libs.C;
import xyz.ufactions.libs.F;
import xyz.ufactions.libs.UtilPlayer;
import xyz.ufactions.upermissions.Main;
import xyz.ufactions.upermissions.data.PermissionsGroup;

import java.util.List;

public class PermissionsCommand implements CommandExecutor {

    private Main main;

    public PermissionsCommand(Main main) {
        this.main = main;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                UtilPlayer.message(sender, F.main("UPermissions", "Reloading artifacts..."));
                main.reload();
                UtilPlayer.message(sender, F.main("UPermissions", "Artifacts reloaded!"));
                return true;
            }
            if (args[0].equalsIgnoreCase("group") || args[0].equalsIgnoreCase("groups")) {
                UtilPlayer.message(sender, C.mHead + C.Strike + "------------------------------");
                UtilPlayer.message(sender, C.mBody + "Available groups:");
                for (PermissionsGroup group : main.getManager().getGroups()) {
                    UtilPlayer.message(sender, " " + group.getName() + " #" + group.getWeight() + " " + group.getInheritance());
                }
                UtilPlayer.message(sender, C.mHead + C.Strike + "------------------------------");
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("group")) {
                PermissionsGroup group = matchGroup(sender, args[1], true);
                if (group != null) {
                    UtilPlayer.message(sender, C.mHead + C.Strike + "---------------" + C.cWhite + "[" + F.elem(group.getName()) + C.cWhite + "]" + C.mHead + C.Strike + "---------------");
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
                    UtilPlayer.message(sender, C.mHead + C.Strike + "------------------------------");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("user")) {
                UtilPlayer.searchOffline(offlinePlayers -> {
                    if (offlinePlayers.size() == 1) {
                        OfflinePlayer target = offlinePlayers.get(0);
                        UtilPlayer.message(sender, C.mHead + C.Strike + "---------------" + C.cWhite + "[" + F.elem(target.getName()) + C.cWhite + "]" + C.mHead + C.Strike + "---------------");
                        main.getManager().getUser(user -> {
                            UtilPlayer.message(sender, "Groups:");
                            for (String group : user.getGroups()) {
                                UtilPlayer.message(sender, "* " + group);
                            }
                            UtilPlayer.message(sender, "Permissions:");
                            if (user.getPermissions().isEmpty()) {
                                UtilPlayer.message(sender, "");
                            } else {
                                for (String permission : user.getPermissions()) {
                                    UtilPlayer.message(sender, "* " + permission);
                                }
                            }
                            UtilPlayer.message(sender, C.mHead + C.Strike + "------------------------------");
                        }, target.getUniqueId());
                    }
                }, sender, args[1], true);
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("delete")) {
                    UtilPlayer.searchOffline(offlinePlayers -> {
                        if (offlinePlayers.size() == 1) {
                            OfflinePlayer target = offlinePlayers
                                    .get(0);
                            main.getManager().deleteUser(target.getUniqueId());
                            UtilPlayer.message(sender, F.main("UPermissions", "User " + F.elem(target.getName()) + " deleted!"));
                        }
                    }, sender, args[1], true);
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("create")) {
                    if (main.getManager().getGroup(args[1]) != null) {
                        UtilPlayer.message(sender, F.error("UPermissions", "Group " + F.elem(args[1]) + C.cRed + " already exists."));
                        return true;
                    }
                    main.getManager().createGroup(args[1]);
                    UtilPlayer.message(sender, "Group " + args[1] + " created!");
                    return true;
                }
                if (args[2].equalsIgnoreCase("delete")) {
                    PermissionsGroup group = matchGroup(sender, args[1], true);
                    if (group != null) {
                        main.getManager().deleteGroup(group);
                        UtilPlayer.message(sender, "Group " + group.getName() + " deleted!");
                    }
                    return true;
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("add")) {
                    PermissionsGroup group = matchGroup(sender, args[1], true);
                    if (group != null) {
                        String permission = args[3];
                        main.getManager().addPermission(group, permission);
                        UtilPlayer.message(sender, "'" + permission + "' added to group '" + group.getName() + "'");
                    }
                    return true;
                }
                if (args[2].equalsIgnoreCase("remove")) {
                    PermissionsGroup group = matchGroup(sender, args[1], true);
                    if (group != null) {
                        String permission = args[3];
                        main.getManager().removePermission(group, permission);
                        UtilPlayer.message(sender, "'" + permission + "' removed from group '" + group.getName() + "'");
                    }
                    return true;
                }
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("child")) {
                    PermissionsGroup parent = matchGroup(sender, args[1], true);
                    PermissionsGroup child = matchGroup(sender, args[4], true);
                    if (args[3].equalsIgnoreCase("remove")) {
                        if (parent != null && child != null) {
                            main.getManager().removeInheritance(parent, child);
                            UtilPlayer.message(sender, F.main("UPermissions", F.elem(child.getName()) + " is no longer a child of " + F.elem(parent.getName()) + "."));
                        }
                        return true;
                    }
                    if (args[3].equalsIgnoreCase("add")) {
                        if (parent != null && child != null) {
                            main.getManager().addInheritance(parent, child);
                            UtilPlayer.message(sender, F.main("UPermissions", F.elem(child.getName()) + " is now a child of " + F.elem(parent.getName()) + "."));
                        }
                        return true;
                    }
                    if (args[3].equalsIgnoreCase("set")) {
                        if (parent != null && child != null) {
                            main.getManager().setInheritance(parent, child);
                            UtilPlayer.message(sender, F.main("UPermissions", F.elem(child.getName()) + " is now a child of " + F.elem(parent.getName()) + "."));
                        }
                        return true;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("user")) {
                if (args[2].equalsIgnoreCase("group")) {
                    if (args[3].equalsIgnoreCase("set")) {
                        PermissionsGroup group = matchGroup(sender, args[4], true);
                        if (group != null) {
                            UtilPlayer.searchOffline(offlinePlayers -> {
                                if (offlinePlayers.size() == 1) {
                                    OfflinePlayer target = offlinePlayers.get(0);
                                    main.getManager().setGroup(target.getUniqueId(), group);
                                    UtilPlayer.message(sender, F.main("UPermissions", target.getName() + "'s group has been set to " + group.getName() + "."));
                                }
                            }, sender, args[1], true);
                        }
                        return true;
                    }
                }
            }
        }
        UtilPlayer.message(sender, F.help("/" + label + " reload", "Reload all plugin artifacts (users, groups, configurations)"));
        UtilPlayer.message(sender, F.help("/" + label + " groups", "View all groups"));
        UtilPlayer.message(sender, F.help("/" + label + " group <group>", "View group specific information"));
        UtilPlayer.message(sender, F.help("/" + label + " group <group> create", "Create <group>"));
        UtilPlayer.message(sender, F.help("/" + label + " group <group> delete", "Remove <group>"));
        UtilPlayer.message(sender, F.help("/" + label + " group <group> add <permission>", "Add <permission> to <group>"));
        UtilPlayer.message(sender, F.help("/" + label + " group <group> remove <permission>", "Remove <permission> from <group>"));
        UtilPlayer.message(sender, F.help("/" + label + " group <parent> child set <child>", "Set <parent> to inherit all from <child>"));
        UtilPlayer.message(sender, F.help("/" + label + " group <parent> child add <child>", "Add <child>'s inheritance to <parent>"));
        UtilPlayer.message(sender, F.help("/" + label + " group <parent> child remove <child>", "Remove <child>'s inheritance from <parent>"));
        UtilPlayer.message(sender, F.help("/" + label + " user <user>", "List user permissions"));
        UtilPlayer.message(sender, F.help("/" + label + " user <user> delete", "Remove <user>"));
        UtilPlayer.message(sender, F.help("/" + label + " user <user> group set <group>", "Set <group> for <user>"));
        return true;
    }

    private PermissionsGroup matchGroup(CommandSender sender, String group, boolean inform) {
        List<PermissionsGroup> matches = main.getManager().searchGroup(group);
        if (matches.isEmpty()) {
            UtilPlayer.message(sender, F.error("UPermissions", "No available groups for " + F.elem(group) + C.cRed + "."));
            return null;
        } else if (matches.size() == 1) {
            return matches.get(0);
        } else {
            if (inform) {
                String[] array = new String[matches.size()];
                for (int i = 0; i < matches.size(); i++) {
                    array[i] = matches.get(i).getName();
                }
                UtilPlayer.message(sender, F.main("UPermissions", "Available groups for '" + F.elem(group) + "': " + C.cWhite + F.concatenate(", ", array)));
            }
            return null;
        }
    }
}