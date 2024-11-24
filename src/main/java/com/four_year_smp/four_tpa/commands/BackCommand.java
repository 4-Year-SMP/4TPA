package com.four_year_smp.four_tpa.commands;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import com.four_year_smp.four_tpa.FourTpaPlugin;
import com.four_year_smp.four_tpa.LocalizationHandler;

public final class BackCommand implements CommandExecutor, Listener {
    private final HashMap<UUID, Location> _backLocations = new HashMap<UUID, Location>();
    private final LocalizationHandler _localizationHandler;
    private final FourTpaPlugin _plugin;

    public BackCommand(LocalizationHandler localizationHandler, FourTpaPlugin plugin) {
        _localizationHandler = localizationHandler;
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
        if (_plugin.isFolia) {
            player.getScheduler().run(_plugin, task -> player.teleportAsync(backLocation), null);
        } else {
            player.teleport(backLocation);
        }

        player.sendMessage(_localizationHandler.getPlayerBackTeleported());
        return true;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        _backLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
        _plugin.getLogger().info(MessageFormat.format("Stored /back location for player {0}: {1}", event.getPlayer().getName(), event.getFrom()));
    }
}
