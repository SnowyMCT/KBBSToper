// CommandHandler.java
package mc233.fun.kbbstoper;

import org.bukkit.command.CommandSender;
import java.util.List;

/**
 * 每个子命令的处理器，handle() 完成业务逻辑，
 * tabComplete() 返回二级参数建议（可返回 null）。
 */
public interface CommandHandler {
    void handle(CommandSender sender, String[] args);
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
