package com.four_year_smp.four_tpa.teleport;

import java.util.ArrayList;
import java.util.UUID;
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

    public int getTimeout();

    public void processRequests();

    public void dispose();
}
