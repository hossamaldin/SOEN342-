package taskmanager.service;

import taskmanager.model.core.Subtask;
import taskmanager.model.core.Tag;
import taskmanager.model.core.Task;
import taskmanager.model.enums.CollaboratorCategory;
import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import taskmanager.model.project.Collaborator;
import taskmanager.model.project.Project;
import taskmanager.model.project.TaskAssignment;
import taskmanager.search.SearchResult;
import taskmanager.util.CsvUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvService {
    public static final String CSV_HEADER =
            "TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory,Tags";

    private final TaskService taskService;

    public CsvService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void exportTasks(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Task task : taskService.getTasks()) {
            for (String row : task.toCsvLines()) {
                lines.add(row);
            }
        }
        Files.write(Path.of(filePath), lines, StandardCharsets.UTF_8);
    }

    public void exportSearchResult(SearchResult result, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        if (result != null && result.getTasks() != null) {
            for (Task task : result.getTasks()) {
                for (String row : task.toCsvLines()) {
                    lines.add(row);
                }
            }
        }
        Files.write(Path.of(filePath), lines, StandardCharsets.UTF_8);
    }

    public void importTasks(String filePath) throws IOException {
        List<String> raw = Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8);
        for (String line : raw) {
            if (line.isBlank()) continue;
            if (line.trim().equalsIgnoreCase(CSV_HEADER)) continue;
            if (isHeaderLine(line)) continue;
            importRow(line);
        }
    }

    private static boolean isHeaderLine(String line) {
        String[] p = CsvUtil.splitLine(line);
        return p.length > 0 && "TaskName".equalsIgnoreCase(p[0].trim());
    }

    private void importRow(String line) {
        String[] p = CsvUtil.splitLine(line);
        String[] c = new String[11];
        for (int i = 0; i < 11; i++) {
            c[i] = i < p.length ? p[i] : "";
        }
        String taskName = CsvUtil.unescape(c[0]);
        String description = CsvUtil.unescape(c[1]);
        String subtaskTitle = CsvUtil.unescape(c[2]);
        Status status = parseStatus(c[3]);
        Priority priority = parsePriority(c[4]);
        LocalDate dueDate = parseDate(c[5]);
        String projectName = CsvUtil.unescape(c[6]);
        String projectDescription = CsvUtil.unescape(c[7]);
        String collaboratorName = CsvUtil.unescape(c[8]);
        CollaboratorCategory category = parseCategory(c[9]);
        String tagsRaw = CsvUtil.unescape(c[10]);

        Project project = null;
        if (projectName != null && !projectName.isBlank()) {
            project = taskService.findOrCreateProject(projectName, projectDescription);
        }

        Task task = taskService.findTaskByTitleAndDueDate(taskName, dueDate);
        if (task == null) {
            task = new Task(taskName, description, dueDate, priority);
            task.setStatus(status);
            if (project != null) {
                task.setProject(project);
            }
            taskService.addTask(task);
            task.recordActivity("Imported from CSV");
        } else {
            if (description != null && !description.isBlank()) {
                task.setDescription(description);
            }
            task.setStatus(status);
            task.setPriority(priority);
            if (project != null) {
                task.setProject(project);
            }
        }

        if (tagsRaw != null && !tagsRaw.isBlank()) {
            for (String tagName : tagsRaw.split("\\|")) {
                if (!tagName.isBlank()) task.addTag(new Tag(tagName.trim()));
            }
        }

        if (subtaskTitle != null && !subtaskTitle.isBlank()) {
            Subtask sub = findSubtaskByTitle(task, subtaskTitle);
            if (sub == null) {
                sub = new Subtask(subtaskTitle, task);
                task.addSubtask(sub);
            }
            sub.setStatus(status);
            if (collaboratorName != null && !collaboratorName.isBlank() && project != null) {
                Collaborator collab = taskService.findOrCreateCollaborator(project, collaboratorName, category);
                if (sub.getAssignedTo() == null) {
                    if (!collab.canAcceptTask()) {
                        throw new IllegalStateException(
                                "Cannot assign collaborator " + collaboratorName + ": open-task limit reached");
                    }
                    sub.setAssignedTo(collab);
                    collab.assignSubtask(sub);
                    task.getAssignments().add(new TaskAssignment(collab, sub));
                }
            }
        } else if (collaboratorName != null && !collaboratorName.isBlank()) {
            if (project == null) {
                throw new IllegalArgumentException("Collaborator requires a project name in CSV");
            }
            Collaborator collab = taskService.findOrCreateCollaborator(project, collaboratorName, category);
            String expectedTitle = task.getTitle() + " (" + collab.getName() + ")";
            boolean exists = task.getSubtasks().stream()
                    .anyMatch(s -> collab.equals(s.getAssignedTo()) && expectedTitle.equals(s.getTitle()));
            if (!exists) {
                if (!collab.canAcceptTask()) {
                    throw new IllegalStateException(
                            "Cannot assign collaborator " + collaboratorName + ": open-task limit reached");
                }
                task.assignToCollaborator(collab);
            }
        }
    }

    private static Subtask findSubtaskByTitle(Task task, String title) {
        for (Subtask s : task.getSubtasks()) {
            if (s.getTitle().equalsIgnoreCase(title)) return s;
        }
        return null;
    }

    private static Status parseStatus(String s) {
        if (s == null || s.isBlank()) return Status.OPEN;
        return Status.valueOf(s.trim().toUpperCase());
    }

    private static Priority parsePriority(String s) {
        if (s == null || s.isBlank()) return Priority.MEDIUM;
        return Priority.valueOf(s.trim().toUpperCase());
    }

    private static CollaboratorCategory parseCategory(String s) {
        if (s == null || s.isBlank()) return CollaboratorCategory.JUNIOR;
        return CollaboratorCategory.valueOf(s.trim().toUpperCase());
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + s, e);
        }
    }
}
