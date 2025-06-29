package mc233.fun.kbbstoper.sql;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mc233.fun.kbbstoper.*;

import mc233.fun.kbbstoper.gui.GUI;

public class SQLManager {
	public static SQLer sql;
	private static BukkitTask timingreconnecttask;

	public static void initializeSQLer() {// 初始化或重载数据库
		SQLer.writelock.lock();
		try {
			if (sql != null) {
				sql.closeConnection();// 此方法会在已经建立过连接的情况下关闭连接
			}
			if (Option.DATABASE_TYPE.getString().equalsIgnoreCase("mysql")) {
				sql = MySQLer.getInstance();
			} else if (Option.DATABASE_TYPE.getString().equalsIgnoreCase("sqlite")) {
				sql = SQLiter.getInstance();
			}
			sql.load();
			SQLer instance = sql;                   // sql 已经初始化
			CLI.setSQLer(instance);
			GUI.setSQLer(instance);
			Crawler.setSQLer(instance);
			Poster.setSQLer(instance);
			Reminder.setSQLer(instance);
			if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
				PAPIExpansion.setSQLer(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SQLer.writelock.unlock();
		}
	}

	public static void closeSQLer() {// 关闭数据库
		sql.closeConnection();
		sql = null;
	}

	public static SQLer getSQLer() {
		return sql;
	}

	public static void startTimingReconnect() {// 自动重连数据库的方法
		if (timingreconnecttask != null && !timingreconnecttask.isCancelled()) {// 将之前的任务取消(如果存在)
			timingreconnecttask.cancel();
		}
		int period = Option.DATABASE_TIMINGRECONNECT.getInt() * 20;
		if (period > 0) {
			timingreconnecttask = new BukkitRunnable() {
				@Override
				public void run() {
					Util.addRunningTaskID(this.getTaskId());
					initializeSQLer();// 重载数据库
					Util.removeRunningTaskID(this.getTaskId());
				}
			}.runTaskTimerAsynchronously(KBBSToper.getInstance(), period, period);
		}
	}
}
