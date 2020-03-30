package xyz.ufactions.upermissions.hook;

import net.milkbowl.vault.chat.Chat;
import xyz.ufactions.upermissions.Main;

public class UPermissionsVaultChat extends Chat {

    private Main main;

    public UPermissionsVaultChat(Main main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "UPermissions";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPlayerPrefix(String world, String player) {
        return main.getManager().buildPrefix(getPlayer(player));
    }

    @Override
    public void setPlayerPrefix(String world, String player, String prefix) {

    }

    @Override
    public String getPlayerSuffix(String world, String player) {
        return "{TODO}";
    }

    @Override
    public void setPlayerSuffix(String world, String player, String suffix) {

    }

    @Override
    public String getGroupPrefix(String world, String group) {
        return "{TODO}";
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {

    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return "{TODO}";
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {

    }

    @Override
    public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
        return 0;
    }

    @Override
    public void setPlayerInfoInteger(String world, String player, String node, int value) {

    }

    @Override
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
        return 0;
    }

    @Override
    public void setGroupInfoInteger(String world, String group, String node, int value) {

    }

    @Override
    public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
        return 0;
    }

    @Override
    public void setPlayerInfoDouble(String world, String player, String node, double value) {

    }

    @Override
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
        return 0;
    }

    @Override
    public void setGroupInfoDouble(String world, String group, String node, double value) {

    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
        return false;
    }

    @Override
    public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {

    }

    @Override
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
        return false;
    }

    @Override
    public void setGroupInfoBoolean(String world, String group, String node, boolean value) {

    }

    @Override
    public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
        return "{TODO}";
    }

    @Override
    public void setPlayerInfoString(String world, String player, String node, String value) {

    }

    @Override
    public String getGroupInfoString(String world, String group, String node, String defaultValue) {
        return "{TODO}";
    }

    @Override
    public void setGroupInfoString(String world, String group, String node, String value) {

    }
}