package taskmanager.strategy;

import taskmanager.model.interfaces.RecurrenceStrategy;

import java.time.LocalDate;
import java.util.List;

public class DailyRecurrence implements RecurrenceStrategy {
    private int interval;
    private LocalDate startDate;
    private LocalDate endDate;

    public DailyRecurrence(int interval, LocalDate startDate, LocalDate endDate) {
        this.interval = interval;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public LocalDate nextOccurrence(LocalDate from) { return null; } // TODO:

    @Override
    public List<LocalDate> generateOccurrences() { return null; } // TODO:
}