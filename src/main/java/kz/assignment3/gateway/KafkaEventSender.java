package kz.assignment3.gateway;

public final class KafkaEventSender extends AbstractMessageSender {
    @Override
    public String getChannelName() {
        return "Kafka";
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (body == null) {
            return false;
        }
        String event = "event{recipient=" + recipient + ", title=" + title + ", body=" + body + "}";
        return deliver(recipient, title, event);
    }
}
