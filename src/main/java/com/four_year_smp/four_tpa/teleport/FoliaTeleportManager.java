package com.four_year_smp.four_tpa.teleport;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.four_year_smp.four_tpa.FourTpaPlugin;
import com.four_year_smp.four_tpa.LocalizationHandler;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;

public final class FoliaTeleportManager extends PaperTeleportManager {
    private final AsyncScheduler _scheduler;

    public FoliaTeleportManager(FourTpaPlugin plugin, AsyncScheduler scheduler, LocalizationHandler localizationHandler) {
        super(plugin, null, localizationHandler);
        _scheduler = scheduler;
    }

    @Override
    public void teleport(Player player, Location location) {
        if (_plugin.isFolia) {
            player.getScheduler().run(_plugin, task -> player.teleportAsync(location), null);
        } else {
            player.teleport(location);
        }
    }

    @Override
    public void dispose() {
        _requests.clear();
        _scheduler.cancelTasks(_plugin);
    }

    @Override
    public void processRequests() {
        ArrayList<UUID> keys = new ArrayList<UUID>(_requests.keySet());
        for (UUID sender : keys) {
            TeleportRequest request = _requests.get(sender);
            if (request == null) {
                continue;
            } else if (request.isAccepted()) {
                _plugin.getLogger().info(MessageFormat.format("Accepting request from {0}", sender));
                _requests.remove(sender);
                _scheduler.runNow(_plugin, task -> acceptRequest(request));
            } else if (request.hasExpired(getTimeout() * 1000)) {
                _plugin.getLogger().info(MessageFormat.format("Expiring request from {0}", sender));
                _requests.remove(sender);
                _scheduler.runNow(_plugin, task -> expiredRequest(sender, request.getReceiver()));
            }
        }

        _scheduler.runDelayed(_plugin, task -> processRequests(), 200, TimeUnit.MILLISECONDS);
    }

    private void acceptRequest(TeleportRequest request) {
        Player senderPlayer = _plugin.getServer().getPlayer(request.getSender());
        Player receiverPlayer = _plugin.getServer().getPlayer(request.getReceiver());
        if (senderPlayer == null) {
            // If the sender is offline, let the receiver know.
            OfflinePlayer offlineSender = Bukkit.getOfflinePlayer(request.getSender());
            receiverPlayer.sendMessage(_localizationHandler.getPlayerWentOffline(offlineSender.getName()));
            return;
        } else if (receiverPlayer == null) {
            // If the receiver is offline, let the sender know.
            OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(request.getReceiver());
            senderPlayer.sendMessage(_localizationHandler.getPlayerWentOffline(offlineReceiver.getName()));
            return;
        }

        // Try teleporting the sender to the receiver.
        if (request instanceof TeleportHereRequest) {
            senderPlayer.getScheduler().run(_plugin, task -> teleport(receiverPlayer, senderPlayer.getLocation()), null);
        } else {
            receiverPlayer.getScheduler().run(_plugin, task -> teleport(senderPlayer, receiverPlayer.getLocation()), null);
        }
    }
}
