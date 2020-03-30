package xyz.ufactions.upermissions.files;

import xyz.ufactions.api.Module;
import xyz.ufactions.libs.FileManager;

public class ConfigFile extends FileManager {

    public ConfigFile(Module module) {
        super(module, "config.yml");
    }

    public boolean debug() {
        return getBoolean("debug");
    }

    @Override
    public void create() {
        set("debug", false);
        super.create();
    }
}