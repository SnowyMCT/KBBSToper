package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.*;
import mc233.fun.kbbstoper.sql.SQLManager;
import org.bukkit.command.CommandSender;

public class ReloadCommandHandler implements CommandHandler {
    private final ConfigManager configManager;

    public ReloadCommandHandler(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bbstoper.reload")) {
            sender.sendMessage(Message.PREFIX.getString()+Message.NOPERMISSION.getString());
            return;
        }
        KBBSToper.getInstance().saveDefaultConfig();
        Option.load();
        configManager.reloadConfig();
        Message.load(configManager);
        SQLManager.initializeSQLer();
        SQLManager.startTimingReconnect();
        Util.startAutoReward();
        sender.sendMessage(Message.PREFIX.getString()+Message.RELOAD.getString());
    }
}
