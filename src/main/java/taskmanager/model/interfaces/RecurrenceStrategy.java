package taskmanager.model.interfaces;

import java.time.LocalDate;
import java.util.List;

public interface RecurrenceStrategy {
    LocalDate nextOccurrence(LocalDate from);
    List<LocalDate> generateOccurrences();
}