package taskmanager;

import taskmanager.gateway.ICalGateway;
import taskmanager.model.core.Tag;
import taskmanager.model.core.Task;
import taskmanager.model.core.User;
import taskmanager.model.enums.CollaboratorCategory;
import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import taskmanager.model.project.Collaborator;
import taskmanager.model.interfaces.RecurrenceStrategy;
import taskmanager.model.project.Project;
import taskmanager.search.SearchCriteria;
import taskmanager.search.SearchResult;
import taskmanager.service.CsvService;
import taskmanager.service.TaskService;
import taskmanager.strategy.DailyRecurrence;
import taskmanager.strategy.MonthlyRecurrence;
import taskmanager.strategy.WeeklyRecurrence;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static TaskService taskService;
    private static ICalGateway iCalGateway;
    private static CsvService csvService;
    private static Scanner scanner;

    public static void main(String[] args) {
        User user = new User("Default User");
        taskService = new TaskService(user);
        iCalGateway = new ICalGateway(taskService);
        csvService = new CsvService(taskService);
        scanner = new Scanner(System.in);

        System.out.println("=== Personal Task Management System ===");
        System.out.println("Welcome, " + user.getName() + "!\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": createTask(); break;
                case "2": viewAllTasks(); break;
                case "3": updateTask(); break;
                case "4": markTaskComplete(); break;
                case "5": cancelTask(); break;
                case "6": createProject(); break;
                case "7": assignTaskToProject(); break;
                case "8": addSubtask(); break;
                case "9":  searchTasks(); break;
                case "10": exportAllTasksCsv(); break;
                case "11": importTasksCsv(); break;
                case "12": setRecurringTask(); break;
                case "13": addCollaborator(); break;
                case "14": assignTaskToCollaboratorMenu(); break;
                case "15": exportSearchResultsCsv(); break;
                case "16": exportSingleTaskToICal(); break;
                case "17": exportProjectToICal(); break;
                case "18": exportFilteredTasksToICal(); break;
                case "19": listOverloadedCollaborators(); break;
                case "20": addTagToTask(); break;
                case "0":  running = false; System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid option.");
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("--- Menu ---");
        System.out.println("1.  Create Task");
        System.out.println("2.  View All Tasks");
        System.out.println("3.  Update Task");
        System.out.println("4.  Mark Task Complete");
        System.out.println("5.  Cancel Task");
        System.out.println("6.  Create Project");
        System.out.println("7.  Assign Task to Project");
        System.out.println("8.  Add Subtask");
        System.out.println("9.  Search Tasks");
        System.out.println("10. Export all tasks to CSV");
        System.out.println("11. Import tasks from CSV");
        System.out.println("12. Set recurring pattern on task");
        System.out.println("13. Add collaborator to project");
        System.out.println("14. Assign task to collaborator");
        System.out.println("15. Export search results to CSV");
        System.out.println("16. Export Single Task to iCal");
        System.out.println("17. Export Project to iCal");
        System.out.println("18. Export Filtered Tasks to iCal");
        System.out.println("19. List Overloaded Collaborators");
        System.out.println("20. Add Tag to Task");
        System.out.println("0.  Exit");
        System.out.print("Choose: ");
    }

    private static void createTask() {
        System.out.print("Task name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Due date (YYYY-MM-DD, Enter to skip): ");
        LocalDate dueDate = parseDate(scanner.nextLine().trim());
        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        Priority priority = parsePriority(scanner.nextLine().trim());
        System.out.print("Tags (comma-separated, Enter to skip): ");
        String tagsInput = scanner.nextLine().trim();
        Task task = taskService.createTask(name, desc, dueDate, priority);
        if (!tagsInput.isEmpty()) {
            for (String tagName : tagsInput.split(",")) {
                if (!tagName.isBlank()) task.addTag(new Tag(tagName.trim()));
            }
        }
        System.out.println("Task created: " + task.getTitle());
    }

    private static void viewAllTasks() {
        List<Task> tasks = taskService.viewTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks in the system.");
            return;
        }
        System.out.printf("%-20s %-12s %-10s %-12s %-15s %-20s%n", "Name", "Status", "Priority", "Due Date", "Project", "Tags");
        System.out.println("-".repeat(90));
        for (Task task : tasks) {
            String proj = task.getProject() != null ? task.getProject().getName() : "-";
            String tags = task.getTags().isEmpty() ? "-" :
                    task.getTags().stream().map(Tag::getName).reduce((a, b) -> a + ", " + b).orElse("-");
            System.out.printf("%-20s %-12s %-10s %-12s %-15s %-20s%n",
                    task.getTitle(), task.getStatus(), task.getPriority(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "-", proj, tags);
        }
    }

    private static void updateTask() {
        System.out.print("Task name to update: ");
        String name = scanner.nextLine().trim();
        System.out.print("New description (Enter to skip): ");
        String desc = scanner.nextLine().trim();
        System.out.print("New due date YYYY-MM-DD (Enter to skip): ");
        String dateStr = scanner.nextLine().trim();
        System.out.print("New priority LOW/MEDIUM/HIGH (Enter to skip): ");
        String priStr = scanner.nextLine().trim();

        try {
            taskService.updateTask(name,
                    desc.isEmpty() ? null : desc,
                    dateStr.isEmpty() ? null : parseDate(dateStr),
                    priStr.isEmpty() ? null : parsePriority(priStr));
            System.out.println("Task updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void markTaskComplete() {
        System.out.print("Task name: ");
        String name = scanner.nextLine().trim();
        try {
            taskService.markComplete(name);
            System.out.println("Task marked as completed.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void cancelTask() {
        System.out.print("Task name: ");
        String name = scanner.nextLine().trim();
        try {
            taskService.cancelTask(name);
            System.out.println("Task cancelled.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void createProject() {
        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        try {
            Project project = taskService.createProject(name, desc);
            System.out.println("Project created: " + project.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void assignTaskToProject() {
        System.out.print("Task name: ");
        String taskName = scanner.nextLine().trim();
        System.out.print("Project name: ");
        String projectName = scanner.nextLine().trim();
        try {
            taskService.assignTaskToProject(taskName, projectName);
            System.out.println("Task assigned to project.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addSubtask() {
        System.out.print("Parent task name: ");
        String parent = scanner.nextLine().trim();
        System.out.print("Subtask title: ");
        String subtask = scanner.nextLine().trim();
        try {
            taskService.addSubtask(parent, subtask);
            System.out.println("Subtask added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static SearchCriteria promptSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();

        System.out.print("Name match (Enter to skip): ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) criteria.setNameMatch(name);

        System.out.print("Status OPEN/COMPLETED/CANCELLED (Enter to skip): ");
        String statusStr = scanner.nextLine().trim();
        if (!statusStr.isEmpty()) {
            try { criteria.setTaskStatus(Status.valueOf(statusStr.toUpperCase())); }
            catch (IllegalArgumentException e) { System.out.println("Invalid status, skipping."); }
        }

        System.out.print("Priority LOW/MEDIUM/HIGH (Enter to skip): ");
        String priStr = scanner.nextLine().trim();
        if (!priStr.isEmpty()) {
            try { criteria.setPriority(Priority.valueOf(priStr.toUpperCase())); }
            catch (IllegalArgumentException e) { System.out.println("Invalid priority, skipping."); }
        }

        System.out.print("Period start YYYY-MM-DD (Enter to skip): ");
        String startStr = scanner.nextLine().trim();
        if (!startStr.isEmpty()) criteria.setPeriodStart(parseDate(startStr));

        System.out.print("Period end YYYY-MM-DD (Enter to skip): ");
        String endStr = scanner.nextLine().trim();
        if (!endStr.isEmpty()) criteria.setPeriodEnd(parseDate(endStr));

        System.out.print("Day of week MON..SUN (Enter to skip): ");
        String dowStr = scanner.nextLine().trim();
        if (!dowStr.isEmpty()) {
            try {
                criteria.setDayOfWeek(DayOfWeek.valueOf(dowStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid day of week, skipping.");
            }
        }

        System.out.print("Tag (Enter to skip): ");
        String tagStr = scanner.nextLine().trim();
        if (!tagStr.isEmpty()) criteria.setTagMatch(tagStr);

        return criteria;
    }

    private static void searchTasks() {
        SearchCriteria criteria = promptSearchCriteria();
        SearchResult result = taskService.searchTasks(criteria);

        if (result.isEmpty()) {
            System.out.println("No matching tasks found.");
        } else {
            System.out.println("\nFound " + result.size() + " task(s):");
            System.out.printf("%-20s %-12s %-10s %-12s %-15s %-20s%n", "Name", "Status", "Priority", "Due Date", "Project", "Tags");
            System.out.println("-".repeat(90));
            for (Task task : result.getTasks()) {
                String proj = task.getProject() != null ? task.getProject().getName() : "-";
                String tags = task.getTags().isEmpty() ? "-" :
                        task.getTags().stream().map(Tag::getName).reduce((a, b) -> a + ", " + b).orElse("-");
                System.out.printf("%-20s %-12s %-10s %-12s %-15s %-20s%n",
                        task.getTitle(), task.getStatus(), task.getPriority(),
                        task.getDueDate() != null ? task.getDueDate().toString() : "-", proj, tags);
            }
        }
    }

    private static void exportAllTasksCsv() {
        System.out.print("Output file path: ");
        String path = scanner.nextLine().trim();
        try {
            csvService.exportTasks(path);
            System.out.println("Exported tasks to " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static void importTasksCsv() {
        System.out.print("Input file path: ");
        String path = scanner.nextLine().trim();
        try {
            csvService.importTasks(path);
            System.out.println("Import completed.");
        } catch (IOException e) {
            System.out.println("Import failed: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    private static void exportSearchResultsCsv() {
        SearchCriteria criteria = promptSearchCriteria();
        SearchResult result = taskService.searchTasks(criteria);
        if (result.isEmpty()) {
            System.out.println("No tasks match; nothing exported.");
            return;
        }
        System.out.print("Output file path: ");
        String path = scanner.nextLine().trim();
        try {
            csvService.exportSearchResult(result, path);
            System.out.println("Exported " + result.size() + " task(s) (with sub-occurrence rows) to " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static void setRecurringTask() {
        System.out.print("Task name: ");
        String taskName = scanner.nextLine().trim();
        System.out.println("Pattern: 1=Daily, 2=Weekly, 3=Monthly, 0=Clear recurrence");
        System.out.print("Choice: ");
        String type = scanner.nextLine().trim();
        try {
            if ("0".equals(type)) {
                taskService.setRecurringTask(taskName, null);
                System.out.println("Recurrence cleared.");
                return;
            }
            System.out.print("Interval (e.g. every N days/weeks/months): ");
            int interval = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Start date YYYY-MM-DD: ");
            LocalDate start = requireDate(scanner.nextLine().trim());
            System.out.print("End date YYYY-MM-DD: ");
            LocalDate end = requireDate(scanner.nextLine().trim());
            RecurrenceStrategy strategy;
            switch (type) {
                case "1":
                    strategy = new DailyRecurrence(interval, start, end);
                    break;
                case "2":
                    System.out.print("Weekdays (comma-separated MON,TUE,...): ");
                    List<DayOfWeek> days = parseDayOfWeekList(scanner.nextLine());
                    strategy = new WeeklyRecurrence(interval, days, start, end);
                    break;
                case "3":
                    System.out.print("Day of month (1-31): ");
                    int dom = Integer.parseInt(scanner.nextLine().trim());
                    strategy = new MonthlyRecurrence(interval, dom, start, end);
                    break;
                default:
                    System.out.println("Invalid pattern type.");
                    return;
            }
            taskService.setRecurringTask(taskName, strategy);
            Task t = taskService.findTaskByName(taskName);
            int n = t != null ? t.getOccurrences().size() : 0;
            System.out.println("Recurrence applied. Generated " + n + " occurrence(s).");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static List<DayOfWeek> parseDayOfWeekList(String line) {
        List<DayOfWeek> out = new ArrayList<>();
        if (line == null || line.isBlank()) return out;
        for (String part : line.split(",")) {
            String p = part.trim().toUpperCase();
            if (p.isEmpty()) continue;
            out.add(DayOfWeek.valueOf(p));
        }
        return out;
    }

    private static LocalDate requireDate(String str) {
        LocalDate d = parseDate(str);
        if (d == null) throw new IllegalArgumentException("Invalid date: " + str);
        return d;
    }

    private static void addCollaborator() {
        System.out.print("Project name: ");
        String projectName = scanner.nextLine().trim();
        System.out.print("Collaborator name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Category JUNIOR/INTERMEDIATE/SENIOR: ");
        String catStr = scanner.nextLine().trim();
        try {
            CollaboratorCategory cat = CollaboratorCategory.valueOf(catStr.toUpperCase());
            taskService.addCollaborator(projectName, name, cat);
            System.out.println("Collaborator added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void assignTaskToCollaboratorMenu() {
        System.out.print("Task name: ");
        String taskName = scanner.nextLine().trim();
        System.out.print("Project name (where collaborator is defined): ");
        String projectName = scanner.nextLine().trim();
        System.out.print("Collaborator name: ");
        String collabName = scanner.nextLine().trim();
        try {
            taskService.assignTaskToCollaborator(taskName, projectName, collabName);
            System.out.println("Task assigned; collaborator subtask created.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // iCal export handlers (options 16, 17, 18)

    private static void exportSingleTaskToICal() {
        List<Task> tasks = taskService.viewTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks available to export.");
            return;
        }
        System.out.println("Available tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String due = t.getDueDate() != null ? t.getDueDate().toString() : "no due date - not eligible";
            System.out.printf("  %d. %-20s [%s] Due: %s%n", i + 1, t.getTitle(), t.getStatus(), due);
        }
        System.out.print("Enter task number: ");
        String input = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= tasks.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            String taskName = tasks.get(index).getTitle();
            String filePath = buildExportPath(taskName);
            List<Task> exported = iCalGateway.exportSingleTask(taskName, filePath);
            printExportSummary(exported, filePath);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void exportProjectToICal() {
        List<taskmanager.model.project.Project> projects = taskService.getProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects available to export.");
            return;
        }
        System.out.println("Available projects:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, projects.get(i).getName());
        }
        System.out.print("Enter project number: ");
        String input = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= projects.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            String projectName = projects.get(index).getName();
            String filePath = buildExportPath("project_" + projectName);
            List<Task> exported = iCalGateway.exportProject(projectName, filePath);
            printExportSummary(exported, filePath);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void exportFilteredTasksToICal() {
        System.out.println("Set filters (only tasks with a due date will be exported):");
        SearchCriteria criteria = new SearchCriteria();

        System.out.print("Status OPEN/COMPLETED/CANCELLED (Enter to skip): ");
        String statusStr = scanner.nextLine().trim();
        if (!statusStr.isEmpty()) {
            try { criteria.setTaskStatus(Status.valueOf(statusStr.toUpperCase())); }
            catch (IllegalArgumentException e) { System.out.println("Invalid status, skipping."); }
        }

        System.out.print("Period start YYYY-MM-DD (Enter to skip): ");
        String startStr = scanner.nextLine().trim();
        if (!startStr.isEmpty()) criteria.setPeriodStart(parseDate(startStr));

        System.out.print("Period end YYYY-MM-DD (Enter to skip): ");
        String endStr = scanner.nextLine().trim();
        if (!endStr.isEmpty()) criteria.setPeriodEnd(parseDate(endStr));

        try {
            String filePath = buildExportPath("filtered_tasks");
            List<Task> exported = iCalGateway.exportFiltered(criteria, filePath);
            printExportSummary(exported, filePath);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Builds a safe file path inside the exports/ folder.
     */
    private static String buildExportPath(String name) throws IOException {
        java.io.File exportsDir = new java.io.File("exports");
        if (!exportsDir.exists()) exportsDir.mkdirs();
        String safeName = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return "exports/" + safeName + ".ics";
    }

    /** Prints a table of the tasks that were written to the .ics file. */
    private static void printExportSummary(List<Task> exported, String filePath) {
        if (exported.isEmpty()) {
            System.out.println("No eligible tasks to export (tasks must have a due date).");
            return;
        }
        System.out.println("\nExported " + exported.size() + " task(s) to: " + filePath);
        System.out.printf("%-22s %-12s %-10s %-12s %-15s%n",
                "Task", "Status", "Priority", "Due Date", "Project");
        System.out.println("-".repeat(74));
        for (Task t : exported) {
            String proj = t.getProject() != null ? t.getProject().getName() : "-";
            System.out.printf("%-22s %-12s %-10s %-12s %-15s%n",
                    t.getTitle(), t.getStatus(), t.getPriority(),
                    t.getDueDate().toString(), proj);
        }
    }


    // Tag management (option 20)

    private static void addTagToTask() {
        System.out.print("Task name: ");
        String taskName = scanner.nextLine().trim();
        System.out.print("Tag name(s) (comma-separated): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("No tags entered.");
            return;
        }
        try {
            for (String tagName : input.split(",")) {
                if (!tagName.isBlank()) taskService.addTag(taskName, tagName.trim());
            }
            System.out.println("Tag(s) added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Overload listing handler (option 19)

    private static void listOverloadedCollaborators() {
        List<Collaborator> overloaded = taskService.getOverloadedCollaborators();
        if (overloaded.isEmpty()) {
            System.out.println("No overloaded collaborators.");
            return;
        }
        System.out.println("Overloaded collaborators:");
        System.out.printf("%-20s %-15s %-10s %-10s%n", "Name", "Category", "Open", "Limit");
        System.out.println("-".repeat(58));
        for (Collaborator c : overloaded) {
            System.out.printf("%-20s %-15s %-10d %-10d%n",
                    c.getName(),
                    c.getCategory(),
                    c.getOpenTaskCount(),
                    c.getCategory().getOpenTaskLimit());
        }
    }

    private static LocalDate parseDate(String str) {
        try { return LocalDate.parse(str); }
        catch (DateTimeParseException e) { return null; }
    }

    private static Priority parsePriority(String str) {
        try { return Priority.valueOf(str.toUpperCase()); }
        catch (IllegalArgumentException e) { return Priority.MEDIUM; }
    }
}
