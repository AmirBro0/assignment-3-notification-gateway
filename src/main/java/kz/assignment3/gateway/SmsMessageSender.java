package kz.assignment3.gateway;

public final class SmsMessageSender extends AbstractMessageSender {
    public static final int MAX_CHARACTERS = 160;

    @Override
    public String getChannelName() {
        return "SMS";
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        // An SMS has no separate subject. Count Unicode code points, not UTF-16 units.
        if (body == null || body.codePointCount(0, body.length()) > MAX_CHARACTERS) {
            return false;
        }
        return deliver(recipient, "", body);
    }
}
