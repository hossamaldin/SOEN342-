package taskmanager.strategy;

import taskmanager.model.interfaces.RecurrenceStrategy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MonthlyRecurrence implements RecurrenceStrategy {
    private final int interval;
    private final int dayOfMonth;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public MonthlyRecurrence(int interval, int dayOfMonth, LocalDate startDate, LocalDate endDate) {
        if (interval < 1) throw new IllegalArgumentException("interval must be >= 1");
        if (dayOfMonth < 1 || dayOfMonth > 31) throw new IllegalArgumentException("dayOfMonth must be 1..31");
        this.interval = interval;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private static LocalDate safeDay(YearMonth ym, int dom) {
        int last = ym.lengthOfMonth();
        int d = Math.min(dom, last);
        return ym.atDay(d);
    }

    @Override
    public LocalDate nextOccurrence(LocalDate from) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) return null;
        List<LocalDate> all = generateOccurrences();
        for (LocalDate d : all) {
            if (!d.isBefore(from)) return d;
        }
        return null;
    }

    @Override
    public List<LocalDate> generateOccurrences() {
        List<LocalDate> out = new ArrayList<>();
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) return out;
        YearMonth ym = YearMonth.from(startDate);
        while (true) {
            LocalDate occ = safeDay(ym, dayOfMonth);
            if (occ.isAfter(endDate)) break;
            if (!occ.isBefore(startDate)) {
                out.add(occ);
            }
            ym = ym.plusMonths(interval);
        }
        return out;
    }

    public int getInterval() { return interval; }
    public int getDayOfMonth() { return dayOfMonth; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
