package taskmanager.service;

import java.io.IOException;

public class CsvService {
    private final TaskService taskService;

    public CsvService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void exportTasks(String filePath) throws IOException {
        // TODO:  implements UC-17
    }

    public void importTasks(String filePath) throws IOException {
        // TODO:  implements UC-16
    }
}