package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.CommandHandler;
import mc233.fun.kbbstoper.Message;
import mc233.fun.kbbstoper.Reward;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TestRewardCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.PLAYERCMD.getString());
            sender.sendMessage(Message.HELP_HELP.getString());
            return;
        }
        if (!sender.hasPermission("bbstoper.testreward")) {
            sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
            return;
        }
        String type = "NORMAL";
        if (args.length == 2) {
            String t = args[1].toUpperCase();
            if (t.equals("INCENTIVE")||t.equals("OFFDAY")) type=t;
            else {
                sender.sendMessage(Message.PREFIX.getString()+Message.INVALID.getString());
                sender.sendMessage(Message.PREFIX.getString()+Message.HELP_TESTREWARD.getString());
                return;
            }
        }
        new Reward((Player)sender, null,0).testAward(type);
        sender.sendMessage(Message.PREFIX.getString()+Message.REWARDGIVED.getString());
    }
}
