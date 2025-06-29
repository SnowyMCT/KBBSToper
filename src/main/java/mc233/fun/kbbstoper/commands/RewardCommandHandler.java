package mc233.fun.kbbstoper.commands;

import mc233.fun.kbbstoper.*;
import mc233.fun.kbbstoper.sql.SQLer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class RewardCommandHandler implements CommandHandler {
    private final SQLer sql;
    private final Map<UUID,Long> queryrecord;

    public RewardCommandHandler(SQLer sql, Map<UUID,Long> queryrecord) {
        this.sql = sql;
        this.queryrecord = queryrecord;
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.PLAYERCMD.getString());
            sender.sendMessage(Message.HELP_HELP.getString());
            return;
        }
        if (!sender.hasPermission("bbstoper.reward")) {
            sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
            return;
        }
        Player player = (Player)sender;
        UUID uid = player.getUniqueId();
        Poster poster = sql.getPoster(uid.toString());
        if (poster == null) {
            sender.sendMessage(Message.PREFIX.getString() + Message.NOTBOUND.getString());
            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
            return;
        }
        // 冷却处理
        if (!sender.hasPermission("bbstoper.bypassquerycooldown")) {
            long now = System.currentTimeMillis();
            Long last = queryrecord.getOrDefault(uid,0L);
            double cdSec = (Option.BBS_QUERYCOOLDOWN.getInt()*1000 - (now-last)) / 1000.0;
            if (cdSec>0) {
                sender.sendMessage(Message.PREFIX.getString() +
                        Message.QUERYCOOLDOWN.getString().replace("%COOLDOWN%", String.valueOf((int)cdSec)));
                return;
            }
            queryrecord.put(uid, now);
        }

        Crawler crawler = new Crawler();
        if (!crawler.visible) {
            sender.sendMessage(Message.PREFIX.getString() + Message.PAGENOTVISIBLE.getString());
            return;
        }

        // 主逻辑：遍历 crawler.ID，执行 Reward.award, 统计 issucceed/isovertime/iswaitamin
        boolean issucceed=false, isovertime=false, iswaitamin=false, havepost=false;
        List<String> temp = new ArrayList<>();
        for (int i=0; i<crawler.ID.size(); i++){
            if (!crawler.ID.get(i).equalsIgnoreCase(poster.getBbsname())) continue;
            // 重复一分钟内过滤
            for (String t:temp) if (t.equals(crawler.Time.get(i))){
                iswaitamin=true; break;
            }
            if (iswaitamin) break;
            // 未在数据库则奖励
            if (!poster.getTopStates().contains(crawler.Time.get(i))) {
                havepost=true;
                // 日期归零
                String today=new SimpleDateFormat("yyyy-M-dd").format(new Date());
                if (!today.equals(poster.getRewardbefore())){
                    poster.setRewardbefore(today);
                    poster.setRewardtime(0);
                }
                if (poster.getRewardtime() < Option.REWARD_TIMES.getInt()) {
                    new Reward(player, crawler, i).award();
                    sql.addTopState(poster.getBbsname(), crawler.Time.get(i));
                    poster.setRewardtime(poster.getRewardtime()+1);
                    issucceed=true;
                } else {
                    isovertime=true;
                }
                temp.add(crawler.Time.get(i));
            }
        }
        sql.updatePoster(poster);

        // 反馈
        if (issucceed) {
            sender.sendMessage(Message.PREFIX.getString()+Message.REWARDGIVED.getString());
            Bukkit.getOnlinePlayers().stream()
                    .filter(p->p.hasPermission("bbstoper.reward"))
                    .filter(p->p.canSee(player))
                    .forEach(p->
                            p.sendMessage(Message.BROADCAST.getString().replace("%PLAYER%",player.getName()))
                    );
        }
        if (isovertime) {
            sender.sendMessage(Message.PREFIX.getString()+
                    Message.OVERTIME.getString().replace("%REWARDTIMES%",String.valueOf(Option.REWARD_TIMES.getInt()))
            );
        }
        if (iswaitamin)
            sender.sendMessage(Message.PREFIX.getString()+Message.WAITAMIN.getString());
        if (!havepost)
            sender.sendMessage(Message.PREFIX.getString()+Message.NOPOST.getString());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
