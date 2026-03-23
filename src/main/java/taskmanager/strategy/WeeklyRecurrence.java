package taskmanager.strategy;

import taskmanager.model.interfaces.RecurrenceStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class WeeklyRecurrence implements RecurrenceStrategy {
    private int interval;
    private List<DayOfWeek> weekdays;
    private LocalDate startDate;
    private LocalDate endDate;

    public WeeklyRecurrence(int interval, List<DayOfWeek> weekdays, LocalDate startDate, LocalDate endDate) {
        this.interval = interval;
        this.weekdays = weekdays;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public LocalDate nextOccurrence(LocalDate from) { return null; } // TODO:

    @Override
    public List<LocalDate> generateOccurrences() { return null; } // TODO:
}