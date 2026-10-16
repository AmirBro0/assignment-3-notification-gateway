package kz.assignment3.gateway;

/** One successfully simulated delivery. The body is the actual channel payload. */
public record DeliveryRecord(String channel, String recipient, String title, String body) {
}
