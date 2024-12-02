package com.four_year_smp.four_tpa.teleport;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ITeleportManager {
    public void add(TeleportRequest request);

    public void accept(UUID receiver);

    public TeleportRequest cancel(UUID sender);

    public TeleportRequest getSender(UUID sender);

    public TeleportRequest getRequest(UUID receiver, UUID sender);

    public ArrayList<TeleportRequest> getRequests(UUID receiver);

    public void teleport(Player player, Location location);

    public void delay(Player player, int delay, Callable<Location> grabLocation, Consumer<Location> callback);

    public Location getLastLocation(UUID playerId);

    public int getTimeout();

    public void processRequests();

    public void dispose();
}
