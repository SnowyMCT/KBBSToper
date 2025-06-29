package mc233.fun.kbbstoper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ConfigManager {
    private JavaPlugin plugin;
    private FileConfiguration configFile, langFile, guiFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.setup();
        setupGuiConfig();// 新增 gui.yml
    }

    private void setupGuiConfig() {
        File f = new File(plugin.getDataFolder(), "gui.yml");
        if (!f.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiFile = YamlConfiguration.loadConfiguration(f);
    }

    private void setup() {
        // config.yml
        File cfg = new File(plugin.getDataFolder(), "config.yml");
        if (!cfg.exists()) {
            plugin.saveResource("config.yml", false);
        }
        configFile = YamlConfiguration.loadConfiguration(cfg);
        updateConfig(cfg, "config");

        // lang.yml
        File lang = new File(plugin.getDataFolder(), "lang.yml");
        if (!lang.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        langFile = YamlConfiguration.loadConfiguration(lang);
        updateConfig(lang, "lang");
    }

    private void updateConfig(File file, String name) {
        String fileVersion = configFile.getString("version");
        String pluginVersion = plugin.getDescription().getVersion();

        if (fileVersion != null && !fileVersion.equals(pluginVersion)) {
            try {
                // 先备份老配置
                Files.copy(
                        file.toPath(),
                        new File(plugin.getDataFolder(), name + "_old.yml").toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
                // 再从 jar 中复制最新的模板出来
                plugin.saveResource(name + ".yml", true);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "更新 " + name + ".yml 时出错", ex);
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
            this.guiFile.save(new File(this.plugin.getDataFolder(), "gui.yml"));
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void reloadConfig() {
        this.configFile = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "config.yml"));
        this.langFile = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "lang.yml"));
        this.guiFile = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "gui.yml"));
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

    public FileConfiguration getGuiConfig() {
        return guiFile;
    }
}
