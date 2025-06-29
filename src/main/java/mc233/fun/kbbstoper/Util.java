package mc233.fun.kbbstoper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Util {

	private static BukkitTask autorewardtask;
	private static ArrayList<Integer> runningtaskidlist = new ArrayList<Integer>();

	public static void startAutoReward() {// 自动奖励的方法
		if (autorewardtask != null) {// 任务对象不为空
			boolean taskcancelled;// 是否已经取消
			try {
				taskcancelled = autorewardtask.isCancelled();
			} catch (NoSuchMethodError e) {// 1.7.10还没有这个方法
				taskcancelled = false;// 默认就当这个任务没有取消
			}
			if (!taskcancelled) {// 如果任务还被取消
				autorewardtask.cancel();// 将之前的任务取消
			}
		}
		int period = Option.REWARD_AUTO.getInt() * 20;
		if (period > 0) {
			autorewardtask = new BukkitRunnable() {// 自动奖励，异步执行
				@Override
				public void run() {
					addRunningTaskID(this.getTaskId());
					task();
					removeRunningTaskID(this.getTaskId());
				}

				public void task() {
					Crawler crawler = new Crawler();
					if (!crawler.visible)
						return;
					crawler.activeReward();
				}
			}.runTaskTimerAsynchronously(KBBSToper.getInstance(), 0, period);
		}
	}

	public static void waitForAllTask() {// 此方法会阻塞直到所有此插件创建的线程结束
		int count = 0;
		try {
			while (!runningtaskidlist.isEmpty()) {// 当list非空，阻塞线程100毫秒后再判断一次
				if (count > 30000) {// 超过30秒没有关闭就算超时
					throw new TimeoutException();
				}
				Thread.sleep(100);
				count = count + 100;
			}
		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	public static void addRunningTaskID(int i) {
		if (!runningtaskidlist.contains(i))
			runningtaskidlist.add(i);
	}

	public static void removeRunningTaskID(int i) {
		if (runningtaskidlist.contains(i))
			runningtaskidlist.remove((Integer) i);
	}

	public static String getExtraReward(Crawler crawler) {
		boolean incentive = false; // 激励奖励
		boolean offday    = false; // 休息日奖励

		// 当前时间
		Calendar current = Calendar.getInstance();

		// 上一次顶帖时间，默认设置为 1970-01-01
		Calendar lastpost = Calendar.getInstance();
		lastpost.setTime(new Date(0));

		// 如果有顶帖记录，就尝试解析第一条时间
		if (!crawler.Time.isEmpty()) {
			String firstTime = crawler.Time.get(0);
			if (firstTime != null && !firstTime.isBlank()) {
				try {
					Date parsed = new SimpleDateFormat("yyyy-M-d HH:mm")
							.parse(firstTime);
					lastpost.setTime(parsed);
				} catch (ParseException e) {
					// 解析失败时打印一条警告，保持 lastpost=1970
					KBBSToper.getInstance().getLogger()
							.warning("无法解析顶帖时间: \"" + firstTime + "\"");
				}
			}
		}

		// 判断激励/休息日奖励条件
		if (Reward.canIncentiveReward(current, lastpost)) {
			incentive = true;
		}
		if (Reward.canOffDayReward(current)) {
			offday = true;
		}

		// 拼接额外奖励提示
		String extra = null;
		if (incentive) {
			// 当同时满足 offday 且激励/休息日都不是“额外”时
			if (!(offday
					&& !Option.REWARD_INCENTIVEREWARD_EXTRA.getBoolean()
					&& !Option.REWARD_OFFDAYREWARD_EXTRA.getBoolean())) {
				extra = Message.GUI_INCENTIVEREWARDS.getString();
			}
		}
		if (offday) {
			String offText = Message.GUI_OFFDAYREWARDS.getString();
			extra = (extra == null) ? offText : (extra + "+" + offText);
		}
		return extra;
	}
}
