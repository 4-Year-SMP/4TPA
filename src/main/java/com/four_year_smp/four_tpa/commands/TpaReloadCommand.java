package com.four_year_smp.four_tpa.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;

public class TpaReloadCommand extends AbstractTpaCommand {
    public TpaReloadCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager) {
        super(localizationHandler, teleportManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        _localizationHandler.reload();
        sender.sendMessage(_localizationHandler.getReloaded());
        return true;
    }
}
