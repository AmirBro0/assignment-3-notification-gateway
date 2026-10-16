package kz.assignment3.gateway;

public final class EmailMessageSender extends AbstractMessageSender {
    @Override
    public String getChannelName() {
        return "Email";
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (body == null) {
            return false;
        }
        return deliver(recipient, title, body + "\n\n-- Notification Gateway");
    }
}
