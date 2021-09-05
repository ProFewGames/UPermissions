package xyz.ufactions.upermissions.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.ufactions.prolib.animation.Animation;
import xyz.ufactions.prolib.gui.ButtonBuilder;
import xyz.ufactions.prolib.gui.GUI;
import xyz.ufactions.prolib.gui.button.Button;
import xyz.ufactions.prolib.libs.DummyModule;
import xyz.ufactions.prolib.libs.ItemBuilder;
import xyz.ufactions.upermissions.UPermissions;

public class UPermissionsGUI extends GUI<DummyModule> {

    private final Animation groupNameAnimation;

    public UPermissionsGUI(UPermissions plugin) {
        super(plugin.getDummy(), plugin.getName(), 27, GUIFiller.NONE);

        Button<?> groups = ButtonBuilder.instance(plugin.getDummy())
                .slot(15)
                .item(new ItemBuilder(Material.BOOKSHELF)
                        .name("GROUPS")
                        .lore(""))
                .onClick((player, type) -> {
                    new GroupsGUI(plugin).openInventory(player, this);
                })
                .build();
        this.groupNameAnimation = new Animation("GROUPS", Animation.AnimationType.WAVE, name -> {
            ItemMeta meta = groups.getItem().getItemMeta();
            meta.setDisplayName(name);
            groups.getItem().setItemMeta(meta);
            Bukkit.getOnlinePlayers().forEach(Player::updateInventory);
        });
        this.groupNameAnimation.start(plugin);
        addButton(groups);
    }

    @Override
    public void onActionPerformed(GUIAction action, Player player) {
        if (action == GUIAction.CLOSE) this.groupNameAnimation.stop();
    }
}