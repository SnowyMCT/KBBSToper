package mc233.fun.kbbstoper;

import mc233.fun.kbbstoper.gui.GUIManager;
import mc233.fun.kbbstoper.sql.SQLManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class KBBSToper extends JavaPlugin {
	private static KBBSToper kbbstoper;
	private Logger logger = Logger.getLogger("KBBSToper");
	public static KBBSToper getInstance() {
		return kbbstoper;
	}

	@Override
	public void onEnable() {
		kbbstoper = this;
		ConfigManager configManager = new ConfigManager(this); // 创建 ConfigManager 实例
		Option.load();
		Message.load(configManager); // 将 ConfigManager 实例传递给 Message 类
		SQLManager.initializeSQLer();
		this.getCommand("kbbstoper").setExecutor(CLI.getInstance());
		this.getCommand("kbbstoper").setTabCompleter(CLI.getInstance());
		new Reminder(this);
		new GUIManager(this);
		SQLManager.startTimingReconnect();
		Util.startAutoReward();
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PAPIExpansion().register();
		}
		int pluginId = 21098; // <-- Replace with the id of your plugin!
		Metrics metrics = new Metrics(this, pluginId);
		logInfo(Message.ENABLE.getString());
		logInfo("------");
		logInfo("插件原作者 R_Josef");
		logInfo("GitHub项目地址 https://github.com/R-Josef/BBSToper");
		logInfo("------");
		logInfo("修改作者 小浩");
		logInfo("项目地址 https://github.com/SnowCherryServer/KBBSToper");
		logInfo("-----");
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(kbbstoper);
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				Util.waitForAllTask();// 此方法会阻塞
				SQLManager.closeSQLer();
				kbbstoper = null;
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void logInfo(String message) {
		logger.info(message);
	}
}
