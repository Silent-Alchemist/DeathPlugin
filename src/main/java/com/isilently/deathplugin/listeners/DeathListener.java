package com.isilently.deathplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.isilently.deathplugin.utils.SQLBridge;

public class DeathListener implements Listener {

    private final SQLBridge sqlBridge;
    private final String deathMessage;
    private final int cost;
    private final int first24HCost;
    private final boolean fullLost;
    private final String banMessage;

    public DeathListener(SQLBridge sqlBridge, String deathMessage, int cost, int first24HCost, boolean fullLost,
            String banMessage) {
        this.sqlBridge = sqlBridge;
        this.deathMessage = deathMessage;
        this.cost = cost;
        this.first24HCost = first24HCost;
        this.fullLost = fullLost;
        this.banMessage = banMessage;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();

        // Determine actual cost based on first login timestamp (86,400,000 ms = 24
        // hours)
        int actualCost = cost;
        if (System.currentTimeMillis() - player.getFirstPlayed() <= 86400000L) {
            actualCost = first24HCost;
        }

        if (sqlBridge.getMoney(player.getUniqueId()) >= actualCost) {
            player.sendMessage(deathMessage);
            if (fullLost)
                sqlBridge.setMoney(player.getUniqueId(), 0);
            else
                sqlBridge.setMoney(player.getUniqueId(), sqlBridge.getMoney(player.getUniqueId()) - actualCost);
        } else {
            sqlBridge.banPlayer(player.getUniqueId(), true);
            e.getDrops().clear();
            player.getInventory().clear();

            java.util.Date unbanDate = java.util.Date
                    .from(java.time.Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS));
            org.bukkit.ban.ProfileBanList banList = Bukkit.getBanList(io.papermc.paper.ban.BanListType.PROFILE);
            banList.addBan(player.getPlayerProfile(), banMessage, unbanDate, "DeathPlugin: Out of Essence");

            player.kick(net.kyori.adventure.text.Component.text(banMessage,
                    net.kyori.adventure.text.format.NamedTextColor.RED));
        }
    }
}