package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.CommandHandler;
import mc233.fun.kbbstoper.Message;
import org.bukkit.command.CommandSender;
import java.util.List;

public class HelpCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandSender sender, String[] args) {
        sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TITLE.getString());
        if (sender.hasPermission("bbstoper.reward"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_REWARD.getString());
        if (sender.hasPermission("bbstoper.testreward"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TESTREWARD.getString());
        if (sender.hasPermission("bbstoper.binding"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
        if (sender.hasPermission("bbstoper.list"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_LIST.getString());
        if (sender.hasPermission("bbstoper.top"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TOP.getString());
        if (sender.hasPermission("bbstoper.check"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_CHECK.getString());
        if (sender.hasPermission("bbstoper.delete"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_DELETE.getString());
        if (sender.hasPermission("bbstoper.reload"))
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_RELOAD.getString());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
