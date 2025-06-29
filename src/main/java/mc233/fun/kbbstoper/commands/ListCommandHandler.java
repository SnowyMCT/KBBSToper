package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.*;
import mc233.fun.kbbstoper.sql.SQLer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ListCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bbstoper.list")) {
            sender.sendMessage(Message.PREFIX.getString()+Message.NOPERMISSION.getString());
            return;
        }
        if (sender instanceof Player && !sender.hasPermission("bbstoper.bypassquerycooldown")) {
            double cd = CLIUtil.getQueryCooldown((Player)sender);
            if (cd>0) {
                sender.sendMessage(Message.PREFIX.getString()+
                        Message.QUERYCOOLDOWN.getString().replace("%COOLDOWN%",String.valueOf((int)cd))
                );
                return;
            }
            CLIUtil.recordQuery((Player)sender);
        }
        int page=1;
        if (args.length==2) {
            try { page=Integer.parseInt(args[1]); }
            catch(Exception e){
                sender.sendMessage(Message.PREFIX.getString()+Message.INVALID.getString());
                sender.sendMessage(Message.PREFIX.getString()+Message.HELP_LIST.getString());
                return;
            }
        } else if (args.length>2) {
            sender.sendMessage(Message.PREFIX.getString()+Message.INVALID.getString());
            sender.sendMessage(Message.PREFIX.getString()+Message.HELP_LIST.getString());
            return;
        }

        Crawler crawler = new Crawler();
        if (!crawler.visible) {
            sender.sendMessage(Message.PREFIX.getString()+Message.PAGENOTVISIBLE.getString());
            return;
        }

        int size = crawler.ID.size();
        int total = (int)Math.ceil(size/(double)Option.BBS_PAGESIZE.getInt());
        if (page>total) {
            sender.sendMessage(Message.PREFIX.getString()+Message.OVERPAGE.getString());
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add(Message.PREFIX.getString()+Message.POSTERNUM.getString()+":"+size);
        int start=(page-1)*Option.BBS_PAGESIZE.getInt();
        int end=Math.min(start+Option.BBS_PAGESIZE.getInt(), size);
        for (int i=start;i<end;i++){
            lines.add(
                    Message.POSTERID.getString()+":"+crawler.ID.get(i)+" "+
                            Message.POSTERTIME.getString()+":"+crawler.Time.get(i)
            );
        }
        if (start==end) lines.add(Message.NOPOSTER.getString());
        lines.add(
                Message.PREFIX.getString()+
                        Message.PAGEINFO.getString()
                                .replace("%PAGE%",String.valueOf(page))
                                .replace("%TOTALPAGE%",String.valueOf(total))
        );
        lines.forEach(sender::sendMessage);
    }
}
