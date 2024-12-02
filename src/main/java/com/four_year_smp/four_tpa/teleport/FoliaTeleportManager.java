package com.four_year_smp.four_tpa.teleport;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Location;
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
    public void delay(Player player, int delay, Callable<Location> grabLocation, Consumer<Location> callback) {
        // Default value support since JAVA CAN'T HAVE DEFAULT VALUES FOR FUNCTION PARAMETERS
        // What even is this programming language man - A C# dev
        if (delay == -1) {
            delay = _plugin.getConfig().getInt("tpa_delay", 0) * 20;
        }

        // If the delay is less than one second, teleport the player
        if (delay < 20 || player.hasPermission("fourtpa.instant")) {
            try {
                callback.accept(grabLocation.call());
            } catch (Exception e) {
                _plugin.getLogger().warning(MessageFormat.format("Failed to grab location for {0}: {1}", player.getUniqueId(), e.getMessage()));
            }
        }

        // Otherwise count down and teleport the player after the delay
        player.sendActionBar(_localizationHandler.getTeleportDelayMessage(delay / 20));

        // Delay is in ticks and there's 20 ticks in a second...
        final int nextDelay = delay - 20;
        player.getScheduler().runDelayed(_plugin, task -> delay(player, nextDelay, grabLocation, callback), null, 20);
    }

    @Override
    public void teleport(Player player, Location location) {
        _plugin.getLogger().info(MessageFormat.format("Storing last location for {0}: {1}", player.getUniqueId(), player.getLocation()));
        _lastLocations.put(player.getUniqueId(), player.getLocation());

        _plugin.logDebug(MessageFormat.format("Teleporting {0} to {1}", player.getUniqueId(), location));
        player.teleportAsync(location);
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
                _scheduler.runNow(_plugin, task -> expiredRequest(sender, request.getTarget()));
            }
        }

        _scheduler.runDelayed(_plugin, task -> processRequests(), 200, TimeUnit.MILLISECONDS);
    }
}
