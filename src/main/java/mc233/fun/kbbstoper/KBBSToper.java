package mc233.fun.kbbstoper;

import mc233.fun.kbbstoper.gui.GUIManager;
import mc233.fun.kbbstoper.sql.SQLManager;
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

		// 1. 先加载配置和语言
		ConfigManager configManager = new ConfigManager(this);
		Option.load();
		Message.load(configManager);

		// 2. 初始化数据库访问
		SQLManager.initializeSQLer();

		// 3. 注册命令执行器与补全，用新拆分的 CLI 构造函数
		ConfigManager cfg = new ConfigManager(this);
		CLI cli = new CLI(cfg);
		this.getCommand("kbbstoper").setExecutor(cli);
		this.getCommand("kbbstoper").setTabCompleter(cli);

		// 4. 其它功能初始化
		new Reminder(this);
		new GUIManager(this, cfg, cli);

		SQLManager.startTimingReconnect();
		Util.startAutoReward();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PAPIExpansion().register();
		}

		// 5. bStats 统计
		int pluginId = 21098;
		Metrics metrics = new Metrics(this, pluginId);

		// 6. 启动日志
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
