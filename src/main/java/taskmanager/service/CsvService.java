package taskmanager.service;

import java.io.IOException;
import taskmanager.model.core.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvService {
    private final TaskService taskService;

    public CsvService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void exportTasks(String filePath) throws IOException {
        List<Task> tasks = taskService.getTasks();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory");
            writer.newLine();

            for (Task task : tasks) {
                writer.write(task.toCsv());
                writer.newLine();
            }
        }
    }

    public void importTasks(String filePath) throws IOException {
        // TODO:  implements UC-16
    }
}