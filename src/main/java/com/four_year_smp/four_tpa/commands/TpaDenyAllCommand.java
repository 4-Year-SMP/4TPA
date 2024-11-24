package com.four_year_smp.four_tpa.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;
import com.four_year_smp.four_tpa.teleport.TeleportRequest;

public class TpaDenyAllCommand extends TpaCommand implements TabCompleter {
    public TpaDenyAllCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        super(localizationHandler, teleportManager);
    }

    @Override
    public boolean onCommand(CommandSender invoker, Command command, String label, String[] args) {
        if (!(invoker instanceof Player target)) {
            // Console cannot TPA
            invoker.sendMessage(_localizationHandler.getPlayersOnly());
            return true;
        } else if (args.length != 0) {
            return false;
        }

        UUID playerId = target.getUniqueId();
        Server server = target.getServer();
        ArrayList<TeleportRequest> teleportRequests = _teleportManager.getRequests(playerId);
        if (teleportRequests.size() == 0) {
            // You have no pending TPA requests.
            target.sendMessage(_localizationHandler.getTpaNone());
            return true;
        }

        // Accept all pending TPA requests
        target.sendMessage(_localizationHandler.getTpaSenderAcceptAll(teleportRequests.size()));
        while (teleportRequests.size() > 0) {
            TeleportRequest request = teleportRequests.get(0);
            teleportRequests.remove(0);

            if (!(server.getPlayer(request.getSender()) instanceof Player sender)) {
                // Don't message the player saying the target went offline
                // since the player disconnect event handler will handle that.
                _teleportManager.cancel(request.getSender());
                continue;
            }

            denyTpaRequest(request, sender, target);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}
