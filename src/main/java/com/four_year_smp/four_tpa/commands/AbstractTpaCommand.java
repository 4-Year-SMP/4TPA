package com.four_year_smp.four_tpa.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.sayandev.sayanvanish.api.SayanVanishAPI;
import org.sayandev.sayanvanish.api.User;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;
import com.four_year_smp.four_tpa.teleport.TeleportHereRequest;
import com.four_year_smp.four_tpa.teleport.TeleportRequest;

public abstract class AbstractTpaCommand implements CommandExecutor {
    protected final LocalizationHandler _localizationHandler;
    protected final ITeleportManager _teleportManager;

    public AbstractTpaCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        _localizationHandler = localizationHandler;
        _teleportManager = teleportManager;
    }

    public void sendTpaRequest(TeleportRequest teleportRequest, Player sender, Player receiver) {
        _teleportManager.add(teleportRequest);
        if (teleportRequest instanceof TeleportHereRequest) {
            sender.sendMessage(_localizationHandler.getTpaHereSender(receiver.getName(), _teleportManager.getTimeout()));
            receiver.sendMessage(_localizationHandler.getTpaHereReceiver(sender.getName(), _teleportManager.getTimeout()));
        } else {
            sender.sendMessage(_localizationHandler.getTpaSenderSend(receiver.getName(), _teleportManager.getTimeout()));
            receiver.sendMessage(_localizationHandler.getTpaReceiverReceive(sender.getName(), _teleportManager.getTimeout()));
        }
    }

    public void acceptTpaRequest(TeleportRequest request, Player sender, Player receiver) {
        _teleportManager.accept(request.getSender());
        sender.sendMessage(_localizationHandler.getTpaSenderAccepted(receiver.getName()));
        receiver.sendMessage(_localizationHandler.getTpaReceiverAccept(sender.getName()));
    }

    public void denyTpaRequest(TeleportRequest request, Player sender, Player receiver) {
        _teleportManager.cancel(request.getSender());
        sender.sendMessage(_localizationHandler.getTpaSenderDenied(receiver.getName()));
        receiver.sendMessage(_localizationHandler.getTpaReceiverDeny(sender.getName()));
    }

    public void cancelTpaRequest(TeleportRequest request, Player sender, Player receiver) {
        _teleportManager.cancel(request.getSender());
        sender.sendMessage(_localizationHandler.getTpaSenderCancel(receiver.getName()));
        receiver.sendMessage(_localizationHandler.getTpaReceiverCancel(sender.getName()));
    }

    public Player getPlayerOrFailWithMessage(Server server, Player player, String targetName) {
        if (server.getPlayerExact(targetName) instanceof Player target) {
            SayanVanishAPI<User> api = SayanVanishAPI.getInstance();
            User playerUser = api.getUser(player.getUniqueId());
            User targetUser = api.getUser(target.getUniqueId());
            if (targetUser == null || SayanVanishAPI.getInstance().canSee(playerUser, targetUser)) {
                return target;
            } else {
                player.sendMessage(_localizationHandler.getPlayerWentOffline(target.getName()));
                return null;
            }
        } else if (server.getOfflinePlayerIfCached(targetName) instanceof OfflinePlayer offlineTarget) {
            // Player 'OoLunar' is not online.
            player.sendMessage(_localizationHandler.getPlayerWentOffline(offlineTarget.getName()));
            return null;
        } else {
            // Player 'OoLunar' was not found.
            player.sendMessage(_localizationHandler.getPlayerNotFound(targetName));
            return null;
        }
    }
}
