package mc233.fun.kbbstoper.gui;

import mc233.fun.kbbstoper.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import mc233.fun.kbbstoper.*;

import java.util.*;

public class IDListener extends RegisteredListener implements Listener, EventExecutor {
	public static final Object lock = new Object(); // 线程锁
	public static final Map<UUID, IDListener> map = new HashMap<>();
	private static ConfigManager configManager; // 添加 ConfigManager 实例

	private UUID uid;
	private boolean state;

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		callEvent(event);
	}

	@Override
	public void callEvent(Event event) throws EventException {
		if (event instanceof AsyncPlayerChatEvent) {
			onPlayerChat((AsyncPlayerChatEvent) event);
		}
	}

	public IDListener(UUID uuid, ConfigManager configManager) { // 修改构造函数以接受 ConfigManager 参数
		super(null, null, EventPriority.HIGH, KBBSToper.getInstance(), false);
		this.uid = uuid;
		this.state = false;
		IDListener.configManager = configManager; // 保存 ConfigManager 实例
	}

	public static void unregister(UUID uniqueId) {
		synchronized (lock) {
			Optional.ofNullable(map.get(uniqueId)).ifPresent(IDListener::unregister);
		}
	}

	public static void unregister(CommandSender sender) {
		if (sender instanceof Player) {
			unregister(((Player) sender).getUniqueId());
		}
	}

	public void unregister() {
		synchronized (lock) {
			AsyncPlayerChatEvent.getHandlerList().unregister((RegisteredListener) this);
			if (!map.remove(uid, this)) {
				KBBSToper.getInstance().getLogger().warning(Message.FAILEDUNINSTALLMO.getString());
			}
		}
	}

	public void register() {
		for (RegisteredListener lis : AsyncPlayerChatEvent.getHandlerList().getRegisteredListeners()) {
			if (lis == this)
				return; // 如果已经注册就取消注册
		}
		synchronized (lock) {
			IDListener old = map.put(uid, this);
			if (old != null && old != this)
				old.unregister();// 防止遗留
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				Util.addRunningTaskID(this.getTaskId());
				unregister(uid);
				Util.removeRunningTaskID(this.getTaskId());
			}
		}.runTaskLater(KBBSToper.getInstance(), 2 * 60 * 20);// 如果这个监听器还存在，那么将在2分钟后被取消
		AsyncPlayerChatEvent.getHandlerList().register(this);
	}

	public UUID getUid() {// 获取玩家名
		return this.uid;
	}

	public void onPlayerChat(AsyncPlayerChatEvent event) {// 处理事件
		if (!event.getPlayer().getUniqueId().equals(uid))
			return;
		Player player = event.getPlayer();
		String msg = event.getMessage();
		event.setCancelled(true);
		List<String> cancelkeywords = Option.GUI_CANCELKEYWORDS.getStringList();// 取消绑定关键词
		if (cancelkeywords.contains(msg)) {// 如果关键词中包含这次输入的消息
			unregister();// 取消监听事件
			CLI.getInstance(configManager).getCache().put(player.getUniqueId().toString(), null);// 清理这个键
			player.sendMessage(Message.PREFIX.getString() + Message.CANCELED.getString());
			return;
		}
		List<String> list = new ArrayList<>(Arrays.asList(msg.split("\\s+")));
		list.add(0, "binding");
		String[] args = list.toArray(new String[0]);
		CLI.getInstance(configManager).onCommand(player, null, null, args);
		if (state) {// state为true说明这是第二次进入这个方法
			unregister();
		} else {// state为false说明是第一次进入
			state = true;
		}
	}
}
