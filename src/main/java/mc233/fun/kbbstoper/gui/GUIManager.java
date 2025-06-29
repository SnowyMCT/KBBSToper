package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.CLI;
import mc233.fun.kbbstoper.ConfigManager;
import mc233.fun.kbbstoper.Message;
import mc233.fun.kbbstoper.Option;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class GUIManager implements Listener {
	private final CLI cli;

	/**
	 * 构造时传入 CLI 单例
	 */
	public GUIManager(Plugin plugin, ConfigManager cfg, CLI cli) {
		this.cli = cli;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player)) return;
		Player p = (Player) ev.getWhoClicked();
		InventoryHolder holder = p.getOpenInventory().getTopInventory().getHolder();
		if (!(holder instanceof GUI.GUIHolder)) return;

		ev.setCancelled(true);
		int slot = ev.getRawSlot();
		String action = ((GUI.GUIHolder) holder).getActions().get(slot);
		if (action == null) return;

		p.closeInventory();
		// 注意：这里 cmd 和 label 参数我们传 null 和插件主命令名
		Command fakeCmd = null;
		String label = "kbbstoper";

		switch (action.toLowerCase()) {
			case "binding":
				cli.onCommand(p, fakeCmd, label, new String[]{"binding"});
				break;
			case "reward":
				cli.onCommand(p, fakeCmd, label, new String[]{"reward"});
				break;
			case "top":
				cli.onCommand(p, fakeCmd, label, new String[]{"top"});
				break;
			case "open":
				String url = "https://www.klpbbs.com/thread-"
						+ Option.BBS_URL.getString()
						+ "-1-1.html";
				for (String line : Message.CLICKPOSTICON.getStringList()) {
					p.sendMessage(line.replace("%PAGE%", url));
				}
				break;
			default:
				p.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
		}
	}
}
