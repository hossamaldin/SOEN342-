package taskmanager.search;

import taskmanager.model.core.Task;

import java.time.LocalDateTime;
import java.util.List;

public class SearchResult {
    private List<Task> tasks;
    private LocalDateTime exportedAt;

    public SearchResult(List<Task> tasks) { this.tasks = tasks; }

    public List<Task> getTasks() { return tasks; }
    public LocalDateTime getExportedAt() { return exportedAt; }
    public void setExportedAt(LocalDateTime exportedAt) { this.exportedAt = exportedAt; }
    public boolean isEmpty() { return tasks == null || tasks.isEmpty(); }
    public int size() { return tasks != null ? tasks.size() : 0; }
}