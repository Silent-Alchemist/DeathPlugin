package com.isilently.deathplugin.listeners;

import com.isilently.deathplugin.utils.SQLBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final SQLBridge sqlBridge;
    private final String joinMessage;
    private final int dailyMoney;

    public JoinListener(SQLBridge sqlBridge, String joinMessage, int dailyMoney) {
        this.sqlBridge = sqlBridge;
        this.joinMessage = joinMessage;
        this.dailyMoney = dailyMoney;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Syncs the database ban status to false since their 24h Paper API ban has
        // naturally expired if they are able to join
        sqlBridge.banPlayer(player.getUniqueId(), false);

        if (sqlBridge.playerExist(player.getUniqueId())) {
            if (sqlBridge.newDay(player.getUniqueId())) {
                player.sendMessage(joinMessage);
                sqlBridge.setMoney(player.getUniqueId(), sqlBridge.getMoney(player.getUniqueId()) + dailyMoney);
            }
        } else {
            sqlBridge.createPlayerData(player.getUniqueId(), player.getName());
            player.sendMessage(joinMessage);
            sqlBridge.setMoney(player.getUniqueId(), sqlBridge.getMoney(player.getUniqueId()) + dailyMoney);
        }
    }
}