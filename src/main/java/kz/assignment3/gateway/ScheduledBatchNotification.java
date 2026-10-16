package kz.assignment3.gateway;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Sends one batch when send is called; scheduling itself is outside this exercise. */
public final class ScheduledBatchNotification extends Notification {
    private final List<String> recipients;
    private BatchSummary lastSummary = new BatchSummary(0, 0, 0);

    public ScheduledBatchNotification(MessageSender sender, Collection<String> recipients) {
        super(sender);
        this.recipients = List.copyOf(Objects.requireNonNull(recipients, "recipients"));
    }

    public BatchSummary getLastSummary() {
        return lastSummary;
    }

    @Override
    public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message");
        LinkedHashSet<String> targets = new LinkedHashSet<>(recipients);
        // The inherited single-recipient argument can add one more target to the batch.
        if (recipient != null && !recipient.isBlank()) {
            targets.add(recipient);
        }
        int successful = 0;
        for (String target : targets) {
            if (sender.isAvailable() && sender.sendMessage(target, "Scheduled batch", message)) {
                successful++;
            }
        }
        lastSummary = new BatchSummary(targets.size(), successful, targets.size() - successful);
        System.out.printf("Total: %d%nSuccessful: %d%nFailed: %d%n",
                lastSummary.total(), lastSummary.successful(), lastSummary.failed());
    }
}
