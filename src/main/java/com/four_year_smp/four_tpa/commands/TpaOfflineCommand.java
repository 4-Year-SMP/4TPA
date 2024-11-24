package com.four_year_smp.four_tpa.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.four_year_smp.four_tpa.FourTpaPlugin;
import com.four_year_smp.four_tpa.LocalizationHandler;
import com.four_year_smp.four_tpa.teleport.ITeleportManager;

public class TpaOfflineCommand extends AbstractTpaCommand implements TabCompleter, Listener {
    private final FourTpaPlugin _plugin;
    private ArrayList<OfflinePlayer> _offlinePlayers;

    public TpaOfflineCommand(LocalizationHandler localizationHandler, ITeleportManager teleportManager, FourTpaPlugin plugin) {
        super(localizationHandler, teleportManager);
        _plugin = plugin;
        _offlinePlayers = new ArrayList<OfflinePlayer>(Arrays.asList(plugin.getServer().getOfflinePlayers()));
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

        // Test to see if the player is online
        if (invoker.getServer().getPlayer(args[0]) instanceof Player target) {
            sender.sendMessage(_localizationHandler.getPlayerIsOnline(target.getName()));
            return true;
        }

        // Test to see if the player has logged into the server ever
        OfflinePlayer offlinePlayer = invoker.getServer().getOfflinePlayer(args[0]);
        Location location = offlinePlayer.getLocation();
        if (location == null) {
            sender.sendMessage(_localizationHandler.getPlayerHasNotPlayedBefore(offlinePlayer.getName()));
            return true;
        }

        // Teleport the player to the offline player's last known location
        if (_plugin.isFolia) {
            sender.getScheduler().run(_plugin, task -> sender.teleportAsync(location), null);
        } else {
            sender.teleport(location);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        for (OfflinePlayer player : _offlinePlayers) {
            if (completions.size() == 100) {
                break;
            } else if (player.isOnline()) {
                continue;
            }

            String name = player.getName();
            if (name != null && (args.length == 0 || name.startsWith(args[0]))) {
                completions.add(name);
            }
        }

        return completions;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent eventArgs) {
        _plugin.getLogger().info(MessageFormat.format("Added player {0} to the offline player cache.", eventArgs.getPlayer().getName()));
        _offlinePlayers.add(eventArgs.getPlayer().getServer().getOfflinePlayer(eventArgs.getPlayer().getName()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent eventArgs) {
        _plugin.getLogger().info(MessageFormat.format("Removed player {0} from the offline player cache.", eventArgs.getPlayer().getName()));
        _offlinePlayers.remove(eventArgs.getPlayer().getServer().getOfflinePlayer(eventArgs.getPlayer().getName()));
    }
}
