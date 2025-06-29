package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.ConfigManager;
import mc233.fun.kbbstoper.KBBSToper;
import mc233.fun.kbbstoper.Message;
import mc233.fun.kbbstoper.Option;
import mc233.fun.kbbstoper.Util;
import mc233.fun.kbbstoper.sql.SQLer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUI {

	private final ConfigManager cfgMgr;
	private static SQLer sql;
	private Inventory inv;
	private final Map<Integer, String> actions = new HashMap<>();

	public static void setSQLer(SQLer s) {
		sql = s;
	}

	public GUI(Player player, ConfigManager cfgMgr) {
		this.cfgMgr = cfgMgr;
		buildGui(player);
		Bukkit.getScheduler().runTask(KBBSToper.getInstance(),
				() -> player.openInventory(inv)
		);
	}

	/** 标记这是我们的 GUI，并携带 slot->action 映射 */
	public class GUIHolder implements InventoryHolder {
		@Override
		public Inventory getInventory() { return inv; }
		public Map<Integer,String> getActions() { return actions; }
	}

	private void buildGui(Player player) {
		// 1. 从 gui.yml 读取根节点
		ConfigurationSection root = cfgMgr.getGuiConfig().getConfigurationSection("gui");
		if (root == null) {
			KBBSToper.getInstance().getLogger()
					.severe("无法读取 gui.yml 中的 gui 节点！");
			return;
		}

		String rawTitle = root.getString("title", Message.GUI_TITLE.getString())
				.replace("%PREFIX%", Message.PREFIX.getString());
		String title = ChatColor.translateAlternateColorCodes('&', rawTitle);

		int rows = Math.max(1, root.getInt("rows", 3));
		inv = Bukkit.createInventory(new GUIHolder(), rows * 9, title);

		// 3. 画边框（如果配置了 border）
		if (root.isConfigurationSection("border")) {
			ConfigurationSection bsec = root.getConfigurationSection("border");
			Material fill = Material.valueOf(bsec.getString("fill", "WHITE_STAINED_GLASS_PANE"));
			String rawSlots = String.join(";",
					(List<String>) bsec.get("slots", Collections.emptyList())
			);
			for (String part : rawSlots.split(";")) {
				try {
					int slot = Integer.parseInt(part.trim());
					inv.setItem(slot, createPane(fill));
				} catch (Exception ignore) {}
			}
		} else {
			// 默认随机边框
			for (int i = 0; i < inv.getSize(); i++) {
				if (i > 9 && i < 17) continue;
				inv.setItem(i, getRandomPane());
			}
		}

		// 4. 按 items 定义放置物品
		ConfigurationSection items = root.getConfigurationSection("items");
		if (items != null) {
			for (String key : items.getKeys(false)) {
				ConfigurationSection isec = items.getConfigurationSection(key);
				int slot = isec.getInt("slot", -1);
				if (slot < 0 || slot >= inv.getSize()) continue;

				// 材质
				Material mat = Material.valueOf(isec.getString("type", "STONE"));
				ItemStack item = new ItemStack(mat);
				ItemMeta meta = item.getItemMeta();

				// 显示名
				String dn = isec.getString("displayName", "").replace("&", "§");
				meta.setDisplayName(dn);

				// lore
				List<String> lore = new ArrayList<>();
				for (String line : isec.getStringList("lore")) {
					lore.add(line.replace("&", "§"));
				}
				meta.setLore(lore);

				// 如果是玩家头像
				if (mat == Material.PLAYER_HEAD && posterBound(player)) {
					((SkullMeta) meta).setOwningPlayer(player);
				}

				item.setItemMeta(meta);
				inv.setItem(slot, item);

				// 记录点击动作
				String action = isec.getString("action", "");
				if (!action.isBlank()) {
					actions.put(slot, action);
				}
			}
		}
	}

	/** border 随机玻璃板 */
	private ItemStack getRandomPane() {
		Material[] panes = {/*.. 同上 ..*/};
		Material m = panes[new Random().nextInt(panes.length)];
		return createPane(m);
	}

	private ItemStack createPane(Material m) {
		ItemStack it = new ItemStack(m);
		ItemMeta me = it.getItemMeta();
		me.setDisplayName(Message.GUI_FRAME.getString());
		it.setItemMeta(me);
		return it;
	}

	private boolean posterBound(Player p) {
		return sql.getPoster(p.getUniqueId().toString()) != null;
	}
}
