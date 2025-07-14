package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.CLI;
import mc233.fun.kbbstoper.ConfigManager;
import mc233.fun.kbbstoper.Message;
import mc233.fun.kbbstoper.Option;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

// Spigot 自带的 TextComponent 系列类，随 SpigotAPI 而来，无需额外依赖 BungeeCord 插件
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class GUIManager implements Listener {
	private final CLI cli;

	public GUIManager(Plugin plugin, ConfigManager cfg, CLI cli) {
		this.cli = cli;
		plugin.getServer()
				.getPluginManager()
				.registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player)) return;
		Player p = (Player) ev.getWhoClicked();
		InventoryHolder holder = p.getOpenInventory()
				.getTopInventory()
				.getHolder();
		if (!(holder instanceof GUI.GUIHolder)) return;

		ev.setCancelled(true);
		String action = ((GUI.GUIHolder) holder)
				.getActions()
				.get(ev.getRawSlot());
		if (action == null) return;

		p.closeInventory();
		Command fake = null;
		String label = "kbbstoper";

		switch (action.toLowerCase()) {
			case "binding":
				sendBindingSuggestion(p);
				break;

			case "reward":
				cli.onCommand(p, fake, label, new String[]{"reward"});
				break;

			case "top":
				cli.onCommand(p, fake, label, new String[]{"top"});
				break;

			case "open":
				String url = "https://www.klpbbs.com/thread-"
						+ Option.BBS_URL.getString()
						+ "-1-1.html";
				Message.CLICKPOSTICON.getStringList().forEach(
						line -> p.sendMessage(
								line.replace("%PAGE%", url)
						)
				);
				break;

			default:
				p.sendMessage(
						Message.PREFIX.getString()
								+ Message.INVALID.getString()
				);
		}
	}

	/**
	 * 发送一条可点击的聊天组件，
	 * 玩家点击后聊天栏自动补全 "/bt binding "，
	 * 然后玩家自己在后面输入 ID 并回车触发绑定命令。
	 */
	private void sendBindingSuggestion(Player p) {
		TextComponent msg = new TextComponent("▶ §a点击此处绑定论坛ID §7◀");
		// 鼠标悬停提示
		msg.setHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("§7点击后自动补全 §b/bt binding ").create()
		));
		// 点击后在聊天栏填入命令
		msg.setClickEvent(new ClickEvent(
				ClickEvent.Action.SUGGEST_COMMAND,
				"/bt binding "
		));
		p.spigot().sendMessage(msg);
	}
}
