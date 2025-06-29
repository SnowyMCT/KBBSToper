package mc233.fun.kbbstoper;

import org.bukkit.entity.Player;
import java.util.UUID;

public class CLIUtil {
    /** 获取剩余查询冷却（秒） */
    public static double getQueryCooldown(Player p) {
        // 假设 CLI#onCommand 里是异步执行，
        // 这里取 CLI 单例里保存的 queryrecord
        UUID uuid = p.getUniqueId();
        return CLI.getInstance().getQueryCooldown(uuid);
    }

    /** 记录一次查询时间 */
    public static void recordQuery(Player p) {
        UUID uuid = p.getUniqueId();
        CLI.getInstance().recordQuery(uuid, System.currentTimeMillis());
    }
}
