package taskmanager.strategy;

import taskmanager.model.interfaces.RecurrenceStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class WeeklyRecurrence implements RecurrenceStrategy {
    private final int interval;
    private final Set<DayOfWeek> weekdays;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public WeeklyRecurrence(int interval, List<DayOfWeek> weekdays, LocalDate startDate, LocalDate endDate) {
        if (interval < 1) throw new IllegalArgumentException("interval must be >= 1");
        this.interval = interval;
        this.weekdays = weekdays == null || weekdays.isEmpty()
                ? EnumSet.noneOf(DayOfWeek.class)
                : EnumSet.copyOf(weekdays);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private static LocalDate startOfWeekMonday(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private boolean inScheduledWeek(LocalDate d) {
        long weeksBetween = ChronoUnit.WEEKS.between(startOfWeekMonday(startDate), startOfWeekMonday(d));
        return weeksBetween % interval == 0;
    }

    @Override
    public LocalDate nextOccurrence(LocalDate from) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate) || weekdays.isEmpty()) return null;
        List<LocalDate> all = generateOccurrences();
        for (LocalDate d : all) {
            if (!d.isBefore(from)) return d;
        }
        return null;
    }

    @Override
    public List<LocalDate> generateOccurrences() {
        List<LocalDate> out = new ArrayList<>();
        if (startDate == null || endDate == null || startDate.isAfter(endDate) || weekdays.isEmpty()) return out;
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            if (weekdays.contains(d.getDayOfWeek()) && inScheduledWeek(d) && !d.isBefore(startDate)) {
                out.add(d);
            }
            d = d.plusDays(1);
        }
        return out;
    }

    public List<DayOfWeek> getWeekdays() {
        return new ArrayList<>(weekdays);
    }

    public int getInterval() { return interval; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
