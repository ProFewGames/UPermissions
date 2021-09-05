package xyz.ufactions.upermissions.gui;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import xyz.ufactions.prolib.api.Module;
import xyz.ufactions.prolib.gui.ButtonBuilder;
import xyz.ufactions.prolib.gui.GUI;
import xyz.ufactions.prolib.libs.ItemBuilder;
import xyz.ufactions.upermissions.UPermissions;

import java.util.ArrayList;
import java.util.List;

public class UsersGUI extends GUI<Module> {

    public UsersGUI(UPermissions plugin) {
        super(plugin.getDummy(), "Users", 54, GUIFiller.NONE);

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (!player.hasPlayedBefore()) continue;
            addButton(ButtonBuilder.instance(plugin.getDummy())
                    .item(new ItemBuilder(player)
                            .name(player.getName())
                            .lore(constructLore(player))
                            .glow(true))
                    .onClick((p, type) -> {
                        new UserGUI(plugin, p).openInventory(p, this);
                    })
                    .build());
        }
    }

    private List<String> constructLore(OfflinePlayer player) {
        List<String> lore = new ArrayList<>();
        return lore;
    }
}