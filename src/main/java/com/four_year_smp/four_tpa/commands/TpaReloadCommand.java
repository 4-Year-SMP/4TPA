package com.four_year_smp.four_tpa.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.four_year_smp.four_tpa.FourTpaPlugin;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;

public class TpaReloadCommand extends AbstractTpaCommand {
    private final FourTpaPlugin _plugin;

    public TpaReloadCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager, FourTpaPlugin plugin) {
        super(localizationHandler, teleportManager);
        _plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        _localizationHandler.reload();
        _plugin.reloadConfig();
        sender.sendMessage(_localizationHandler.getReloaded());
        return true;
    }
}
