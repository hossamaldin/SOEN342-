package taskmanager.service;

import taskmanager.model.core.Subtask;
import taskmanager.model.core.Task;
import taskmanager.model.core.User;
import taskmanager.model.enums.CollaboratorCategory;
import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import taskmanager.model.interfaces.RecurrenceStrategy;
import taskmanager.model.project.Collaborator;
import taskmanager.model.project.Project;
import taskmanager.search.SearchCriteria;
import taskmanager.search.SearchResult;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    private User currentUser;
    private List<Task> tasks;
    private List<Project> projects;

    public TaskService(User user) {
        this.currentUser = user;
        this.tasks = new ArrayList<>();
        this.projects = new ArrayList<>();
    }

    //  Create Task
    // OCL: "The number of open tasks without a due date should not exceed 50."
    public Task createTask(String title, String description, LocalDate dueDate, Priority priority) {
        if (dueDate == null) {
            long openWithoutDueDate = tasks.stream()
                    .filter(t -> t.isOpen() && t.getDueDate() == null)
                    .count();
            if (openWithoutDueDate >= 50)
                throw new IllegalArgumentException(
                        "OCL violation: Cannot create more than 50 open tasks without a due date.");
        }
        Task task = new Task(title, description, dueDate, priority);
        tasks.add(task);
        task.recordActivity("Task created");
        return task;
    }

    //  Update Task
    public void updateTask(String taskName, String newDescription, LocalDate newDueDate, Priority newPriority) {
        Task task = findTaskByName(taskName);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskName);
        if (newDescription != null) task.setDescription(newDescription);
        if (newDueDate != null) task.setDueDate(newDueDate);
        if (newPriority != null) task.setPriority(newPriority);
        task.recordActivity("Task updated");
    }

    //  Mark Complete
    public void markComplete(String taskName) {
        Task task = findTaskByName(taskName);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskName);
        task.setStatus(Status.COMPLETED);
        task.recordActivity("Task marked as completed");
    }

    //  Cancel Task
    public void cancelTask(String taskName) {
        Task task = findTaskByName(taskName);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskName);
        task.setStatus(Status.CANCELLED);
        task.recordActivity("Task cancelled");
    }

    //  View Tasks
    public List<Task> viewTasks() {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    // Create Project
    public Project createProject(String name, String description) {
        Project existing = findProjectByName(name);
        if (existing != null) throw new IllegalArgumentException("Project already exists: " + name);
        Project project = new Project(name, description);
        projects.add(project);
        return project;
    }

    //  Assign Task to Project
    public void assignTaskToProject(String taskName, String projectName) {
        Task task = findTaskByName(taskName);
        Project project = findProjectByName(projectName);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskName);
        if (project == null) throw new IllegalArgumentException("Project not found: " + projectName);
        task.setProject(project);
        task.recordActivity("Task assigned to project: " + projectName);
    }

    //  Add Subtask
    public void addSubtask(String parentTaskName, String subtaskTitle) {
        Task parent = findTaskByName(parentTaskName);
        if (parent == null) throw new IllegalArgumentException("Task not found: " + parentTaskName);
        if (parent.getSubtasks().size() >= 20)
            throw new IllegalArgumentException(
                    "OCL violation: A task cannot have more than 20 sub-tasks.");
        Subtask subtask = new Subtask(subtaskTitle, parent);
        parent.addSubtask(subtask);
        parent.recordActivity("Subtask added: " + subtaskTitle);
    }

    public void setRecurringTask(String taskName, RecurrenceStrategy strategy) {
        Task task = findTaskByName(taskName);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskName);
        task.setRecurring(strategy);
        task.recordActivity(strategy == null ? "Recurrence cleared" : "Recurrence pattern set");
    }

    public Collaborator addCollaborator(String projectName, String collaboratorName, CollaboratorCategory category) {
        Project project = findProjectByName(projectName);
        if (project == null) throw new IllegalArgumentException("Project not found: " + projectName);
        Collaborator existing = project.findCollaborator(collaboratorName);
        if (existing != null) {
            throw new IllegalArgumentException("Collaborator already exists in project: " + collaboratorName);
        }
        Collaborator c = new Collaborator(collaboratorName, category, project);
        project.addCollaborator(c);
        return c;
    }

    public void assignTaskToCollaborator(String taskName, String projectName, String collaboratorName) {
        Task task = findTaskByName(taskName);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskName);
        Project project = findProjectByName(projectName);
        if (project == null) throw new IllegalArgumentException("Project not found: " + projectName);
        Collaborator collaborator = project.findCollaborator(collaboratorName);
        if (collaborator == null) {
            throw new IllegalArgumentException("Collaborator not found in project: " + collaboratorName);
        }
        if (task.getProject() != null && !task.getProject().getName().equalsIgnoreCase(project.getName())) {
            throw new IllegalArgumentException("Task is assigned to a different project");
        }
        if (task.getProject() == null) {
            task.setProject(project);
        }
        String expectedTitle = task.getTitle() + " (" + collaborator.getName() + ")";
        boolean alreadyLinked = task.getSubtasks().stream()
                .anyMatch(s -> collaborator.equals(s.getAssignedTo())
                        && expectedTitle.equalsIgnoreCase(s.getTitle()));
        if (alreadyLinked) {
            return;
        }
        task.assignToCollaborator(collaborator);
    }

    //  Search Tasks
    public SearchResult searchTasks(SearchCriteria criteria) {
        List<Task> result;

        if (criteria == null || criteria.isEmpty()) {
            result = tasks.stream()
                    .filter(t -> t.getStatus() == Status.OPEN)
                    .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } else {
            result = tasks.stream()
                    .filter(t -> matchesCriteria(t, criteria))
                    .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }

        return new SearchResult(result);
    }

    private boolean matchesCriteria(Task task, SearchCriteria criteria) {
        if (criteria.getNameMatch() != null && !criteria.getNameMatch().isEmpty()) {
            if (!task.getTitle().toLowerCase().contains(criteria.getNameMatch().toLowerCase()))
                return false;
        }
        if (criteria.getTaskStatus() != null) {
            if (task.getStatus() != criteria.getTaskStatus()) return false;
        }
        if (criteria.getPriority() != null) {
            if (task.getPriority() != criteria.getPriority()) return false;
        }
        if (criteria.getPeriodStart() != null && task.getDueDate() != null) {
            if (task.getDueDate().isBefore(criteria.getPeriodStart())) return false;
        }
        if (criteria.getPeriodEnd() != null && task.getDueDate() != null) {
            if (task.getDueDate().isAfter(criteria.getPeriodEnd())) return false;
        }
        if (criteria.getDayOfWeek() != null && task.getDueDate() != null) {
            if (task.getDueDate().getDayOfWeek() != criteria.getDayOfWeek()) return false;
        }
        return true;
    }

    // Helper methods
    public Task findTaskByName(String name) {
        return tasks.stream()
                .filter(t -> t.getTitle().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Task findTaskByTitleAndDueDate(String title, LocalDate dueDate) {
        return tasks.stream()
                .filter(t -> t.getTitle().equalsIgnoreCase(title))
                .filter(t -> Objects.equals(t.getDueDate(), dueDate))
                .findFirst()
                .orElse(null);
    }

    public Project findProjectByName(String name) {
        return projects.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Project findOrCreateProject(String name, String description) {
        Project project = findProjectByName(name);
        if (project == null) {
            project = new Project(name, description != null ? description : "");
            projects.add(project);
        }
        return project;
    }

    public Collaborator findOrCreateCollaborator(Project project, String name, CollaboratorCategory category) {
        Collaborator c = project.findCollaborator(name);
        if (c == null) {
            c = new Collaborator(name, category, project);
            project.addCollaborator(c);
        }
        return c;
    }

    // Collaborator management

    // Returns every collaborator across all projects.
    public List<Collaborator> getAllCollaborators() {
        return projects.stream()
                .flatMap(p -> p.getCollaborators().stream())
                .collect(Collectors.toList());
    }

    public List<Collaborator> getOverloadedCollaborators() {
        return getAllCollaborators().stream()
                .filter(c -> c.getOpenTaskCount() > c.getCategory().getOpenTaskLimit())
                .collect(Collectors.toList());
    }

    // Accessors
    public void addTask(Task task) { tasks.add(task); }
    public List<Task> getTasks() { return tasks; }
    public List<Project> getProjects() { return projects; }
    public User getCurrentUser() { return currentUser; }
}
