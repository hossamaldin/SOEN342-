package taskmanager.gateway;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.validate.ValidationException;

import taskmanager.model.core.Subtask;
import taskmanager.model.core.Task;
import taskmanager.search.SearchCriteria;
import taskmanager.search.SearchResult;
import taskmanager.service.TaskService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/***
 * Tasks without a due date are silently skipped as they are not eligible for export.
 * Subtasks  summarised inside the parent task's description, never as separate VEvents.
 */
public class ICalGateway {

    private final TaskService taskService;

    public ICalGateway(TaskService taskService) {
        this.taskService = taskService;
    }

    // -------------------------------------------------------------------------
    // Public export methods
    // -------------------------------------------------------------------------

    /** a) Export a single task to an .ics file. Returns the exported task in a list. */
    public List<Task> exportSingleTask(String taskName, String filePath) throws IOException {
        Task task = taskService.findTaskByName(taskName);
        if (task == null)
            throw new IllegalArgumentException("Task not found: " + taskName);
        if (task.getDueDate() == null)
            throw new IllegalArgumentException("Task has no due date and is not eligible for export.");

        List<Task> exported = List.of(task);
        writeIcsFile(exported, filePath);
        return exported;
    }

    /** Export all tasks with a due date in a project. */
    public List<Task> exportProject(String projectName, String filePath) throws IOException {
        if (taskService.findProjectByName(projectName) == null)
            throw new IllegalArgumentException("Project not found: " + projectName);

        List<Task> eligible = taskService.getTasks().stream()
                .filter(t -> t.getProject() != null
                        && t.getProject().getName().equalsIgnoreCase(projectName)
                        && t.getDueDate() != null)
                .collect(Collectors.toList());

        writeIcsFile(eligible, filePath);
        return eligible;
    }

    /** c) Export a filtered list of tasks.  */
    public List<Task> exportFiltered(SearchCriteria criteria, String filePath) throws IOException {
        SearchResult result = taskService.searchTasks(criteria);
        List<Task> eligible = result.getTasks().stream()
                .filter(t -> t.getDueDate() != null)
                .collect(Collectors.toList());

        writeIcsFile(eligible, filePath);
        return eligible;
    }

    // -------------------------------------------------------------------------
    //build  iCal4j Calendar and write it to disk
    // -------------------------------------------------------------------------

    private void writeIcsFile(List<Task> tasks, String filePath) throws IOException {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//TaskManager//SOEN342//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.PUBLISH);

        for (Task task : tasks) {
            if (task.getDueDate() == null) continue;
            calendar.getComponents().add(buildVEvent(task));
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, fos);
        } catch (ValidationException e) {
            throw new IOException("iCal4j validation error: " + e.getMessage(), e);
        }
    }


     // Builds a single VEVENT from a Task.

    private VEvent buildVEvent(Task task) {
        java.util.Date startUtil = java.util.Date.from(
                task.getDueDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        java.util.Date endUtil = java.util.Date.from(
                task.getDueDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));

        Date icalStart = new Date(startUtil);
        Date icalEnd   = new Date(endUtil);

        VEvent event = new VEvent(icalStart, icalEnd, task.getTitle());

        event.getProperties().add(new Uid(UUID.randomUUID() + "@taskmanager"));

        // Description
        event.getProperties().add(new Description(buildDescription(task)));

        // Status
        event.getProperties().add(mapStatus(task));

        // Priority: 1 = HIGH, 5 = MEDIUM, 9 = LOW
        event.getProperties().add(mapPriority(task));

        // Project name
        if (task.getProject() != null) {
            event.getProperties().add(new Categories(task.getProject().getName()));
        }

        return event;
    }

    // Builds the DESCRIPTION field.

    private String buildDescription(Task task) {
        StringBuilder sb = new StringBuilder();

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            sb.append(task.getDescription());
        }

        if (task.getProject() != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Project: ").append(task.getProject().getName());
        }

        sb.append("\nStatus: ").append(task.getStatus());
        sb.append("\nPriority: ").append(task.getPriority());

        List<Subtask> subtasks = task.getSubtasks();
        if (!subtasks.isEmpty()) {
            sb.append("\nSubtasks (").append(subtasks.size()).append("):");
            for (Subtask st : subtasks) {
                sb.append("\n  - ")
                  .append(st.getTitle())
                  .append(" [").append(st.getStatus()).append("]");
            }
        }

        return sb.toString();
    }

    // Maps domain Status → iCal4j VEVENT Status constant

    private Status mapStatus(Task task) {
        taskmanager.model.enums.Status domainStatus = task.getStatus();
        if (domainStatus == taskmanager.model.enums.Status.CANCELLED) {
            return Status.VEVENT_CANCELLED;
        }
        return Status.VEVENT_CONFIRMED;
    }

    /** Maps domain Priority → iCal4j Priority property (1=high, 5=medium, 9=low). */
    private Priority mapPriority(Task task) {
        taskmanager.model.enums.Priority domainPriority = task.getPriority();
        if (domainPriority == taskmanager.model.enums.Priority.HIGH)   return new Priority(1);
        if (domainPriority == taskmanager.model.enums.Priority.MEDIUM) return new Priority(5);
        return new Priority(9);
    }
}
