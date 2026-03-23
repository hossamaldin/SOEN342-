package taskmanager.model.core;

import java.time.LocalDateTime;

public class ActivityEntry {
    private LocalDateTime timestamp;
    private String description;

    public ActivityEntry(String description) {
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return "[" + timestamp + "] " + description; }
}