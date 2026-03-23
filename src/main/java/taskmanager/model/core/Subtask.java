package taskmanager.model.core;

import taskmanager.model.project.Collaborator;

public class Subtask extends WorkItem {
    private Task parentTask;
    private Collaborator assignedTo;

    public Subtask(String title, Task parentTask) {
        super(title);
        this.parentTask = parentTask;
    }

    public Task getParentTask() { return parentTask; }
    public void setParentTask(Task parentTask) { this.parentTask = parentTask; }
    public Collaborator getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Collaborator assignedTo) { this.assignedTo = assignedTo; }
}