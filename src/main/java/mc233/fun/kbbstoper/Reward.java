package mc233.fun.kbbstoper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Reward {
	private Player player; // 发放奖励的对象
	private Crawler crawler; // 一个爬虫对象
	private int index; // 要发放奖励的那条记录的序号

	// 定义常量，用于正则表达式
	private static final Pattern UPCASE_PATTERN = Pattern.compile("^[A-Z]+$");
	private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}-\\d{2}$");

	private static Calendar parseDateToCalendar(String dateStr, SimpleDateFormat dateFormat) {
		Calendar calendar = Calendar.getInstance();
		if (dateStr == null || dateStr.isBlank()) {
			calendar.setTime(new Date(0)); // 默认时间
			return calendar;
		}
		try {
			Date date = dateFormat.parse(dateStr);
			calendar.setTime(date);
		} catch (ParseException e) {
			KBBSToper.getInstance().getLogger().warning("无法解析时间：" + dateStr);
			calendar.setTime(new Date(0)); // 回退到安全时间
		}
		return calendar;
	}

	// current指需要判断的时间, before指上一个顶贴的时间
	public static boolean canIncentiveReward(Calendar current, Calendar before) {
		if (Option.REWARD_INCENTIVEREWARD_ENABLE.getBoolean()) { // 开启了激励奖励
			Calendar copyofcurrent = (Calendar) before.clone(); // 上次顶贴时间的副本
			copyofcurrent.add(Calendar.MINUTE, Option.REWARD_INCENTIVEREWARD_PERIOD.getInt()); // 加上设定好的激励时间
			return copyofcurrent.before(current); // 如果这个时间已经处于"当前领奖的记录"之前
		}
		return false;
	}

	// current指需要判断的时间
	public static boolean canOffDayReward(Calendar current) {
		if (Option.REWARD_OFFDAYREWARD_ENABLE.getBoolean()) { // 开启了休息日奖励
			for (String day : Option.REWARD_OFFDAYREWARD_OFFDAYS.getStringList()) {
				if (UPCASE_PATTERN.matcher(day).matches()) { // 如果是全大写英文字符
					int dayofweek = getDayOfWeekFromString(day);
					if (dayofweek == current.get(Calendar.DAY_OF_WEEK)) { // 如果当前就是设定的日子
						return true;
					}
				} else if (DATE_PATTERN.matcher(day).matches()) { // 如果是00-00这种字符串
					SimpleDateFormat offdayformat = new SimpleDateFormat("M-dd");
					Calendar offdaycalendar = parseDateToCalendar(day, offdayformat);
					if (current.get(Calendar.MONTH) == offdaycalendar.get(Calendar.MONTH)
							&& current.get(Calendar.DAY_OF_MONTH) == offdaycalendar.get(Calendar.DAY_OF_MONTH)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 从字符串获取星期几（例如："MONDAY" -> Calendar.MONDAY）
	private static int getDayOfWeekFromString(String day) {
		try {
			return Calendar.class.getField(day).getInt(null); // 获取Calendar类中对应的星期字段
		} catch (Exception e) {
			e.printStackTrace();
			return -1; // 如果无法获取，返回一个无效值
		}
	}

	public boolean isIntervalTooShort(Calendar thispost, int index) {
		SimpleDateFormat bbsformat = new SimpleDateFormat("yyyy-M-d HH:mm");
		Date thispostDate = thispost.getTime();

		for (int x = index + 1; x < crawler.Time.size(); x++) {
			if (!crawler.ID.get(x).equalsIgnoreCase(crawler.ID.get(index))) continue;

			String timeStr = crawler.Time.get(x);
			if (timeStr == null || timeStr.isBlank()) continue;

			try {
				Date lastDate = bbsformat.parse(timeStr);
				if (lastDate == null) continue;

				long minutes = (thispostDate.getTime() - lastDate.getTime()) / (1000 * 60);
				if (minutes <= Option.REWARD_INTERVAL.getInt()) {
					return true;
				} else {
					break; // 时间间隔已超阈值，可退出
				}
			} catch (ParseException e) {
				KBBSToper.getInstance().getLogger()
						.warning("无法解析顶贴时间：" + timeStr + "（index=" + x + "）");
			}
		}
		return false;
	}


	// 构造函数
	public Reward(Player player, Crawler crawler, int index) {
		this.player = player;
		this.crawler = crawler;
		this.index = index;
	}

	// 发放奖励
	public void award() {
		List<String> cmds = new ArrayList<>();
		boolean incentive = false;
		boolean offday = false;
		boolean normal = true;
		SimpleDateFormat bbsformat = new SimpleDateFormat("yyyy-M-d HH:mm"); // bbs的日期格式
		Calendar thispost = parseDateToCalendar(crawler.Time.get(index), bbsformat);

		// 如果顶贴间隔短于设定值则不进行操作
		if (Option.REWARD_INTERVAL.getInt() > 0 && isIntervalTooShort(thispost, index)) {
			player.sendMessage(Message.PREFIX.getString() + Message.INTERVALTOOSHORT.getString()
					.replaceAll("%TIME%", crawler.Time.get(index))
					.replaceAll("%INTERVAL%", Option.REWARD_INTERVAL.getString()));
			return;
		}

		// 获取上一次顶贴时间
		Calendar lastpost = Calendar.getInstance();
		if (crawler.Time.size() > index + 1) {
			lastpost = parseDateToCalendar(crawler.Time.get(index + 1), bbsformat);
		} else {
			lastpost.setTime(new Date(0)); // 没人顶过贴, 将时间设置为1970年
		}

		if (canIncentiveReward(thispost, lastpost)) {
			incentive = true;
		}
		if (canOffDayReward(thispost)) {
			offday = true;
		}

		String extra = null;
		if (incentive) { // 激励奖励
			if (!(offday && !Option.REWARD_INCENTIVEREWARD_EXTRA.getBoolean() && !Option.REWARD_OFFDAYREWARD_EXTRA.getBoolean())) {
				cmds.addAll(Option.REWARD_INCENTIVEREWARD_COMMANDS.getStringList());
				extra = Message.GUI_INCENTIVEREWARDS.getString();
			}
			if (!Option.REWARD_INCENTIVEREWARD_EXTRA.getBoolean()) {
				normal = false; // 如果激励奖励已发放，不再发放普通奖励
			}
		}
		if (offday) { // 休息日奖励
			cmds.addAll(Option.REWARD_OFFDAYREWARD_COMMANDS.getStringList());
			if (extra == null) {
				extra = Message.GUI_OFFDAYREWARDS.getString();
			} else {
				extra = extra + "+" + Message.GUI_OFFDAYREWARDS.getString();
			}
			if (!Option.REWARD_OFFDAYREWARD_EXTRA.getBoolean()) {
				normal = false; // 如果休息日奖励已发放，不再发放普通奖励
			}
		}
		if (normal) { // 普通奖励
			cmds.addAll(Option.REWARD_COMMANDS.getStringList());
		}

		// 让主线程执行奖励命令
		Bukkit.getScheduler().runTask(KBBSToper.getInstance(), () -> {
			for (String cmd : cmds) {
				cmd = cmd.replaceAll("%PLAYER%", player.getName());
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		});

		// 给玩家发奖励信息
		player.sendMessage(Message.PREFIX.getString() + Message.REWARD.getString().replaceAll("%TIME%", crawler.Time.get(index)));
		if (extra != null) {
			player.sendMessage(Message.PREFIX.getString() + Message.EXTRAREWARD.getString().replaceAll("%EXTRA%", extra));
		}
	}

	// 测试奖励发放
	public void testAward(String type) {
		List<String> cmds = new ArrayList<>();
		switch (type) {
			case "NORMAL":
				cmds.addAll(Option.REWARD_COMMANDS.getStringList());
				break;
			case "INCENTIVE":
				cmds.addAll(Option.REWARD_INCENTIVEREWARD_COMMANDS.getStringList());
				break;
			case "OFFDAY":
				cmds.addAll(Option.REWARD_OFFDAYREWARD_COMMANDS.getStringList());
				break;
		}
		// 让主线程执行
		Bukkit.getScheduler().runTask(KBBSToper.getInstance(), () -> {
			for (String cmd : cmds) {
				cmd = cmd.replaceAll("%PLAYER%", player.getName());
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		});
	}
}
