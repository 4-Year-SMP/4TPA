package com.four_year_smp.four_tpa.teleport;

import java.util.Date;
import java.util.UUID;

public class TeleportRequest {
    private final UUID _sender;
    private final UUID _receiver;
    private final Date _sentAt;
    private boolean _accepted;

    public TeleportRequest(UUID sender, UUID receiver) {
        _sender = sender;
        _receiver = receiver;
        _sentAt = new Date();
    }

    public boolean hasExpired(int timeOutMilliseconds) {
        return new Date().getTime() - _sentAt.getTime() > timeOutMilliseconds;
    }

    public UUID getSender() {
        return _sender;
    }

    public UUID getReceiver() {
        return _receiver;
    }

    public boolean isAccepted() {
        return _accepted;
    }

    public void accept() {
        _accepted = true;
    }
}
