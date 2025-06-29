package mc233.fun.kbbstoper.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import mc233.fun.kbbstoper.*;

import java.util.Arrays;
import java.util.UUID;

public class GUIManager implements Listener {
	private ConfigManager configManager; // 添加 ConfigManager 实例

	public GUIManager(Plugin plugin, ConfigManager configManager) { // 修改构造函数以接受 ConfigManager 参数
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.configManager = configManager; // 保存 ConfigManager 实例
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;// 如果不是玩家操作的，返回
		Player player = (Player) event.getWhoClicked();
		InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
		if(holder instanceof GUI.GUIHolder) {// 确认操作的是此插件的GUI
			event.setCancelled(true);
			if (event.getRawSlot() == 12) {// 点击绑定
				if (Option.GUI_USECHATGETID.getBoolean() == true) {
					player.closeInventory();
					UUID uid = player.getUniqueId();
					synchronized (IDListener.lock) { // 线程锁防止异步错位修改
						IDListener rglistener = IDListener.map.get(uid);
						// 如果这个玩家没有一个监听器
						if (rglistener == null) {
							new IDListener(player.getUniqueId(), configManager).register(); // 为此玩家创建一个监听器
							String keywords = Arrays.toString(Option.GUI_CANCELKEYWORDS.getStringList().toArray());
							player.sendMessage(Message.PREFIX.getString() + Message.ENTER.getString().replaceAll("%KEYWORD%", keywords));
						}
					}
				}
				if (Option.GUI_USECHATGETID.getBoolean() == false) {
					player.closeInventory();
					player.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
				}
			}
			if (event.getRawSlot() == 13) {
				player.closeInventory();
				String[] args = { "reward" };
				CLI.getInstance(configManager).onCommand(player, null, null, args); // 将 ConfigManager 实例传递给 CLI 类
			}
			if (event.getRawSlot() == 22) {// 获取链接
				player.closeInventory();
				for (String msg : Message.CLICKPOSTICON.getStringList()) {
					String url = "https://www.klpbbs.com/thread-" + Option.BBS_URL.getString() + "-1-1.html";
					player.sendMessage(msg.replaceAll("%PAGE%", url));
				}
			}
		}
	}

}
