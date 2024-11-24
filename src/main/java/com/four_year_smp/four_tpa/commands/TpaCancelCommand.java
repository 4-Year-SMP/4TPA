package com.four_year_smp.four_tpa.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;
import com.four_year_smp.four_tpa.teleport.TeleportRequest;

public class TpaCancelCommand extends AbstractTpaCommand implements TabCompleter {
    public TpaCancelCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        super(localizationHandler, teleportManager);
    }

    @Override
    public boolean onCommand(CommandSender invoker, Command command, String label, String[] args) {
        if (!(invoker instanceof Player sender)) {
            // Console cannot TPA
            invoker.sendMessage(_localizationHandler.getPlayersOnly());
            return true;
        } else if (args.length != 0) {
            return false;
        }

        TeleportRequest request = _teleportManager.getSender(sender.getUniqueId());
        if (request == null) {
            // You have no pending TPA requests.
            sender.sendMessage(_localizationHandler.getTpaNone());
            return true;
        }

        cancelTpaRequest(request, sender, sender.getServer().getPlayer(request.getReceiver()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}
