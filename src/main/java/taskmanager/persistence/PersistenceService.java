package taskmanager.persistence;

import taskmanager.model.core.ActivityEntry;
import taskmanager.model.core.Subtask;
import taskmanager.model.core.Tag;
import taskmanager.model.core.Task;
import taskmanager.model.enums.CollaboratorCategory;
import taskmanager.model.enums.Priority;
import taskmanager.model.enums.Status;
import taskmanager.model.project.Collaborator;
import taskmanager.model.project.Project;
import taskmanager.model.project.TaskAssignment;
import taskmanager.service.TaskService;
import taskmanager.strategy.DailyRecurrence;
import taskmanager.strategy.MonthlyRecurrence;
import taskmanager.strategy.WeeklyRecurrence;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceService {
    private final DatabaseManager databaseManager;

    public PersistenceService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    //Saves everything into database
    public void saveAll(TaskService taskService) {
        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);

            clearTables(conn);

            Map<Project, Integer> projectIds = saveProjects(conn, taskService.getProjects());
            Map<Collaborator, Integer> collaboratorIds = saveCollaborators(conn, taskService.getProjects(), projectIds);
            Map<Task, Integer> taskIds = saveTasks(conn, taskService.viewTasks(), projectIds);
            Map<Subtask, Integer> subtaskIds = saveSubtasks(conn, taskService.viewTasks(), taskIds, collaboratorIds);
            saveAssignments(conn, taskService.viewTasks(), collaboratorIds, subtaskIds);
            saveActivityLogs(conn, taskService.viewTasks(), taskIds);
            saveTags(conn, taskService.viewTasks(), taskIds);

            conn.commit();
            System.out.println("Data saved successfully.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to save data", e);
        }
    }

    //loads everything from database
    public void loadAll(TaskService taskService) {
        try (Connection conn = databaseManager.getConnection()) {
            Map<Integer, Project> projectMap = loadProjects(conn, taskService);
            Map<Integer, Collaborator> collaboratorMap = loadCollaborators(conn, projectMap);
            Map<Integer, Task> taskMap = loadTasks(conn, taskService, projectMap);
            Map<Integer, Subtask> subtaskMap = loadSubtasks(conn, taskMap, collaboratorMap);
            loadAssignments(conn, collaboratorMap, subtaskMap);
            loadActivityLogs(conn, taskMap);
            loadTags(conn, taskMap);

            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load data", e);
        }
    }

    private void clearTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM task_assignments");
            stmt.executeUpdate("DELETE FROM activity_log");
            stmt.executeUpdate("DELETE FROM tags");
            stmt.executeUpdate("DELETE FROM subtasks");
            stmt.executeUpdate("DELETE FROM collaborators");
            stmt.executeUpdate("DELETE FROM tasks");
            stmt.executeUpdate("DELETE FROM projects");
        }
    }

    //Save Methods
    private Map<Project, Integer> saveProjects(Connection conn, List<Project> projects) throws SQLException {
        Map<Project, Integer> ids = new HashMap<>();

        String sql = "INSERT INTO projects(name, description) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Project project : projects) {
                ps.setString(1, project.getName());
                ps.setString(2, project.getDescription());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ids.put(project, rs.getInt(1));
                    }
                }
            }
        }

        return ids;
    }

    private Map<Collaborator, Integer> saveCollaborators(Connection conn,
                                                         List<Project> projects,
                                                         Map<Project, Integer> projectIds) throws SQLException {
        Map<Collaborator, Integer> ids = new HashMap<>();

        String sql = "INSERT INTO collaborators(name, category, project_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Project project : projects) {
                for (Collaborator collaborator : project.getCollaborators()) {
                    ps.setString(1, collaborator.getName());
                    ps.setString(2, collaborator.getCategory().name());
                    ps.setInt(3, projectIds.get(project));
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            ids.put(collaborator, rs.getInt(1));
                        }
                    }
                }
            }
        }

        return ids;
    }

    private Map<Task, Integer> saveTasks(Connection conn,
                                         List<Task> tasks,
                                         Map<Project, Integer> projectIds) throws SQLException {
        Map<Task, Integer> ids = new HashMap<>();

        String sql = "INSERT INTO tasks(title, description, creation_date, due_date, priority, status, project_id, " +
                "recurrence_type, recurrence_interval, recurrence_start_date, recurrence_end_date, recurrence_days, recurrence_day_of_month) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Task task : tasks) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getCreationDate() != null ? task.getCreationDate().toString() : null);
                ps.setString(4, task.getDueDate() != null ? task.getDueDate().toString() : null);
                ps.setString(5, task.getPriority() != null ? task.getPriority().name() : null);
                ps.setString(6, task.getStatus() != null ? task.getStatus().name() : null);

                if (task.getProject() != null) {
                    ps.setInt(7, projectIds.get(task.getProject()));
                } else {
                    ps.setNull(7, Types.INTEGER);
                }

                setRecurrenceParameters(ps, task);

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ids.put(task, rs.getInt(1));
                    }
                }
            }
        }

        return ids;
    }

    private Map<Subtask, Integer> saveSubtasks(Connection conn,
                                               List<Task> tasks,
                                               Map<Task, Integer> taskIds,
                                               Map<Collaborator, Integer> collaboratorIds) throws SQLException {
        Map<Subtask, Integer> subtaskIds = new HashMap<>();

        String sql = "INSERT INTO subtasks(title, status, parent_task_id, assigned_collaborator_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Task task : tasks) {
                Integer taskId = taskIds.get(task);

                for (Subtask subtask : task.getSubtasks()) {
                    ps.setString(1, subtask.getTitle());
                    ps.setString(2, subtask.getStatus() != null ? subtask.getStatus().name() : null);
                    ps.setInt(3, taskId);

                    if (subtask.getAssignedTo() != null &&
                            collaboratorIds.containsKey(subtask.getAssignedTo())) {
                        ps.setInt(4, collaboratorIds.get(subtask.getAssignedTo()));
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            subtaskIds.put(subtask, rs.getInt(1));
                        }
                    }
                }
            }
        }
        return subtaskIds;
    }

    private void saveAssignments(Connection conn,
                                 List<Task> tasks,
                                 Map<Collaborator, Integer> collaboratorIds,
                                 Map<Subtask, Integer> subtaskIds) throws SQLException {
        String sql = "INSERT INTO task_assignments(assigned_date, status, collaborator_id, subtask_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Task task : tasks) {
                for (TaskAssignment assignment : task.getAssignments()) {
                    ps.setString(1, assignment.getAssignedDate() != null
                            ? assignment.getAssignedDate().toString()
                            : null);

                    ps.setString(2, assignment.getStatus() != null
                            ? assignment.getStatus().name()
                            : null);

                    Collaborator collaborator = assignment.getCollaborator();
                    Subtask subtask = assignment.getSubtask();

                    if (collaborator != null && collaboratorIds.containsKey(collaborator)) {
                        ps.setInt(3, collaboratorIds.get(collaborator));
                    } else {
                        ps.setNull(3, Types.INTEGER);
                    }

                    if (subtask != null && subtaskIds.containsKey(subtask)) {
                        ps.setInt(4, subtaskIds.get(subtask));
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }

                    ps.executeUpdate();
                }
            }
        }
    }

    private void saveActivityLogs(Connection conn,
                                  List<Task> tasks,
                                  Map<Task, Integer> taskIds) throws SQLException {
        String sql = "INSERT INTO activity_log(task_id, timestamp, description) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Task task : tasks) {
                Integer taskId = taskIds.get(task);
                if (taskId == null) continue;

                for (ActivityEntry entry : task.getActivityLog()) {
                    ps.setInt(1, taskId);
                    ps.setString(2, entry.getTimestamp() != null ? entry.getTimestamp().toString() : null);
                    ps.setString(3, entry.getDescription());
                    ps.executeUpdate();
                }
            }
        }
    }

    //Load Methods
    private Map<Integer, Project> loadProjects(Connection conn, TaskService taskService) throws SQLException {
        Map<Integer, Project> projectMap = new HashMap<>();

        String sql = "SELECT id, name, description FROM projects";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");

                Project project = new Project(name, description);
                taskService.addLoadedProject(project);
                projectMap.put(id, project);
            }
        }

        return projectMap;
    }

    private Map<Integer, Collaborator> loadCollaborators(Connection conn,
                                                         Map<Integer, Project> projectMap) throws SQLException {
        Map<Integer, Collaborator> collaboratorMap = new HashMap<>();

        String sql = "SELECT id, name, category, project_id FROM collaborators";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String categoryStr = rs.getString("category");
                int projectId = rs.getInt("project_id");

                Project project = projectMap.get(projectId);
                if (project != null) {
                    Collaborator collaborator = new Collaborator(
                            name,
                            CollaboratorCategory.valueOf(categoryStr),
                            project
                    );

                    project.addCollaborator(collaborator);
                    collaboratorMap.put(id, collaborator);
                }
            }
        }

        return collaboratorMap;
    }

    private Map<Integer, Task> loadTasks(Connection conn,
                                         TaskService taskService,
                                         Map<Integer, Project> projectMap) throws SQLException {
        Map<Integer, Task> taskMap = new HashMap<>();

        String sql = "SELECT id, title, description, creation_date, due_date, priority, status, project_id, " +
                "recurrence_type, recurrence_interval, recurrence_start_date, recurrence_end_date, recurrence_days, recurrence_day_of_month " +
                "FROM tasks";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String creationDateStr = rs.getString("creation_date");
                String dueDateStr = rs.getString("due_date");
                String priorityStr = rs.getString("priority");
                String statusStr = rs.getString("status");
                String recurrenceType = rs.getString("recurrence_type");
                Integer recurrenceInterval = (Integer) rs.getObject("recurrence_interval");
                String recurrenceStartStr = rs.getString("recurrence_start_date");
                String recurrenceEndStr = rs.getString("recurrence_end_date");
                String recurrenceDaysStr = rs.getString("recurrence_days");
                Integer recurrenceDayOfMonth = (Integer) rs.getObject("recurrence_day_of_month");

                int projectId = rs.getInt("project_id");
                boolean hasProject = !rs.wasNull();

                Task task = new Task(
                        title,
                        description,
                        dueDateStr != null ? LocalDate.parse(dueDateStr) : null,
                        priorityStr != null ? Priority.valueOf(priorityStr) : null
                );

                if (creationDateStr != null) {
                    task.setCreationDate(LocalDate.parse(creationDateStr));
                }

                if (statusStr != null) {
                    task.setStatus(Status.valueOf(statusStr));
                }

                if (hasProject) {
                    Project project = projectMap.get(projectId);
                    task.setProject(project);
                }

                if (recurrenceType != null &&
                        recurrenceInterval != null &&
                        recurrenceStartStr != null &&
                        recurrenceEndStr != null) {

                    LocalDate recurrenceStart = LocalDate.parse(recurrenceStartStr);
                    LocalDate recurrenceEnd = LocalDate.parse(recurrenceEndStr);

                    if ("DAILY".equals(recurrenceType)) {
                        task.setRecurring(new DailyRecurrence(
                                recurrenceInterval,
                                recurrenceStart,
                                recurrenceEnd
                        ));

                    } else if ("WEEKLY".equals(recurrenceType)) {
                        List<DayOfWeek> weekdays = new ArrayList<>();

                        if (recurrenceDaysStr != null && !recurrenceDaysStr.isBlank()) {
                            for (String part : recurrenceDaysStr.split(",")) {
                                weekdays.add(DayOfWeek.valueOf(part.trim()));
                            }
                        }

                        task.setRecurring(new WeeklyRecurrence(
                                recurrenceInterval,
                                weekdays,
                                recurrenceStart,
                                recurrenceEnd
                        ));

                    } else if ("MONTHLY".equals(recurrenceType) && recurrenceDayOfMonth != null) {
                        task.setRecurring(new MonthlyRecurrence(
                                recurrenceInterval,
                                recurrenceDayOfMonth,
                                recurrenceStart,
                                recurrenceEnd
                        ));
                    }
                }


                taskService.addLoadedTask(task);
                taskMap.put(id, task);
            }
        }

        return taskMap;
    }

    private Map<Integer, Subtask> loadSubtasks(Connection conn,
                                               Map<Integer, Task> taskMap,
                                               Map<Integer, Collaborator> collaboratorMap) throws SQLException {
        Map<Integer, Subtask> subtaskMap = new HashMap<>();

        String sql = "SELECT id, title, status, parent_task_id, assigned_collaborator_id FROM subtasks";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String statusStr = rs.getString("status");
                int parentTaskId = rs.getInt("parent_task_id");
                int collaboratorId = rs.getInt("assigned_collaborator_id");
                boolean hasCollaborator = !rs.wasNull();

                Task parentTask = taskMap.get(parentTaskId);
                if (parentTask != null) {
                    Subtask subtask = new Subtask(title, parentTask);

                    if (statusStr != null) {
                        subtask.setStatus(Status.valueOf(statusStr));
                    }

                    if (hasCollaborator) {
                        Collaborator collaborator = collaboratorMap.get(collaboratorId);
                        subtask.setAssignedTo(collaborator);
                    }

                    parentTask.getSubtasks().add(subtask);
                    subtaskMap.put(id, subtask);
                }
            }
        }
        return subtaskMap;
    }

    private void loadAssignments(Connection conn,
                                 Map<Integer, Collaborator> collaboratorMap,
                                 Map<Integer, Subtask> subtaskMap) throws SQLException {
        String sql = "SELECT assigned_date, status, collaborator_id, subtask_id FROM task_assignments";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String assignedDateStr = rs.getString("assigned_date");
                String statusStr = rs.getString("status");
                int collaboratorId = rs.getInt("collaborator_id");
                int subtaskId = rs.getInt("subtask_id");

                Collaborator collaborator = collaboratorMap.get(collaboratorId);
                Subtask subtask = subtaskMap.get(subtaskId);

                if (collaborator != null && subtask != null) {
                    Task parentTask = subtask.getParentTask();

                    TaskAssignment assignment = new TaskAssignment(
                            assignedDateStr != null ? LocalDate.parse(assignedDateStr) : null,
                            statusStr != null ? Status.valueOf(statusStr) : null,
                            collaborator,
                            subtask
                    );

                    if (parentTask != null) {
                        parentTask.getAssignments().add(assignment);
                    }
                }
            }
        }
    }

    private void loadActivityLogs(Connection conn,
                                  Map<Integer, Task> taskMap) throws SQLException {
        String sql = "SELECT task_id, timestamp, description FROM activity_log";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int taskId = rs.getInt("task_id");
                String timestampStr = rs.getString("timestamp");
                String description = rs.getString("description");

                Task task = taskMap.get(taskId);

                if (task != null && timestampStr != null && description != null) {
                    ActivityEntry entry = new ActivityEntry(
                            java.time.LocalDateTime.parse(timestampStr),
                            description
                    );
                    task.getActivityLog().add(entry);
                }
            }
        }
    }

    private void saveTags(Connection conn,
                          List<Task> tasks,
                          Map<Task, Integer> taskIds) throws SQLException {
        String sql = "INSERT INTO tags(task_id, name) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Task task : tasks) {
                Integer taskId = taskIds.get(task);
                if (taskId == null) continue;

                for (Tag tag : task.getTags()) {
                    ps.setInt(1, taskId);
                    ps.setString(2, tag.getName());
                    ps.executeUpdate();
                }
            }
        }
    }

    private void loadTags(Connection conn,
                          Map<Integer, Task> taskMap) throws SQLException {
        String sql = "SELECT task_id, name FROM tags";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int taskId = rs.getInt("task_id");
                String name = rs.getString("name");

                Task task = taskMap.get(taskId);
                if (task != null) {
                    task.addTag(new Tag(name));
                }
            }
        }
    }

    //Recurrence Helper
    private void setRecurrenceParameters(PreparedStatement ps, Task task) throws SQLException {
        if (task.getRecurrenceStrategy() == null) {
            ps.setString(8, null);
            ps.setNull(9, Types.INTEGER);
            ps.setString(10, null);
            ps.setString(11, null);
            ps.setString(12, null);
            ps.setNull(13, Types.INTEGER);
            return;
        }

        if (task.getRecurrenceStrategy() instanceof DailyRecurrence) {
            DailyRecurrence daily = (DailyRecurrence) task.getRecurrenceStrategy();

            ps.setString(8, "DAILY");
            ps.setInt(9, daily.getInterval());
            ps.setString(10, daily.getStartDate() != null ? daily.getStartDate().toString() : null);
            ps.setString(11, daily.getEndDate() != null ? daily.getEndDate().toString() : null);
            ps.setString(12, null);
            ps.setNull(13, Types.INTEGER);

        } else if (task.getRecurrenceStrategy() instanceof WeeklyRecurrence) {
            WeeklyRecurrence weekly = (WeeklyRecurrence) task.getRecurrenceStrategy();

            ps.setString(8, "WEEKLY");
            ps.setInt(9, weekly.getInterval());
            ps.setString(10, weekly.getStartDate() != null ? weekly.getStartDate().toString() : null);
            ps.setString(11, weekly.getEndDate() != null ? weekly.getEndDate().toString() : null);

            String weekdays = String.join(",",
                    weekly.getWeekdays().stream().map(Enum::name).toArray(String[]::new));
            ps.setString(12, weekdays);

            ps.setNull(13, Types.INTEGER);

        } else if (task.getRecurrenceStrategy() instanceof MonthlyRecurrence) {
            MonthlyRecurrence monthly = (MonthlyRecurrence) task.getRecurrenceStrategy();

            ps.setString(8, "MONTHLY");
            ps.setInt(9, monthly.getInterval());
            ps.setString(10, monthly.getStartDate() != null ? monthly.getStartDate().toString() : null);
            ps.setString(11, monthly.getEndDate() != null ? monthly.getEndDate().toString() : null);
            ps.setString(12, null);
            ps.setInt(13, monthly.getDayOfMonth());

        } else {
            ps.setString(8, null);
            ps.setNull(9, Types.INTEGER);
            ps.setString(10, null);
            ps.setString(11, null);
            ps.setString(12, null);
            ps.setNull(13, Types.INTEGER);
        }
    }
}