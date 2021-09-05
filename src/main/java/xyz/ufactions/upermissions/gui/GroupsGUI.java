package xyz.ufactions.upermissions.gui;

import xyz.ufactions.prolib.api.Module;
import xyz.ufactions.prolib.gui.ButtonBuilder;
import xyz.ufactions.prolib.gui.GUI;
import xyz.ufactions.prolib.libs.ColorLib;
import xyz.ufactions.upermissions.UPermissions;
import xyz.ufactions.upermissions.data.Group;

public class GroupsGUI extends GUI<Module> {

    public GroupsGUI(UPermissions plugin) {
        super(plugin.getDummy(), "Groups", 54, GUIFiller.NONE);

        for (Group group : plugin.getPermissionsManager().getGroups()) {
            addButton(ButtonBuilder.instance(plugin.getDummy())
                    .item(ColorLib.cw(ColorLib.randomColor())
                            .name(group.getName())
                            .lore("<TODO>"))
                    .onClick((player, type) -> {
                        // TODO
                    })
                    .build());
        }
    }
}