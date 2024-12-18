package com.four_year_smp.four_tpa.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;
import com.four_year_smp.four_tpa.teleport.TeleportRequest;

public class TpaCommand extends AbstractTpaCommand {
    public TpaCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        super(localizationHandler, teleportManager);
    }

    @Override
    public boolean onCommand(CommandSender invoker, Command command, String label, String[] args) {
        if (!(invoker instanceof Player sender)) {
            // Console cannot TPA
            invoker.sendMessage(_localizationHandler.getPlayersOnly());
            return true;
        } else if (args.length != 1) {
            return false;
        }

        // Test to see if there's a pending TPA request
        TeleportRequest request = _teleportManager.getSender(sender.getUniqueId());
        if (request != null) {
            // You already have a pending request with someone else ('OoLunar').
            Player target = invoker.getServer().getPlayer(request.getTarget());
            if (target != null) {
                sender.sendMessage(_localizationHandler.getTpaSenderConflict(target.getName()));
                return true;
            }

            // Player 'OoLunar' is not online.
            _teleportManager.cancel(sender.getUniqueId());
        }

        // Try to fetch the other player. This will return null if the player is not online.
        if (!(getPlayerOrFailWithMessage(invoker.getServer(), sender, args[0]) instanceof Player target)) {
            return true;
        }

        sendTpaRequest(new TeleportRequest(sender.getUniqueId(), target.getUniqueId()), sender, target);
        return true;
    }
}
