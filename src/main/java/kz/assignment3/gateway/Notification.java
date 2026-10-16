package kz.assignment3.gateway;

import java.util.Objects;

/** The abstraction side of the Bridge holds a MessageSender interface reference. */
public abstract class Notification {
    protected MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    public void setSender(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    public abstract void send(String recipient, String message);
}
