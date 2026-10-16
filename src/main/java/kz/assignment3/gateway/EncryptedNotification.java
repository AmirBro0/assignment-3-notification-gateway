package kz.assignment3.gateway;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public final class EncryptedNotification extends Notification {
    public EncryptedNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message");
        String encoded = Base64.getEncoder()
                .encodeToString(message.getBytes(StandardCharsets.UTF_8));
        if (!sender.sendMessage(recipient, "Encrypted", "ENC:" + encoded)) {
            System.out.println("Encrypted notification failed for " + recipient);
        }
    }
}
