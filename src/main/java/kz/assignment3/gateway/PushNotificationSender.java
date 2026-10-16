package kz.assignment3.gateway;

/** Added after the original four senders, without changing any Notification class. */
public final class PushNotificationSender extends AbstractMessageSender {
    @Override
    public String getChannelName() {
        return "Push";
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (body == null) {
            return false;
        }
        return deliver(recipient, title, "PUSH: " + title + "\n" + body);
    }
}
