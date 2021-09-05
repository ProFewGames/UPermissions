package xyz.ufactions.upermissions.gui;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.ClickType;
import xyz.ufactions.prolib.api.Module;
import xyz.ufactions.prolib.gui.ButtonBuilder;
import xyz.ufactions.prolib.gui.GUI;
import xyz.ufactions.prolib.gui.button.Button;
import xyz.ufactions.prolib.libs.*;
import xyz.ufactions.upermissions.UPermissions;

public class UserGUI extends GUI<Module> {

    public UserGUI(UPermissions plugin, OfflinePlayer target) {
        super(plugin.getDummy(), C.mHead + "User Modification " + F.elem(target.getName()), 45, GUIFiller.NONE);

        Button<?> prefixSuffix = ButtonBuilder.instance(plugin.getDummy())
                .slot(24)
                .item(new ItemBuilder(Material.NAME_TAG)
                        .name(C.mBody + "Prefix and Suffix")
                        .lore(F.elem("Left Click") + " to edit prefix", F.elem("Right Click") + " to edit suffix"))
                .onClick((player, type) -> {
                    if (type == ClickType.RIGHT) {
                        UtilPlayer.message(player, F.error(plugin.getName(), "Currently Unavailable"));
                    } else if (type == ClickType.LEFT) {
                        ResponseLib.getString(plugin.getDummy(), player, 60).whenComplete((string, throwable) -> {
                            plugin.getPermissionsManager().setPrefix(target.getUniqueId(), string);
                            UtilPlayer.message(player, F.main(plugin.getName(), F.elem(target.getName()) + "'s prefix changed to " + F.elem(string) + "."));
                        });
                    }
                })
                .build();

        Button<?> viewPermissions = ButtonBuilder.instance(plugin.getDummy())
                .slot(13)
                .item(new ItemBuilder(Material.WRITABLE_BOOK)
                        .name(C.mBody + "View Permissions")
                        .lore(F.elem("Click") + " to view permissions", "", "Permission Nodes:", concatenatePermissions(target)))
                .onClick((player, type) -> {
                    // TODO
                })
                .build();

        addButton(prefixSuffix);
    }

    private String concatenatePermissions(OfflinePlayer player) {
        return "n/a"; // TODO
    }
}