package com.four_year_smp.four_tpa.commands;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;
import com.four_year_smp.four_tpa.FourTpaPlugin;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;

public final class BackCommand extends AbstractTpaCommand implements Listener {
    private final HashMap<UUID, Location> _backLocations = new HashMap<UUID, Location>();
    private final FourTpaPlugin _plugin;

    public BackCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager, FourTpaPlugin plugin) {
        super(localizationHandler, teleportManager);
        _plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(_localizationHandler.getPlayersOnly());
            return true;
        }

        Location backLocation = _backLocations.get(player.getUniqueId());
        if (backLocation == null) {
            player.sendMessage(_localizationHandler.getPlayerBackMissing());
            return true;
        }

        _backLocations.put(player.getUniqueId(), player.getLocation());
        _teleportManager.teleport(player, backLocation);
        player.sendMessage(_localizationHandler.getPlayerBackTeleported());
        return true;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Only store /back locations for player-initiated teleports
        if (event.getCause() != TeleportCause.COMMAND && event.getCause() != TeleportCause.PLUGIN) {
            return;
        }

        _backLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
        _plugin.getLogger().info(MessageFormat.format("Stored /back location for player {0}: {1}", event.getPlayer().getName(), event.getFrom()));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getPlayer().hasPermission("fourtpa.back.on_death")) {
            _backLocations.put(event.getEntity().getUniqueId(), event.getEntity().getLocation());
            _plugin.getLogger().info(MessageFormat.format("Stored /back location for player {0}: {1}", event.getEntity().getName(), event.getEntity().getLocation()));
        }
    }
}
