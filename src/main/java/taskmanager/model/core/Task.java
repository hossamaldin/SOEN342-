package taskmanager.model.core;

import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import taskmanager.model.interfaces.CsvSerializable;
import taskmanager.model.interfaces.RecurrenceStrategy;
import taskmanager.model.project.Collaborator;
import taskmanager.model.project.Project;
import taskmanager.model.project.TaskAssignment;
import taskmanager.strategy.TaskOccurrence;
import taskmanager.util.CsvUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        occurrences.clear();
        if (strategy == null) return;
        for (LocalDate d : strategy.generateOccurrences()) {
            occurrences.add(new TaskOccurrence(d, this));
        }
    }

    public TaskAssignment assignToCollaborator(Collaborator collaborator) {
        if (collaborator == null) throw new IllegalArgumentException("Collaborator is required");
        if (!collaborator.canAcceptTask()) {
            throw new IllegalStateException(
                    "Collaborator has reached the open-task limit for category " + collaborator.getCategory());
        }
        String subTitle = getTitle() + " (" + collaborator.getName() + ")";
        Subtask subtask = new Subtask(subTitle, this);
        subtask.setAssignedTo(collaborator);
        collaborator.assignSubtask(subtask);
        addSubtask(subtask);
        TaskAssignment assignment = new TaskAssignment(collaborator, subtask);
        assignments.add(assignment);
        recordActivity("Assigned to collaborator: " + collaborator.getName());
        return assignment;
    }

    /**
     * CSV columns: TaskName, Description, Subtask, Status, Priority, DueDate, ProjectName,
     * ProjectDescription, Collaborator, CollaboratorCategory
     */
    @Override
    public String toCsv() {
        return toCsvRow(null, null);
    }

    /** One logical row: parent task, or a specific subtask line with collaborator context. */
    public String toCsvRow(Subtask subtask, TaskOccurrence occurrence) {
        String taskName = CsvUtil.escape(getTitle());
        String desc = CsvUtil.escape(description != null ? description : "");
        String subName = subtask != null ? CsvUtil.escape(subtask.getTitle()) : "";
        Status rowStatus = subtask != null
                ? subtask.getStatus()
                : (occurrence != null ? occurrence.getStatus() : getStatus());
        LocalDate rowDue = occurrence != null ? occurrence.getDueDate() : dueDate;
        String statusStr = rowStatus != null ? rowStatus.name() : "";
        String priStr = priority != null ? priority.name() : "";
        String dueStr = rowDue != null ? rowDue.toString() : "";
        String projName = project != null ? CsvUtil.escape(project.getName()) : "";
        String projDesc = project != null && project.getDescription() != null
                ? CsvUtil.escape(project.getDescription()) : "";
        Collaborator collab = subtask != null ? subtask.getAssignedTo() : null;
        String collabName = collab != null ? CsvUtil.escape(collab.getName()) : "";
        String collabCat = collab != null && collab.getCategory() != null ? collab.getCategory().name() : "";

        String tagsStr = tags.isEmpty() ? "" :
                CsvUtil.escape(tags.stream().map(Tag::getName).collect(Collectors.joining("|")));

        return String.join(",",
                taskName, desc, subName, statusStr, priStr, dueStr, projName, projDesc, collabName, collabCat, tagsStr);
    }

    /** All export lines for this task (occurrence and/or subtask rows). */
    public List<String> toCsvLines() {
        List<String> lines = new ArrayList<>();
        if (!occurrences.isEmpty()) {
            for (TaskOccurrence o : occurrences) {
                lines.add(toCsvRow(null, o));
            }
        } else {
            lines.add(toCsvRow(null, null));
        }
        for (Subtask st : subtasks) {
            lines.add(toCsvRow(st, null));
        }
        return lines;
    }

    @Override
    public void fromCsv(String row) {
        String[] c = CsvUtil.splitLine(row);
        if (c.length < 6) throw new IllegalArgumentException("CSV row must have at least 6 columns");
        setTitle(CsvUtil.unescape(c[0]));
        setDescription(CsvUtil.unescape(c[1]));
        if (c.length > 3 && !c[3].isBlank()) {
            setStatus(Status.valueOf(c[3].trim().toUpperCase()));
        }
        if (c.length > 4 && !c[4].isBlank()) {
            setPriority(Priority.valueOf(c[4].trim().toUpperCase()));
        }
        if (c.length > 5 && !c[5].isBlank()) {
            try {
                setDueDate(LocalDate.parse(c[5].trim()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid due date: " + c[5], e);
            }
        }
        if (c.length > 10 && !c[10].isBlank()) {
            for (String tagName : CsvUtil.unescape(c[10]).split("\\|")) {
                if (!tagName.isBlank()) addTag(new Tag(tagName.trim()));
            }
        }
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
