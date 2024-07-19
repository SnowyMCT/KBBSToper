package mc233.fun.kbbstoper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
    private JavaPlugin plugin;
    private FileConfiguration configFile;
    private FileConfiguration langFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.setup();
    }

    private void setup() {
        File config = new File(this.plugin.getDataFolder(), "config.yml");
        if (!config.exists()) {
            this.plugin.saveResource("config.yml", false);
        }
        this.configFile = YamlConfiguration.loadConfiguration(config);
        this.updateConfig(config, "config");


        File lang = new File(this.plugin.getDataFolder(), "lang.yml");
        if (!lang.exists()) {
            this.plugin.saveResource("lang.yml", false);
        }
        this.langFile = YamlConfiguration.loadConfiguration(lang);
        this.updateConfig(lang, "lang");


    }

    private void updateConfig(File file, String name) {
        String version = this.configFile.getString("version");
        if (version == null || !version.equals(this.plugin.getDescription().getVersion())) {
            try {
                Files.copy(file.toPath(), (new File(this.plugin.getDataFolder(), name + "_old.yml")).toPath(), StandardCopyOption.REPLACE_EXISTING);
                this.plugin.saveResource(name + ".yml", true);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

    }

    public FileConfiguration getConfigFile() {
        return this.configFile;
    }

    public FileConfiguration getLangFile() {
        return this.langFile;
    }

    public void saveConfig() {
        try {
            this.configFile.save(new File(this.plugin.getDataFolder(), "config.yml"));
            this.langFile.save(new File(this.plugin.getDataFolder(), "lang.yml"));
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void reloadConfig() {
        this.configFile = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "config.yml"));
        this.langFile = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "lang.yml"));
    }

    public void updateConfigFromJar(String name) {
        File oldConfig = new File(this.plugin.getDataFolder(), name + ".yml");
        File oldConfigRenamed = new File(this.plugin.getDataFolder(), name + "_old.yml");
        if (oldConfigRenamed.exists()) {
            oldConfigRenamed.delete();
        }

        oldConfig.renameTo(oldConfigRenamed);
        this.plugin.saveResource(name + ".yml", false);
    }
}
