package taskmanager.search;

import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class SearchCriteria {
    private String nameMatch;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private DayOfWeek dayOfWeek;
    private Status taskStatus;
    private Priority priority;

    public SearchCriteria() {}

    public boolean isEmpty() {
        return nameMatch == null && periodStart == null && periodEnd == null
                && dayOfWeek == null && taskStatus == null && priority == null;
    }

    public String getNameMatch() { return nameMatch; }
    public void setNameMatch(String nameMatch) { this.nameMatch = nameMatch; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Status getTaskStatus() { return taskStatus; }
    public void setTaskStatus(Status taskStatus) { this.taskStatus = taskStatus; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}