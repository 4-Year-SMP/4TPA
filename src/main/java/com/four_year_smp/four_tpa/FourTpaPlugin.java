package com.four_year_smp.four_tpa;

import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
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
import com.four_year_smp.four_tpa.teleport.TeleportRequest;

public final class FourTpaPlugin extends JavaPlugin implements Listener {
    public final boolean isFolia = isFolia();

    private LocalizationHandler _localizationHandler;
    private ITeleportManager _teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        _localizationHandler = new LocalizationHandler(this);
        _teleportManager = isFolia ? new FoliaTeleportManager(this, getServer().getAsyncScheduler(), _localizationHandler) : new PaperTeleportManager(this, getServer().getScheduler(), _localizationHandler);
        _teleportManager.processRequests();

        // Register the event listeners
        BackCommand backCommand = new BackCommand(_localizationHandler, _teleportManager, this);
        TpaOfflineCommand tpaOfflineCommand = new TpaOfflineCommand(_localizationHandler, _teleportManager, this);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(backCommand, this);
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
        getCommand("tpareload").setExecutor(new TpaReloadCommand(_localizationHandler, _teleportManager));
        getCommand("tpaoffline").setExecutor(tpaOfflineCommand);
    }

    @Override
    public void onDisable() {
        _teleportManager.dispose();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent eventArgs) {
        Server server = getServer();
        String playerName = eventArgs.getPlayer().getName();
        UUID playerId = eventArgs.getPlayer().getUniqueId();

        // Cancel the TPA request the player has sent
        TeleportRequest request = _teleportManager.cancel(playerId);
        if (request != null && server.getPlayer(request.getReceiver()) instanceof Player sender) {
            sender.sendMessage(_localizationHandler.getPlayerWentOffline(playerName));
        }

        // Cancel the TPA requests that the player has received
        for (TeleportRequest receiverRequest : _teleportManager.getRequests(playerId)) {
            if (server.getPlayer(receiverRequest.getSender()) instanceof Player sender) {
                _teleportManager.cancel(receiverRequest.getSender());
                sender.sendMessage(_localizationHandler.getPlayerWentOffline(playerName));
            }
        }
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
