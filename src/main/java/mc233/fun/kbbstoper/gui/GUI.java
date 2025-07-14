package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.*;
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

	public class GUIHolder implements InventoryHolder {
		@Override
		public Inventory getInventory() { return inv; }
		public Map<Integer, String> getActions() { return actions; }
	}

	private void buildGui(Player player) {
		ConfigurationSection root = cfgMgr.getGuiConfig()
				.getConfigurationSection("gui");
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

		// 画边框
		if (root.isConfigurationSection("border")) {
			ConfigurationSection bsec = root.getConfigurationSection("border");
			Material fill = Material.valueOf(
					bsec.getString("fill", "WHITE_STAINED_GLASS_PANE"));
			String rawSlots = String.join(";",
					(List<String>) bsec.get("slots", Collections.emptyList()));
			for (String part : rawSlots.split(";")) {
				try {
					int slot = Integer.parseInt(part.trim());
					inv.setItem(slot, createPane(fill));
				} catch (Exception ignore) {}
			}
		} else {
			for (int i = 0; i < inv.getSize(); i++) {
				if (i > 9 && i < 17) continue;
				inv.setItem(i, getRandomPane());
			}
		}

		// items
		ConfigurationSection items = root.getConfigurationSection("items");
		if (items == null) return;

		for (String key : items.getKeys(false)) {
			ConfigurationSection isec = items.getConfigurationSection(key);
			int slot = isec.getInt("slot", -1);
			if (slot < 0 || slot >= inv.getSize()) continue;

			boolean isBind = key.equals("bind");
			String userUuid = player.getUniqueId().toString();
			Poster poster = sql.getPoster(userUuid);
			String posterId = (poster != null ? poster.getBbsname() : null);

			boolean bound = posterId != null && !posterId.isBlank();

			// 选择材质
			String typeKey = (isBind && bound && isec.getString("bound-type") != null)
					? "bound-type" : "type";
			Material mat = Material.valueOf(
					isec.getString(typeKey, "STONE"));

			ItemStack item = new ItemStack(mat);
			ItemMeta meta = item.getItemMeta();

			// 名称
			String nameKey = (isBind && bound && isec.getString("bound-displayName") != null)
					? "bound-displayName" : "displayName";
			String rawName = isec.getString(nameKey, "");
			String parsedName = applyPlaceholders(player, rawName);
			meta.setDisplayName(
					ChatColor.translateAlternateColorCodes('&', parsedName));

			// lore
			List<String> rawLore = (isBind && bound && isec.isList("bound-lore"))
					? isec.getStringList("bound-lore")
					: isec.getStringList("lore");
			List<String> lore = new ArrayList<>();
			for (String line : rawLore) {
				String parsed = applyPlaceholders(player, line);
				lore.add(ChatColor.translateAlternateColorCodes('&', parsed));
			}
			meta.setLore(lore);

			// 玩家头像
			if (mat == Material.PLAYER_HEAD) {
				SkullMeta skull = (SkullMeta) meta;
				skull.setOwningPlayer(player);
				item.setItemMeta(skull);
			} else {
				item.setItemMeta(meta);
			}

			inv.setItem(slot, item);

			// 点击动作
			String actionKey = (isBind && bound)
					? isec.getString("bound-action", "")
					: isec.getString("action", "");
			if (!actionKey.isBlank()) {
				actions.put(slot, actionKey);
			}
		}
	}

	private ItemStack getRandomPane() {
		Material[] panes = {
				Material.WHITE_STAINED_GLASS_PANE,
				Material.LIGHT_GRAY_STAINED_GLASS_PANE,
				Material.GRAY_STAINED_GLASS_PANE,
				Material.BLACK_STAINED_GLASS_PANE
		};
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

	private String applyPlaceholders(Player player, String text) {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			return me.clip.placeholderapi.PlaceholderAPI
					.setPlaceholders(player, text);
		}
		return text;
	}
}
