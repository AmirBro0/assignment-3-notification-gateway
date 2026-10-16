package kz.assignment3.gateway;

import java.util.Objects;

/** Added after the original abstractions, without changing any sender class. */
public final class MarketingNotification extends Notification {
    public MarketingNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message");
        if (!sender.sendMessage(recipient, "Marketing offer", "[PROMO] " + message)) {
            System.out.println("Marketing notification failed for " + recipient);
        }
    }
}
