package taskmanager.strategy;

import taskmanager.model.interfaces.RecurrenceStrategy;

import java.time.LocalDate;
import java.util.List;

public class MonthlyRecurrence implements RecurrenceStrategy {
    private int interval;
    private int dayOfMonth;
    private LocalDate startDate;
    private LocalDate endDate;

    public MonthlyRecurrence(int interval, int dayOfMonth, LocalDate startDate, LocalDate endDate) {
        this.interval = interval;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public LocalDate nextOccurrence(LocalDate from) { return null; } // TODO:

    @Override
    public List<LocalDate> generateOccurrences() { return null; } // TODO:
}