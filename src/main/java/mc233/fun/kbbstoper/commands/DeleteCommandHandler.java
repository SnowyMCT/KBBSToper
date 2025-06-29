package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.*;
import mc233.fun.kbbstoper.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class DeleteCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bbstoper.delete")) {
            sender.sendMessage(Message.PREFIX.getString()+Message.NOPERMISSION.getString());
            return;
        }
        if (args.length!=2) {
            sender.sendMessage(Message.PREFIX.getString()+Message.INVALID.getString());
            sender.sendMessage(Message.PREFIX.getString()+Message.HELP_DELETE.getString());
            return;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        UUID uid = op.getUniqueId();
        Poster p = SQLManager.getSQLer().getPoster(uid.toString());
        if (p==null) {
            sender.sendMessage(Message.PREFIX.getString()+Message.OWNERNOTFOUND.getString());
        } else {
            SQLManager.getSQLer().deletePoster(uid.toString());
            sender.sendMessage(Message.PREFIX.getString()+Message.DELETESUCCESS.getString());
        }
    }
}
