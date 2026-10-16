package kz.assignment3.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationGatewayTest {
    @Test
    void telegramFormatsTitleAndBody() {
        TelegramMessageSender sender = new TelegramMessageSender();
        assertTrue(sender.sendMessage("@alice", "Alert", "Hello"));
        assertEquals("*Alert*\nHello", sender.getDeliveries().get(0).body());
    }

    @Test
    void emailKeepsTitleAndAddsSignature() {
        EmailMessageSender sender = new EmailMessageSender();
        assertTrue(sender.sendMessage("a@example.com", "Subject", "Hello"));
        DeliveryRecord delivered = sender.getDeliveries().get(0);
        assertEquals("Subject", delivered.title());
        assertTrue(delivered.body().endsWith("-- Notification Gateway"));
    }

    @Test
    void smsAcceptsExactly160UnicodeCharacters() {
        SmsMessageSender sender = new SmsMessageSender();
        assertTrue(sender.sendMessage("+7701", "ignored", "🙂".repeat(160)));
        assertEquals(1, sender.getDeliveries().size());
        assertEquals("", sender.getDeliveries().get(0).title());
    }

    @Test
    void smsRejectsMessageOver160Characters() {
        SmsMessageSender sender = new SmsMessageSender();
        assertFalse(sender.sendMessage("+7701", "ignored", "x".repeat(161)));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test
    void kafkaPublishesSimulatedEvent() {
        KafkaEventSender sender = new KafkaEventSender();
        assertTrue(sender.sendMessage("customer-1", "Offer", "Sale"));
        assertEquals("event{recipient=customer-1, title=Offer, body=Sale}",
                sender.getDeliveries().get(0).body());
    }

    @Test
    void unavailableProviderDoesNotDeliver() {
        TelegramMessageSender sender = new TelegramMessageSender();
        sender.setAvailable(false);
        assertFalse(sender.sendMessage("@alice", "Alert", "Hello"));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test
    void urgentUsesAvailablePrimary() {
        TelegramMessageSender primary = new TelegramMessageSender();
        SmsMessageSender fallback = new SmsMessageSender();
        new UrgentNotification(primary, fallback).send("@alice", "Check system");
        assertTrue(primary.getDeliveries().get(0).body().contains("[URGENT] Check system"));
        assertTrue(fallback.getDeliveries().isEmpty());
    }

    @Test
    void urgentFallsBackThroughInterfaceWhenPrimaryIsUnavailable() {
        TelegramMessageSender primary = new TelegramMessageSender();
        primary.setAvailable(false);
        SmsMessageSender fallback = new SmsMessageSender();
        new UrgentNotification(primary, fallback).send("+7701", "Check system");
        assertTrue(primary.getDeliveries().isEmpty());
        assertEquals("[URGENT] Check system", fallback.getDeliveries().get(0).body());
    }

    @Test
    void urgentFallsBackWhenAvailablePrimaryRejectsDelivery() {
        MessageSender rejectingPrimary = new MessageSender() {
            @Override
            public boolean sendMessage(String recipient, String title, String body) {
                return false;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String getChannelName() {
                return "Rejecting test provider";
            }
        };
        SmsMessageSender fallback = new SmsMessageSender();
        new UrgentNotification(rejectingPrimary, fallback).send("+7701", "Retry me");
        assertEquals("[URGENT] Retry me", fallback.getDeliveries().get(0).body());
    }

    @Test
    void urgentFailsCleanlyWhenBothProvidersAreUnavailable() {
        TelegramMessageSender primary = new TelegramMessageSender();
        SmsMessageSender fallback = new SmsMessageSender();
        primary.setAvailable(false);
        fallback.setAvailable(false);
        new UrgentNotification(primary, fallback).send("+7701", "Check system");
        assertTrue(primary.getDeliveries().isEmpty());
        assertTrue(fallback.getDeliveries().isEmpty());
    }

    @Test
    void encryptedNotificationTransformsUtf8Message() {
        EmailMessageSender sender = new EmailMessageSender();
        new EncryptedNotification(sender).send("a@example.com", "Секрет");
        String payload = sender.getDeliveries().get(0).body();
        String encoded = payload.substring("ENC:".length(), payload.indexOf("\n\n--"));
        assertEquals("Секрет", new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8));
        assertFalse(payload.contains("Секрет"));
    }

    @Test
    void batchSendsToEveryRecipientAndReportsFailures() {
        SmsMessageSender sender = new SmsMessageSender();
        ScheduledBatchNotification batch = new ScheduledBatchNotification(
                sender, List.of("+7701", "", "+7702"));
        batch.send(null, "Reminder");
        assertEquals(new BatchSummary(3, 2, 1), batch.getLastSummary());
        assertEquals(2, sender.getDeliveries().size());
    }

    @Test
    void batchCanAddOneRecipientWithoutDuplicatingExistingOnes() {
        SmsMessageSender sender = new SmsMessageSender();
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sender, List.of("+7701", "+7702"));
        batch.send("+7702", "Reminder");
        assertEquals(new BatchSummary(2, 2, 0), batch.getLastSummary());
    }

    @Test
    void batchReportsAllFailuresWhenProviderIsUnavailable() {
        SmsMessageSender sender = new SmsMessageSender();
        sender.setAvailable(false);
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sender, List.of("+7701", "+7702"));
        batch.send(null, "Reminder");
        assertEquals(new BatchSummary(2, 0, 2), batch.getLastSummary());
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test
    void sameNotificationSwitchesSenderAtRuntime() {
        TelegramMessageSender telegram = new TelegramMessageSender();
        EmailMessageSender email = new EmailMessageSender();
        UrgentNotification notification = new UrgentNotification(telegram);
        notification.send("@alice", "First");
        notification.setSender(email);
        notification.send("alice@example.com", "Second");
        assertEquals(1, telegram.getDeliveries().size());
        assertEquals(1, email.getDeliveries().size());
        assertTrue(email.getDeliveries().get(0).body().contains("[URGENT] Second"));
    }

    @Test
    void newPushSenderWorksWithOriginalNotificationTypes() {
        PushNotificationSender push = new PushNotificationSender();
        new UrgentNotification(push).send("device-1", "Urgent message");
        new EncryptedNotification(push).send("device-2", "Private message");
        new ScheduledBatchNotification(push, List.of("device-3", "device-4"))
                .send(null, "Batch message");
        assertEquals(4, push.getDeliveries().size());
        assertTrue(push.getDeliveries().stream().allMatch(d -> d.body().startsWith("PUSH: ")));
    }

    @Test
    void newMarketingTypeWorksWithExistingSenders() {
        EmailMessageSender email = new EmailMessageSender();
        SmsMessageSender sms = new SmsMessageSender();
        MarketingNotification notification = new MarketingNotification(email);
        notification.send("a@example.com", "Weekend sale");
        notification.setSender(sms);
        notification.send("+7701", "Weekend sale");
        assertTrue(email.getDeliveries().get(0).body().contains("[PROMO] Weekend sale"));
        assertEquals("[PROMO] Weekend sale", sms.getDeliveries().get(0).body());
    }

    @Test
    void blankRecipientIsAFailedDelivery() {
        PushNotificationSender sender = new PushNotificationSender();
        assertFalse(sender.sendMessage(" ", "Alert", "Hello"));
        assertTrue(sender.getDeliveries().isEmpty());
    }
}
