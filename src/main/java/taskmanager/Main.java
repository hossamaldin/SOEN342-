package taskmanager;

import taskmanager.model.core.Task;
import taskmanager.model.core.User;
import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import taskmanager.model.project.Project;
import taskmanager.search.SearchCriteria;
import taskmanager.search.SearchResult;
import taskmanager.service.TaskService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static TaskService taskService;
    private static Scanner scanner;

    public static void main(String[] args) {
        User user = new User("Default User");
        taskService = new TaskService(user);
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
                case "9": searchTasks(); break;
                case "0": running = false; System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid option.");
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("--- Menu ---");
        System.out.println("1. Create Task");
        System.out.println("2. View All Tasks");
        System.out.println("3. Update Task");
        System.out.println("4. Mark Task Complete");
        System.out.println("5. Cancel Task");
        System.out.println("6. Create Project");
        System.out.println("7. Assign Task to Project");
        System.out.println("8. Add Subtask");
        System.out.println("9. Search Tasks");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private static void createTask() {
        System.out.print("Task name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Due date (YYYY-MM-DD): ");
        LocalDate dueDate = parseDate(scanner.nextLine().trim());
        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        Priority priority = parsePriority(scanner.nextLine().trim());
        Task task = taskService.createTask(name, desc, dueDate, priority);
        System.out.println("Task created: " + task.getTitle());
    }

    private static void viewAllTasks() {
        List<Task> tasks = taskService.viewTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks in the system.");
            return;
        }
        System.out.printf("%-20s %-12s %-10s %-12s %-15s%n", "Name", "Status", "Priority", "Due Date", "Project");
        System.out.println("-".repeat(70));
        for (Task task : tasks) {
            String proj = task.getProject() != null ? task.getProject().getName() : "-";
            System.out.printf("%-20s %-12s %-10s %-12s %-15s%n",
                    task.getTitle(), task.getStatus(), task.getPriority(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "-", proj);
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

    private static void searchTasks() {
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

        SearchResult result = taskService.searchTasks(criteria);

        if (result.isEmpty()) {
            System.out.println("No matching tasks found.");
        } else {
            System.out.println("\nFound " + result.size() + " task(s):");
            System.out.printf("%-20s %-12s %-10s %-12s %-15s%n", "Name", "Status", "Priority", "Due Date", "Project");
            System.out.println("-".repeat(70));
            for (Task task : result.getTasks()) {
                String proj = task.getProject() != null ? task.getProject().getName() : "-";
                System.out.printf("%-20s %-12s %-10s %-12s %-15s%n",
                        task.getTitle(), task.getStatus(), task.getPriority(),
                        task.getDueDate() != null ? task.getDueDate().toString() : "-", proj);
            }
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