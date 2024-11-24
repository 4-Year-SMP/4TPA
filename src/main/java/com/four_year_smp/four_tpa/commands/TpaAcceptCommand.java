package com.four_year_smp.four_tpa.commands;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;
import com.four_year_smp.four_tpa.teleport.TeleportRequest;

public class TpaAcceptCommand extends AbstractTpaCommand implements TabCompleter {
    public TpaAcceptCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        super(localizationHandler, teleportManager);
    }

    @Override
    public boolean onCommand(CommandSender invoker, Command command, String label, String[] args) {
        if (!(invoker instanceof Player target)) {
            // Console cannot TPA
            invoker.sendMessage(_localizationHandler.getPlayersOnly());
            return true;
        } else if (args.length > 1) {
            // No arguments means accept the first request.
            // One argument means accept the request from that player.
            // More than one argument means you're doing it wrong.
            return false;
        }

        Player sender = null;
        if (args.length == 0) {
            List<TeleportRequest> teleportRequests = _teleportManager.getRequests(target.getUniqueId());

            // Grab the first valid request
            while (teleportRequests.size() > 0) {
                TeleportRequest request = teleportRequests.get(0);
                sender = target.getServer().getPlayer(request.getSender());
                if (sender != null) {
                    break;
                }

                // Don't message the player saying the target went offline
                // since the player disconnect event handler will handle that.
                _teleportManager.cancel(request.getSender());
                teleportRequests.remove(0);
            }

            if (sender == null) {
                // You have no pending TPA requests.
                target.sendMessage(_localizationHandler.getTpaNone());
                return true;
            }
        } else if ((sender = getPlayerOrFailWithMessage(invoker.getServer(), target, args[0])) == null) {
            return true;
        }

        if (!(_teleportManager.getRequest(target.getUniqueId(), sender.getUniqueId()) instanceof TeleportRequest request)) {
            // You have no pending TPA requests.
            target.sendMessage(_localizationHandler.getTpaNone());
            return true;
        }

        acceptTpaRequest(request, sender, target);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        } else if (args.length == 1) {
            return new ArrayList<String>();
        }

        Server server = player.getServer();
        ArrayList<String> tabCompletions = new ArrayList<String>();
        for (TeleportRequest request : _teleportManager.getRequests(player.getUniqueId())) {
            tabCompletions.add(server.getPlayer(request.getSender()).getName());
        }

        return tabCompletions;
    }
}
