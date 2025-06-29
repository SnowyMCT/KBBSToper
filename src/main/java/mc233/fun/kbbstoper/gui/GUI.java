package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.*;
import mc233.fun.kbbstoper.sql.SQLer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUI {

	private static SQLer sql;
	private Inventory inv;

	/** 从配置里取 Title，保证可改 */
	public static String getTitle() {
		return Message.GUI_TITLE.getString()
				.replace("%PREFIX%", Message.PREFIX.getString());
	}

	public GUI(Player player) {
		createGui(player);
		// 打开界面必须在主线程
		Bukkit.getScheduler().runTask(KBBSToper.getInstance(),
				() -> player.openInventory(inv)
		);
	}

	/** 标记本插件的 InventoryHolder，方便事件中识别 */
	static class GUIHolder implements InventoryHolder {
		private final Inventory gui;
		public GUIHolder(Inventory gui) { this.gui = gui; }
		@Override
		public Inventory getInventory() { return gui; }
	}

	@SuppressWarnings("deprecation")
	private void createGui(Player player) {
		// 1) 创建背包
		inv = Bukkit.createInventory(
				new GUIHolder(inv),
				InventoryType.CHEST,
				getTitle()
		);

		// 2) 边框
		for (int i = 0; i < inv.getSize(); i++) {
			if (i > 9 && i < 17) continue;
			inv.setItem(i, getRandomPane());
		}

		// 3) 玩家头颅
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		if (Option.GUI_DISPLAYHEADSKIN.getBoolean()) {
			// 1.13+ 推荐
			skullMeta.setOwningPlayer(player);
		}
		skullMeta.setDisplayName(
				Message.GUI_SKULL.getString()
						.replace("%PLAYER%", player.getName())
		);
		List<String> lore = new ArrayList<>();
		Poster poster = sql.getPoster(player.getUniqueId().toString());
		if (poster == null) {
			lore.add(Message.GUI_NOTBOUND.getString());
			lore.add(Message.GUI_CLICKBOUND.getString());
		} else {
			lore.add(
					Message.GUI_BBSID.getString()
							.replace("%BBSID%", poster.getBbsname())
			);
			lore.add(
					Message.GUI_POSTTIMES.getString()
							.replace("%TIMES%", "" + poster.getTopStates().size())
			);
			lore.add(Message.GUI_CLICKREBOUND.getString());
		}
		skullMeta.setLore(lore);
		skull.setItemMeta(skullMeta);
		inv.setItem(12, skull);

		// 4) 奖励详情（以太阳花做示例）
		ItemStack rewardItem = new ItemStack(Material.SUNFLOWER, 1);
		ItemMeta rewardMeta = rewardItem.getItemMeta();
		rewardMeta.setDisplayName(Message.GUI_REWARDS.getString());

		List<String> rewardLore = new ArrayList<>(Message.GUI_REWARDSINFO.getStringList());
		if (rewardLore.isEmpty()) {
			// 普通奖励
			rewardLore.addAll(Option.REWARD_COMMANDS.getStringList());
			// 激励奖励
			if (Option.REWARD_INCENTIVEREWARD_ENABLE.getBoolean()) {
				rewardLore.add(Message.GUI_INCENTIVEREWARDS.getString());
				rewardLore.addAll(Option.REWARD_INCENTIVEREWARD_COMMANDS.getStringList());
			}
			// 休息日奖励
			if (Option.REWARD_OFFDAYREWARD_ENABLE.getBoolean()) {
				rewardLore.add(Message.GUI_OFFDAYREWARDS.getString());
				rewardLore.addAll(Option.REWARD_OFFDAYREWARD_COMMANDS.getStringList());
			}
		}
		rewardLore.add(Message.GUI_CLICKGET.getString());
		rewardMeta.setLore(rewardLore);
		rewardItem.setItemMeta(rewardMeta);
		inv.setItem(13, rewardItem);

		// 5) 排行
		ItemStack star = new ItemStack(Material.NETHER_STAR, 1);
		ItemMeta starMeta = star.getItemMeta();
		starMeta.setDisplayName(Message.GUI_TOPS.getString());
		List<String> starLore = new ArrayList<>();
		List<Poster> tops = sql.getTopPosters();
		for (int i = 0; i < Math.min(tops.size(), Option.GUI_TOPPLAYERS.getInt()); i++) {
			Poster p = tops.get(i);
			starLore.add(
					Message.POSTERPLAYER.getString() + ":" + p.getName() + " "
							+ Message.POSTERID.getString() + ":" + p.getBbsname() + " "
							+ Message.POSTERNUM.getString() + ":" + p.getCount()
			);
		}
		starMeta.setLore(starLore);
		star.setItemMeta(starMeta);
		inv.setItem(14, star);

		// 6) 帖子状态
		ItemStack compass = new ItemStack(Material.COMPASS, 1);
		ItemMeta compMeta = compass.getItemMeta();
		compMeta.setDisplayName(Message.GUI_PAGESTATE.getString());
		List<String> compLore = new ArrayList<>();
		compLore.add(
				Message.GUI_PAGEID.getString()
						.replace("%PAGEID%", Option.BBS_URL.getString())
		);
		Crawler crawler = new Crawler();
		if (crawler.visible) {
			String lastPost = crawler.Time.isEmpty() ? "----" : crawler.Time.get(0);
			compLore.add(
					Message.GUI_LASTPOST.getString()
							.replace("%TIME%", lastPost)
			);
		} else {
			compLore.add(Message.GUI_PAGENOTVISIBLE.getString());
		}
		String extra = Util.getExtraReward(crawler);
		if (extra != null) {
			compLore.add(
					Message.GUI_EXTRAREWARDS.getString()
							.replace("%EXTRA%", extra)
			);
		}
		compLore.add(Message.GUI_CLICKOPEN.getString());
		compMeta.setLore(compLore);
		compass.setItemMeta(compMeta);
		inv.setItem(22, compass);
	}

	/**
	 * 随机一个彩色玻璃板作边框
	 */
	private ItemStack getRandomPane() {
		Material[] panes = {
				Material.WHITE_STAINED_GLASS_PANE,
				Material.ORANGE_STAINED_GLASS_PANE,
				Material.MAGENTA_STAINED_GLASS_PANE,
				Material.LIGHT_BLUE_STAINED_GLASS_PANE,
				Material.YELLOW_STAINED_GLASS_PANE,
				Material.LIME_STAINED_GLASS_PANE,
				Material.PINK_STAINED_GLASS_PANE,
				Material.GRAY_STAINED_GLASS_PANE,
				Material.LIGHT_GRAY_STAINED_GLASS_PANE,
				Material.CYAN_STAINED_GLASS_PANE,
				Material.PURPLE_STAINED_GLASS_PANE,
				Material.BLUE_STAINED_GLASS_PANE,
				Material.BROWN_STAINED_GLASS_PANE,
				Material.GREEN_STAINED_GLASS_PANE,
				Material.RED_STAINED_GLASS_PANE,
				Material.BLACK_STAINED_GLASS_PANE
		};
		Material color = panes[new Random().nextInt(panes.length)];
		ItemStack pane = new ItemStack(color, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(Message.GUI_FRAME.getString());
		pane.setItemMeta(meta);
		return pane;
	}

	public Inventory getGui() {
		return inv;
	}

	public static void setSQLer(SQLer s) {
		GUI.sql = s;
	}
}
