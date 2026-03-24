package taskmanager.strategy;

import taskmanager.model.interfaces.RecurrenceStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DailyRecurrence implements RecurrenceStrategy {
    private final int interval;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public DailyRecurrence(int interval, LocalDate startDate, LocalDate endDate) {
        if (interval < 1) throw new IllegalArgumentException("interval must be >= 1");
        this.interval = interval;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public LocalDate nextOccurrence(LocalDate from) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) return null;
        LocalDate cursor = from.isBefore(startDate) ? startDate : from;
        long days = ChronoUnit.DAYS.between(startDate, cursor);
        long rem = ((days % interval) + interval) % interval;
        LocalDate candidate = rem == 0 ? cursor : cursor.plusDays(interval - rem);
        if (candidate.isBefore(startDate)) candidate = startDate;
        if (candidate.isAfter(endDate)) return null;
        return candidate;
    }

    @Override
    public List<LocalDate> generateOccurrences() {
        List<LocalDate> out = new ArrayList<>();
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) return out;
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            out.add(d);
            d = d.plusDays(interval);
        }
        return out;
    }
}
