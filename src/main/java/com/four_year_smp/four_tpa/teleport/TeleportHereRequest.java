package com.four_year_smp.four_tpa.teleport;

import java.util.UUID;

public final class TeleportHereRequest extends TeleportRequest {
    public TeleportHereRequest(UUID sender, UUID receiver) {
        super(sender, receiver);
    }
}
