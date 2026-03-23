package taskmanager.model.core;

import taskmanager.model.enums.Priority;
import taskmanager.model.interfaces.CsvSerializable;
import taskmanager.model.interfaces.RecurrenceStrategy;
import taskmanager.model.project.Collaborator;
import taskmanager.model.project.Project;
import taskmanager.model.project.TaskAssignment;
import taskmanager.strategy.TaskOccurrence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Task extends WorkItem implements CsvSerializable {
    private String description;
    private LocalDate creationDate;
    private LocalDate dueDate;
    private Priority priority;
    private Project project;
    private List<Subtask> subtasks;
    private List<Tag> tags;
    private List<ActivityEntry> activityLog;
    private List<TaskOccurrence> occurrences;
    private List<TaskAssignment> assignments;
    private RecurrenceStrategy recurrenceStrategy;

    public Task(String title, String description, LocalDate dueDate, Priority priority) {
        super(title);
        this.description = description;
        this.creationDate = LocalDate.now();
        this.dueDate = dueDate;
        this.priority = priority;
        this.subtasks = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.activityLog = new ArrayList<>();
        this.occurrences = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) { subtasks.add(subtask); }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) tags.add(tag);
    }

    public void recordActivity(String description) {
        activityLog.add(new ActivityEntry(description));
    }

    public void setRecurring(RecurrenceStrategy strategy) {
        this.recurrenceStrategy = strategy;
        // TODO:  generate occurrences from strategy
    }

    public TaskAssignment assignToCollaborator(Collaborator collaborator) {
        // TODO:  implement collaborator assignment
        return null;
    }

    @Override
    public String toCsv() {
        String subtaskValue = subtasks == null || subtasks.isEmpty()
                ? ""
                : subtasks.stream()
                .map(Subtask::getTitle)
                .collect(Collectors.joining(" | "));

        String collaboratorValue = assignments == null || assignments.isEmpty()
                ? ""
                : assignments.stream()
                .map(TaskAssignment::getCollaborator)
                .filter(c -> c != null)
                .map(Collaborator::getName)
                .distinct()
                .collect(Collectors.joining(" | "));

        String collaboratorCategoryValue = assignments == null || assignments.isEmpty()
                ? ""
                : assignments.stream()
                .map(TaskAssignment::getCollaborator)
                .filter(c -> c != null)
                .map(c -> c.getCategory().name())
                .distinct()
                .collect(Collectors.joining(" | "));

        String projectName = project != null ? project.getName() : "";
        String projectDescription = project != null ? project.getDescription() : "";

        return String.join(",",
                escapeCsv(getTitle()),
                escapeCsv(description),
                escapeCsv(subtaskValue),
                escapeCsv(getStatus().name()),
                escapeCsv(priority != null ? priority.name() : ""),
                escapeCsv(dueDate != null ? dueDate.toString() : ""),
                escapeCsv(projectName),
                escapeCsv(projectDescription),
                escapeCsv(collaboratorValue),
                escapeCsv(collaboratorCategoryValue)
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @Override
    public void fromCsv(String row) {
        // TODO:   implement CSV deserialization
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getCreationDate() { return creationDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public List<Subtask> getSubtasks() { return subtasks; }
    public List<Tag> getTags() { return tags; }
    public List<ActivityEntry> getActivityLog() { return activityLog; }
    public List<TaskOccurrence> getOccurrences() { return occurrences; }
    public List<TaskAssignment> getAssignments() { return assignments; }
    public RecurrenceStrategy getRecurrenceStrategy() { return recurrenceStrategy; }
}
