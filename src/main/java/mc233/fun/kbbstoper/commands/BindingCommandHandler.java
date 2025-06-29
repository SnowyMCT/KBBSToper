package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.CommandHandler;
import mc233.fun.kbbstoper.Message;
import mc233.fun.kbbstoper.Option;
import mc233.fun.kbbstoper.Poster;
import mc233.fun.kbbstoper.gui.IDListener;
import mc233.fun.kbbstoper.sql.SQLer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BindingCommandHandler implements CommandHandler {
    private final SQLer sql;
    private final Map<String,String> cache;

    public BindingCommandHandler(SQLer sql, Map<String,String> cache) {
        this.sql = sql;
        this.cache = cache;
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.PLAYERCMD.getString());
            sender.sendMessage(Message.HELP_HELP.getString());
            return;
        }
        if (!sender.hasPermission("bbstoper.binding")) {
            sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
            IDListener.unregister(sender);
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
            IDListener.unregister(sender);
            return;
        }

        Player p = (Player)sender;
        String uuid = p.getUniqueId().toString();
        Poster poster = sql.getPoster(uuid);
        boolean exists = poster != null;
        if (!exists) {
            poster = new Poster();
            poster.setUuid(uuid);
            poster.setName(p.getName());
        } else {
            // 冷却检测
            long cd = System.currentTimeMillis() - poster.getBinddate();
            long limit = (long) Option.BBS_CHANGEIDCOOLDOWN.getInt() * 86400000L;
            if (cd < limit) {
                long left = (limit - cd) / 86400000L;
                sender.sendMessage(Message.PREFIX.getString() +
                        Message.ONCOOLDOWN.getString().replace("%COOLDOWN%", String.valueOf(left)));
                IDListener.unregister(sender);
                return;
            }
        }

        String input = args[1];
        String otherUuid = sql.bbsNameCheck(input);
        if (otherUuid == null) {
            // **首次输入或确认重复**
            if (input.equals(cache.get(uuid))) {
                poster.setBbsname(input);
                poster.setBinddate(System.currentTimeMillis());
                if (exists) sql.updatePoster(poster);
                else sql.addPoster(poster);
                sender.sendMessage(Message.PREFIX.getString() + Message.BINDINGSUCCESS.getString());
                cache.remove(uuid);
            } else {
                cache.put(uuid, input);
                sender.sendMessage(Message.PREFIX.getString() + Message.REPEAT.getString());
            }
        } else if (otherUuid.equals(uuid)) {
            sender.sendMessage(Message.PREFIX.getString() + Message.OWNSAMEBIND.getString());
        } else {
            sender.sendMessage(Message.PREFIX.getString() + Message.SAMEBIND.getString());
        }
        IDListener.unregister(sender);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
