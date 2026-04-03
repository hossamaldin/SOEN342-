package taskmanager.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:taskmanager.db";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            createProjectsTable(stmt);
            createTasksTable(stmt);
            createCollaboratorsTable(stmt);
            createSubtasksTable(stmt);
            createTaskAssignmentsTable(stmt);
            createActivityLogTable(stmt);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void createProjectsTable(Statement stmt) throws SQLException {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS projects (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "description TEXT" +
                        ")"
        );
    }

    private void createTasksTable(Statement stmt) throws SQLException {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "creation_date TEXT, " +
                        "due_date TEXT, " +
                        "priority TEXT, " +
                        "status TEXT, " +
                        "project_id INTEGER, " +
                        "recurrence_type TEXT, " +
                        "recurrence_interval INTEGER, " +
                        "recurrence_start_date TEXT, " +
                        "recurrence_end_date TEXT, " +
                        "recurrence_days TEXT, " +
                        "recurrence_day_of_month INTEGER, " +
                        "FOREIGN KEY (project_id) REFERENCES projects(id)" +
                        ")"
        );
    }

    private void createCollaboratorsTable(Statement stmt) throws SQLException {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS collaborators (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "project_id INTEGER NOT NULL, " +
                        "UNIQUE(name, project_id), " +
                        "FOREIGN KEY (project_id) REFERENCES projects(id)" +
                        ")"
        );
    }

    private void createSubtasksTable(Statement stmt) throws SQLException {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS subtasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "status TEXT, " +
                        "parent_task_id INTEGER NOT NULL, " +
                        "assigned_collaborator_id INTEGER, " +
                        "FOREIGN KEY (parent_task_id) REFERENCES tasks(id), " +
                        "FOREIGN KEY (assigned_collaborator_id) REFERENCES collaborators(id)" +
                        ")"
        );
    }

    private void createTaskAssignmentsTable(Statement stmt) throws SQLException {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS task_assignments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "assigned_date TEXT, " +
                        "status TEXT, " +
                        "collaborator_id INTEGER NOT NULL, " +
                        "subtask_id INTEGER NOT NULL, " +
                        "FOREIGN KEY (collaborator_id) REFERENCES collaborators(id), " +
                        "FOREIGN KEY (subtask_id) REFERENCES subtasks(id)" +
                        ")"
        );
    }

    private void createActivityLogTable(Statement stmt) throws SQLException {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS activity_log (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "task_id INTEGER NOT NULL, " +
                        "timestamp TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "FOREIGN KEY (task_id) REFERENCES tasks(id)" +
                        ")"
        );
    }
}