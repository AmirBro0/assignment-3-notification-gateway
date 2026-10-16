package kz.assignment3.gateway;

public final class TelegramMessageSender extends AbstractMessageSender {
    @Override
    public String getChannelName() {
        return "Telegram";
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (body == null) {
            return false;
        }
        // Telegram-style Markdown formatting is simulated, not sent to an API.
        String formatted = "*" + title + "*\n" + body;
        return deliver(recipient, title, formatted);
    }
}
