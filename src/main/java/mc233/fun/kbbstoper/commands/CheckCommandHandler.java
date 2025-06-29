package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.*;
import mc233.fun.kbbstoper.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class CheckCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bbstoper.check")) {
            sender.sendMessage(Message.PREFIX.getString()+Message.NOPERMISSION.getString());
            return;
        }
        if (args.length!=3) {
            sender.sendMessage(Message.PREFIX.getString()+Message.INVALID.getString());
            sender.sendMessage(Message.PREFIX.getString()+Message.HELP_CHECK.getString());
            return;
        }
        switch(args[1].toLowerCase()) {
            case "bbsid":
                String user = SQLManager.getSQLer().bbsNameCheck(args[2]);
                if (user==null) {
                    sender.sendMessage(Message.PREFIX.getString()+Message.IDNOTFOUND.getString());
                } else {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(user));
                    sender.sendMessage(
                            Message.PREFIX.getString()+
                                    Message.IDOWNER.getString()
                                            .replace("%PLAYER%",op.getName())
                                            .replace("%UUID%",user)
                    );
                }
                break;
            case "player":
                OfflinePlayer op2 = Bukkit.getOfflinePlayer(args[2]);
                Poster p = SQLManager.getSQLer().getPoster(op2.getUniqueId().toString());
                if (p==null) {
                    sender.sendMessage(Message.PREFIX.getString()+Message.OWNERNOTFOUND.getString());
                } else {
                    sender.sendMessage(
                            Message.PREFIX.getString()+
                                    Message.OWNERID.getString().replace("%ID%",p.getBbsname())
                    );
                }
                break;
            default:
                sender.sendMessage(Message.PREFIX.getString()+Message.INVALID.getString());
        }
    }
}
