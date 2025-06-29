package mc233.fun.kbbstoper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import mc233.fun.kbbstoper.sql.SQLer;

public class Crawler {
	private static SQLer sql;
	public List<String> ID = new ArrayList<>();
	public List<String> Time = new ArrayList<>();
	public boolean visible = true;

	public Crawler() {
		resolveWebData();
		kickExpiredData();
	}

	public void resolveWebData() {
		String url = "https://www.klpbbs.com/forum.php?mod=misc&action=viewthreadmod&tid=" + Option.BBS_URL.getString() + "&mobile=no";
		Document doc = null;
		try {
			if (Option.PROXY_ENABLE.getBoolean()) {
				doc = Jsoup.connect(url).proxy(Option.PROXY_IP.getString(), Option.PROXY_PORT.getInt()).get();
			} else {
				doc = Jsoup.connect(url).get();
			}
		} catch (IOException e) {
			if (Option.DEBUG.getBoolean()) {
				e.printStackTrace();
			}
			KBBSToper.getInstance().getLogger().warning(Message.FAILEDGETWEB.getString());
			return;
		}

		Elements listclass = doc.getElementsByClass("list");
		Element list = null;
		try {
			list = listclass.get(0);
		} catch (IndexOutOfBoundsException e) {
			this.visible = false;
			KBBSToper.getInstance().getLogger().warning(Message.FAILEDRESOLVEWEB.getString());
			return;
		}

		Element listbody = list.getElementsByTag("tbody").get(0);
		for (Element rows : listbody.getElementsByTag("tr")) {
			Elements cells = rows.getElementsByTag("td");
			String action = cells.get(2).text();
			if (!(action.equals("提升(提升卡)") || action.equals("提升(服务器/交易代理提升卡)"))) {
				continue;
			}

			Element idcell = cells.get(0);
			String id = idcell.getElementsByTag("a").get(0).text();
			Element timecell = cells.get(1);
			String time = extractTime(timecell);

			ID.add(id);
			Time.add(time);
		}
	}

	private String extractTime(Element timecell) {
		Element timespan = timecell.getElementsByTag("span").first();
		if (timespan != null) {
			return timespan.attr("title");
		} else {
			return timecell.text();
		}
	}

	public void kickExpiredData() {
		SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-M-d HH:mm");
		Date now = new Date();
		long validtime = Option.REWARD_PERIOD.getInt() * 24 * 60 * 60 * 1000L;
		Date expirydate = new Date(now.getTime() - validtime);

		// 使用 ListIterator 遍历
		ListIterator<String> timeIterator = Time.listIterator();
		ListIterator<String> idIterator = ID.listIterator();

		while (timeIterator.hasNext()) {
			int index = timeIterator.nextIndex(); // 获取当前元素的索引
			String timeStr = timeIterator.next(); // 获取当前时间

			if (timeStr == null || timeStr.isEmpty()) {
				continue;
			}

			Date date = null;
			try {
				date = sdfm.parse(timeStr);
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			}

			// 如果过期则删除
			if (date.before(expirydate)) {
				timeIterator.remove(); // 移除当前时间
				idIterator.remove();   // 移除对应的 ID
			}
		}
	}


	public void activeReward() {
		for (int i = 0; i < ID.size(); i++) {
			String bbsname = ID.get(i);
			String time = Time.get(i);

			if (!sql.checkTopstate(bbsname, time)) {
				String uuid = sql.bbsNameCheck(bbsname);
				Poster poster = sql.getPoster(uuid);
				if (uuid != null) {
					processRewardForPlayer(uuid, poster, bbsname, time, i);
				}
			}
		}
	}

	private void processRewardForPlayer(String uuid, Poster poster, String bbsname, String time, int index) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		Player olplayer;
		if (player.isOnline()) {
			olplayer = Bukkit.getPlayer(UUID.fromString(uuid));
			if (!olplayer.hasPermission("bbstoper.reward")) {
				return;
			}
		} else {
			return;
		}

		String datenow = new SimpleDateFormat("yyyy-M-dd").format(new Date());
		if (!datenow.equals(poster.getRewardbefore())) {
			poster.setRewardbefore(datenow);
			poster.setRewardtime(0);
		}

		if (poster.getRewardtime() >= Option.REWARD_TIMES.getInt()) {
			return;
		}

		new Reward(olplayer, this, index).award();
		sql.addTopState(bbsname, time);
		poster.setRewardtime(poster.getRewardtime() + 1);
		sql.updatePoster(poster);

		broadcastRewardToPlayers(olplayer, player);
	}

	private void broadcastRewardToPlayers(Player olplayer, OfflinePlayer player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!p.canSee(olplayer)) continue;
			if (!p.hasPermission("bbstoper.reward")) continue;
			p.sendMessage(Message.BROADCAST.getString().replace("%PLAYER%", player.getName()));
		}
	}

	public static void setSQLer(SQLer sql) {
		Crawler.sql = sql;
	}
}
