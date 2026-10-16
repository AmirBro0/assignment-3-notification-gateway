package kz.assignment3.gateway;

import java.util.Objects;

public final class UrgentNotification extends Notification {
    private MessageSender fallbackSender;

    public UrgentNotification(MessageSender sender) {
        this(sender, null);
    }

    public UrgentNotification(MessageSender sender, MessageSender fallbackSender) {
        super(sender);
        this.fallbackSender = fallbackSender;
    }

    public void setFallbackSender(MessageSender fallbackSender) {
        this.fallbackSender = fallbackSender;
    }

    @Override
    public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message");
        String urgentBody = "[URGENT] " + message;
        if (sender.isAvailable() && sender.sendMessage(recipient, "Urgent", urgentBody)) {
            return;
        }
        if (fallbackSender != null && fallbackSender != sender
                && fallbackSender.isAvailable()
                && fallbackSender.sendMessage(recipient, "Urgent", urgentBody)) {
            System.out.println("Fallback delivered via " + fallbackSender.getChannelName());
            return;
        }
        System.out.println("Urgent notification failed for " + recipient);
    }
}
