package com.four_year_smp.four_tpa;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.four_year_smp.four_tpa.commands.BackCommand;
import com.four_year_smp.four_tpa.commands.TpaAcceptAllCommand;
import com.four_year_smp.four_tpa.commands.TpaAcceptCommand;
import com.four_year_smp.four_tpa.commands.TpaCancelCommand;
import com.four_year_smp.four_tpa.commands.TpaCommand;
import com.four_year_smp.four_tpa.commands.TpaDenyAllCommand;
import com.four_year_smp.four_tpa.commands.TpaDenyCommand;
import com.four_year_smp.four_tpa.commands.TpaHereCommand;
import com.four_year_smp.four_tpa.commands.TpaOfflineCommand;
import com.four_year_smp.four_tpa.commands.TpaReloadCommand;
import com.four_year_smp.four_tpa.teleport.FoliaTeleportManager;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;
import com.four_year_smp.four_tpa.teleport.PaperTeleportManager;

public final class FourTpaPlugin extends JavaPlugin implements Listener {
    private LocalizationHandler _localizationHandler;
    private ITeleportManager _teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        _localizationHandler = new LocalizationHandler(this);
        _teleportManager = isFolia() ? new FoliaTeleportManager(this, getServer().getAsyncScheduler(), _localizationHandler) : new PaperTeleportManager(this, getServer().getScheduler(), _localizationHandler);
        _teleportManager.processRequests();

        // Register the event listeners
        BackCommand backCommand = new BackCommand(_localizationHandler, _teleportManager);
        TpaOfflineCommand tpaOfflineCommand = new TpaOfflineCommand(_localizationHandler, _teleportManager, this);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents((Listener) _teleportManager, this);
        pluginManager.registerEvents(tpaOfflineCommand, this);

        // Register the commands
        getCommand("back").setExecutor(backCommand);
        getCommand("tpa").setExecutor(new TpaCommand(_localizationHandler, _teleportManager));
        getCommand("tpaccept").setExecutor(new TpaAcceptCommand(_localizationHandler, _teleportManager));
        getCommand("tpacancel").setExecutor(new TpaCancelCommand(_localizationHandler, _teleportManager));
        getCommand("tpadeny").setExecutor(new TpaDenyCommand(_localizationHandler, _teleportManager));
        getCommand("tpahere").setExecutor(new TpaHereCommand(_localizationHandler, _teleportManager));
        getCommand("tpacceptall").setExecutor(new TpaAcceptAllCommand(_localizationHandler, _teleportManager));
        getCommand("tpadenyall").setExecutor(new TpaDenyAllCommand(_localizationHandler, _teleportManager));
        getCommand("tpareload").setExecutor(new TpaReloadCommand(_localizationHandler, _teleportManager, this));
        getCommand("tpaoffline").setExecutor(tpaOfflineCommand);
    }

    @Override
    public void onDisable() {
        _teleportManager.dispose();
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
