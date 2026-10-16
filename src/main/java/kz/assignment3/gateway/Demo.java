package kz.assignment3.gateway;

import java.util.List;

public final class Demo {
    private Demo() {
    }

    public static void main(String[] args) {
        TelegramMessageSender telegram = new TelegramMessageSender();
        EmailMessageSender email = new EmailMessageSender();
        SmsMessageSender sms = new SmsMessageSender();
        PushNotificationSender push = new PushNotificationSender();

        System.out.println("1. Urgent + Telegram");
        UrgentNotification urgent = new UrgentNotification(telegram, sms);
        urgent.send("@alice", "Server needs attention");

        System.out.println("\n2. Encrypted + Email");
        new EncryptedNotification(email).send("bob@example.com", "Private message");

        System.out.println("\n3. Scheduled batch + SMS");
        ScheduledBatchNotification batch = new ScheduledBatchNotification(
                sms, List.of("+77010000001", "+77010000002", "+77010000003"));
        batch.send(null, "Your appointment is tomorrow");

        System.out.println("\n4. Same object: Telegram -> Email");
        urgent.send("@alice", "First channel");
        urgent.setSender(email);
        urgent.send("alice@example.com", "Second channel");

        System.out.println("\n5. Telegram unavailable -> SMS fallback");
        telegram.setAvailable(false);
        new UrgentNotification(telegram, sms).send("+77010000004", "Use backup channel");

        System.out.println("\n6. Marketing + Push");
        new MarketingNotification(push).send("device-123", "Autumn sale starts today");

        System.out.println("\nExtra: Kafka event sender");
        new MarketingNotification(new KafkaEventSender()).send("customer-42", "Event example");
    }
}
