package mc233.fun.kbbstoper;

import mc233.fun.kbbstoper.gui.GUIManager;
import mc233.fun.kbbstoper.sql.SQLManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import mc233.fun.kbbstoper.*;

public class KBBSToper extends JavaPlugin {
	private static KBBSToper kbbstoper;

	public static KBBSToper getInstance() {
		return kbbstoper;
	}

	@Override
	public void onEnable() {
		kbbstoper = this;
		this.saveDefaultConfig();
		Option.load();
		Message.saveDefaultConfig();
		Message.load();
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
		this.getLogger().info(Message.ENABLE.getString());
		this.getLogger().info("------");
		this.getLogger().info("插件原作者 R_Josef");
		getLogger().info("GitHub项目地址 https://github.com/R-Josef/BBSToper");
		getLogger().info("------");
		getLogger().info("修改作者 小浩");
		getLogger().info("项目地址 https://github.com/SnowCherryServer/KBBSToper");
		getLogger().info("-----");
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

}
