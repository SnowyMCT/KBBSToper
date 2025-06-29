package mc233.fun.kbbstoper;

import mc233.fun.kbbstoper.commands.*;
import mc233.fun.kbbstoper.gui.GUI;
import mc233.fun.kbbstoper.sql.SQLer;
import mc233.fun.kbbstoper.sql.SQLManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CLI implements TabExecutor {
	private final ConfigManager configManager;
	private SQLer sql;
	private final Map<String, String> cache = new HashMap<>();       // 绑定临时缓存
	private final Map<UUID, Long> queryrecord = new HashMap<>();     // 查询冷却
	private final Map<String, CommandHandler> handlers = new HashMap<>();
	private static CLI instance;

	/** 暴露给外部使用的缓存 Map */
	public Map<String,String> getCache() {
		return cache;
	}

	public static CLI getInstance() {
		return instance;
	}

	/** 供 SQLManager.initializeSQLer 调用，注入最新 sqler */
	public static void setSQLer(SQLer sql) {
		if (instance != null) {
			instance.sql = sql;
		}
	}

	public CLI(ConfigManager configManager) {
		instance = this;
		this.configManager = configManager;
		this.sql = SQLManager.getSQLer();

		handlers.put("help",      new HelpCommandHandler());
		handlers.put("binding",   new BindingCommandHandler(sql, cache));
		handlers.put("reward",    new RewardCommandHandler(sql, queryrecord));
		handlers.put("testreward",new TestRewardCommandHandler());
		handlers.put("list",      new ListCommandHandler());
		handlers.put("top",       new TopCommandHandler());
		handlers.put("check",     new CheckCommandHandler());
		handlers.put("delete",    new DeleteCommandHandler());
		handlers.put("reload",    new ReloadCommandHandler(configManager));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// 异步执行主逻辑
		new BukkitRunnable() {
			@Override
			public void run() {
				Util.addRunningTaskID(this.getTaskId());
				// args 为空打开 GUI
				if (args.length == 0 && sender instanceof Player) {
					new GUI((Player)sender, configManager);
				} else {
					String key = args.length > 0 ? args[0].toLowerCase() : "help";
					CommandHandler handler = handlers.get(key);
					if (handler == null) {
						sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
					} else {
						handler.handle(sender, args);
					}
				}
				Util.removeRunningTaskID(this.getTaskId());
			}
		}.runTaskAsynchronously(KBBSToper.getInstance());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> list = new ArrayList<>();
			String a = args[0].toLowerCase();
			handlers.forEach((k,v) -> {
				if (k.startsWith(a) && sender.hasPermission("bbstoper." + k.replace("testreward","testreward"))) {
					list.add(k);
				}
			});
			return list;
		}
		if (args.length > 1) {
			CommandHandler handler = handlers.get(args[0].toLowerCase());
			if (handler != null) {
				return handler.tabComplete(sender, args);
			}
		}
		return Collections.emptyList();
	}

	/** 获取剩余查询冷却（秒） */
	public double getQueryCooldown(UUID uuid) {
		long last = queryrecord.getOrDefault(uuid, 0L);
		int coolMs = Option.BBS_QUERYCOOLDOWN.getInt() * 1000;
		double remain = (coolMs - (System.currentTimeMillis() - last)) / 1000.0;
		return Math.max(0, remain);
	}

	/** 记录一次查询时间 */
	public void recordQuery(UUID uuid, long timeMs) {
		queryrecord.put(uuid, timeMs);
	}

}
