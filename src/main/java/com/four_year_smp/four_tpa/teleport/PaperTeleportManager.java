package com.four_year_smp.four_tpa.teleport;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitScheduler;
import com.four_year_smp.four_tpa.FourTpaPlugin;
import com.four_year_smp.four_tpa.LocalizationHandler;

public class PaperTeleportManager implements ITeleportManager, Listener {
    private final BukkitScheduler _scheduler;
    private final HashMap<UUID, Location> _lastLocations = new HashMap<UUID, Location>();

    protected final FourTpaPlugin _plugin;
    protected final LocalizationHandler _localizationHandler;
    protected final HashMap<UUID, TeleportRequest> _requests = new HashMap<UUID, TeleportRequest>();

    public PaperTeleportManager(FourTpaPlugin plugin, BukkitScheduler scheduler, LocalizationHandler localizationHandler) {
        _plugin = plugin;
        _scheduler = scheduler;
        _localizationHandler = localizationHandler;
    }

    public void add(TeleportRequest request) {
        _plugin.getLogger().info(MessageFormat.format("Adding request from {0}", request.getSender()));
        _requests.put(request.getSender(), request);
    }

    public void accept(UUID sender) {
        TeleportRequest teleportRequest = _requests.get(sender);
        if (teleportRequest == null) {
            _plugin.getLogger().info(MessageFormat.format("Request from {0} not found.", sender));
            return;
        }

        teleportRequest.accept();
        _plugin.getLogger().info(MessageFormat.format("Accepted request from {0}", sender));
    }

    public TeleportRequest cancel(UUID sender) {
        _plugin.getLogger().info(MessageFormat.format("Cancelling request from {0}", sender));
        return _requests.remove(sender);
    }

    public TeleportRequest getSender(UUID sender) {
        _plugin.getLogger().info(MessageFormat.format("Getting request from {0}", sender));
        return _requests.get(sender);
    }

    public TeleportRequest getRequest(UUID receiver, UUID sender) {
        for (TeleportRequest request : _requests.values()) {
            if (request.getReceiver() == receiver && request.getSender() == sender) {
                _plugin.getLogger().info(MessageFormat.format("Found request from {0} to {1}", sender, receiver));
                return request;
            }
        }

        _plugin.getLogger().info(MessageFormat.format("Request from {0} to {1} not found.", sender, receiver));
        return null;
    }

    public ArrayList<TeleportRequest> getRequests(UUID receiver) {
        ArrayList<TeleportRequest> requests = new ArrayList<TeleportRequest>();
        for (TeleportRequest request : _requests.values()) {
            if (request.getReceiver() == receiver) {
                _plugin.getLogger().info(MessageFormat.format("Adding request from {0} to {1}", request.getSender(), receiver));
                requests.add(request);
            }
        }

        _plugin.getLogger().info(MessageFormat.format("Found {0} requests for {1}", requests.size(), receiver));
        return requests;
    }

    public void teleport(Player player, Location location) {
        _plugin.getLogger().info(MessageFormat.format("Storing last location for {0}: {1}", player.getUniqueId(), player.getLocation()));
        _lastLocations.put(player.getUniqueId(), player.getLocation());

        _plugin.getLogger().info(MessageFormat.format("Teleporting {0} to {1}", player.getUniqueId(), location));
        player.teleport(location);
    }

    public Location getLastLocation(UUID playerId) {
        return _lastLocations.get(playerId);
    }

    public int getTimeout() {
        return _plugin.getConfig().getInt("tpa_timeout", 60);
    }

    public void dispose() {
        _requests.clear();
        _scheduler.cancelTasks(_plugin);
    }

    public void processRequests() {
        ArrayList<UUID> keys = new ArrayList<UUID>(_requests.keySet());
        for (UUID sender : keys) {
            TeleportRequest request = _requests.get(sender);
            if (request == null) {
                continue;
            } else if (request.isAccepted()) {
                _plugin.getLogger().info(MessageFormat.format("Accepting request from {0}", sender));
                _requests.remove(sender);
                _scheduler.runTask(_plugin, () -> acceptRequest(request));
            } else if (request.hasExpired(getTimeout() * 1000)) {
                _plugin.getLogger().info(MessageFormat.format("Expiring request from {0}", sender));
                _requests.remove(sender);
                _scheduler.runTask(_plugin, () -> expiredRequest(sender, request.getReceiver()));
            }
        }

        _scheduler.runTaskLater(_plugin, this::processRequests, 1);
    }

    private void acceptRequest(TeleportRequest teleportRequest) {
        Player senderPlayer = _plugin.getServer().getPlayer(teleportRequest.getSender());
        Player receiverPlayer = _plugin.getServer().getPlayer(teleportRequest.getReceiver());
        if (senderPlayer == null) {
            // If the sender is offline, let the receiver know.
            OfflinePlayer offlineSender = Bukkit.getOfflinePlayer(teleportRequest.getSender());
            receiverPlayer.sendMessage(_localizationHandler.getPlayerWentOffline(offlineSender.getName()));
            return;
        } else if (receiverPlayer == null) {
            // If the receiver is offline, let the sender know.
            OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(teleportRequest.getReceiver());
            senderPlayer.sendMessage(_localizationHandler.getPlayerWentOffline(offlineReceiver.getName()));
            return;
        }

        // Try teleporting the sender to the receiver.
        if (teleportRequest instanceof TeleportHereRequest) {
            teleport(receiverPlayer, senderPlayer.getLocation());
        } else {
            teleport(senderPlayer, receiverPlayer.getLocation());
        }
    }

    protected void expiredRequest(UUID sender, UUID receiver) {
        Player senderPlayer = _plugin.getServer().getPlayer(sender);
        Player receiverPlayer = _plugin.getServer().getPlayer(receiver);

        // Store usernames
        String senderName = senderPlayer != null ? senderPlayer.getName() : Bukkit.getOfflinePlayer(sender).getName();
        String receiverName = receiverPlayer != null ? receiverPlayer.getName() : Bukkit.getOfflinePlayer(receiver).getName();

        // Let both players know that the request has expired.
        if (senderPlayer != null) {
            senderPlayer.sendMessage(_localizationHandler.getTpaSenderExpired(receiverName));
        }

        if (receiverPlayer != null) {
            receiverPlayer.sendMessage(_localizationHandler.getTpaReceiverExpired(senderName));
        }
    }

    // This will not work on Folia
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Only store /back locations for player-initiated teleports
        if (event.getCause() != TeleportCause.COMMAND && event.getCause() != TeleportCause.PLUGIN) {
            return;
        }

        _lastLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
        _plugin.getLogger().info(MessageFormat.format("Stored /back location for player {0}: {1}", event.getPlayer().getName(), event.getFrom()));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getPlayer().hasPermission("fourtpa.back.on_death")) {
            _lastLocations.put(event.getEntity().getUniqueId(), event.getEntity().getLocation());
            _plugin.getLogger().info(MessageFormat.format("Stored /back location for player {0}: {1}", event.getEntity().getName(), event.getEntity().getLocation()));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent eventArgs) {
        Server server = eventArgs.getPlayer().getServer();
        String playerName = eventArgs.getPlayer().getName();
        UUID playerId = eventArgs.getPlayer().getUniqueId();

        // Cancel the TPA request the player has sent
        TeleportRequest request = cancel(playerId);
        if (request != null && server.getPlayer(request.getReceiver()) instanceof Player sender) {
            sender.sendMessage(_localizationHandler.getPlayerWentOffline(playerName));
        }

        // Cancel the TPA requests that the player has received
        for (TeleportRequest receiverRequest : getRequests(playerId)) {
            if (server.getPlayer(receiverRequest.getSender()) instanceof Player sender) {
                cancel(receiverRequest.getSender());
                sender.sendMessage(_localizationHandler.getPlayerWentOffline(playerName));
            }
        }
    }
}
