package kz.assignment3.gateway;

import java.util.ArrayList;
import java.util.List;

/** Shared simulation plumbing; channel-specific formatting stays in concrete senders. */
abstract class AbstractMessageSender implements MessageSender {
    private boolean available = true;
    private final List<DeliveryRecord> deliveries = new ArrayList<>();

    public final void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public final boolean isAvailable() {
        return available;
    }

    public final List<DeliveryRecord> getDeliveries() {
        return List.copyOf(deliveries);
    }

    protected final boolean deliver(String recipient, String title, String body) {
        if (!available || recipient == null || recipient.isBlank() || body == null) {
            return false;
        }
        DeliveryRecord record = new DeliveryRecord(getChannelName(), recipient, title, body);
        deliveries.add(record);
        System.out.printf("[%s] To: %s | Title: %s | Body: %s%n",
                record.channel(), record.recipient(), record.title(), record.body());
        return true;
    }
}
