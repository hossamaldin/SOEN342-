package taskmanager.strategy;

import taskmanager.model.core.Task;
import taskmanager.model.enums.Status;
import java.time.LocalDate;

public class TaskOccurrence {
    private LocalDate dueDate;
    private Status status;
    private Task parentTask;

    public TaskOccurrence(LocalDate dueDate, Task parentTask) {
        this.dueDate = dueDate;
        this.parentTask = parentTask;
        this.status = Status.OPEN;
    }

    public LocalDate getDueDate() { return dueDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Task getParentTask() { return parentTask; }
}