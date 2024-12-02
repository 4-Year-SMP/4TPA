package com.four_year_smp.four_tpa.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;

public final class BackCommand extends AbstractTpaCommand implements Listener {
    public BackCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        super(localizationHandler, teleportManager);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(_localizationHandler.getPlayersOnly());
            return true;
        }

        Location backLocation = _teleportManager.getLastLocation(player.getUniqueId());
        if (backLocation == null) {
            player.sendMessage(_localizationHandler.getPlayerBackMissing());
            return true;
        }

        _teleportManager.teleport(player, backLocation, -1);
        player.sendMessage(_localizationHandler.getPlayerBackTeleported());
        return true;
    }
}
