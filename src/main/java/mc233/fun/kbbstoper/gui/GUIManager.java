package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.CLI;
import mc233.fun.kbbstoper.ConfigManager;
import mc233.fun.kbbstoper.Message;
import mc233.fun.kbbstoper.Option;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class GUIManager implements Listener {
	private final ConfigManager cfg;

	public GUIManager(Plugin plugin, ConfigManager cfg) {
		this.cfg = cfg;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player)) return;
		Player p = (Player) ev.getWhoClicked();
		InventoryHolder h = p.getOpenInventory().getTopInventory().getHolder();
		if (!(h instanceof GUI.GUIHolder)) return;

		ev.setCancelled(true);
		int slot = ev.getRawSlot();
		String act = ((GUI.GUIHolder) h).getActions().get(slot);
		if (act == null) return;

		p.closeInventory();
		switch (act.toLowerCase()) {
			case "binding":
				CLI.getInstance(cfg)
						.onCommand(p, null, null, new String[]{"binding"});
				break;
			case "reward":
				CLI.getInstance(cfg)
						.onCommand(p, null, null, new String[]{"reward"});
				break;
			case "top":
				CLI.getInstance(cfg)
						.onCommand(p, null, null, new String[]{"top"});
				break;
			case "open":
				// 打开宣传帖链接
				String url = "https://www.klpbbs.com/thread-"
						+ Option.BBS_URL.getString()
						+ "-1-1.html";
				for (String line : Message.CLICKPOSTICON.getStringList()) {
					p.sendMessage(line.replace("%PAGE%", url));
				}
				break;
			default:
				p.sendMessage(Message.PREFIX.getString()
						+ Message.INVALID.getString());
		}
	}
}
