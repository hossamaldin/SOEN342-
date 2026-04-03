package taskmanager.model.project;

import taskmanager.model.core.Subtask;
import taskmanager.model.enums.Status;

import java.time.LocalDate;

public class TaskAssignment {
    private LocalDate assignedDate;
    private Status status;
    private Collaborator collaborator;
    private Subtask subtask;

    public TaskAssignment(Collaborator collaborator, Subtask subtask) {
        this.assignedDate = LocalDate.now();
        this.status = Status.OPEN;
        this.collaborator = collaborator;
        this.subtask = subtask;
    }

    public TaskAssignment(LocalDate assignedDate, Status status, Collaborator collaborator, Subtask subtask) {
        this.assignedDate = assignedDate;
        this.status = status;
        this.collaborator = collaborator;
        this.subtask = subtask;
    }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) {this.assignedDate = assignedDate;}
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Collaborator getCollaborator() { return collaborator; }
    public Subtask getSubtask() { return subtask; }
}