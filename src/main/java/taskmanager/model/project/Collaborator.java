package taskmanager.model.project;

import taskmanager.model.core.Subtask;
import taskmanager.model.enums.CollaboratorCategory;
import taskmanager.model.enums.Status;
import taskmanager.model.interfaces.TaskActor;

import java.util.ArrayList;
import java.util.List;

public class Collaborator implements TaskActor {
    private String name;
    private CollaboratorCategory category;
    private Project project;
    private List<Subtask> assignedSubtasks;

    public Collaborator(String name, CollaboratorCategory category, Project project) {
        this.name = name;
        this.category = category;
        this.project = project;
        this.assignedSubtasks = new ArrayList<>();
    }

    @Override
    public String getName() { return name; }
    public CollaboratorCategory getCategory() { return category; }
    public Project getProject() { return project; }
    public List<Subtask> getAssignedSubtasks() { return assignedSubtasks; }

    public int getOpenTaskCount() {
        return (int) assignedSubtasks.stream()
                .filter(s -> s.getStatus() == Status.OPEN)
                .count();
    }

    public boolean canAcceptTask() {
        return getOpenTaskCount() < category.getOpenTaskLimit();
    }

    public void assignSubtask(Subtask subtask) {
        assignedSubtasks.add(subtask);
    }
}