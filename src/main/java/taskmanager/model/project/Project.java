package taskmanager.model.project;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private String description;
    private List<Collaborator> collaborators;

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.collaborators = new ArrayList<>();
    }

    public void addCollaborator(Collaborator collaborator) {
        collaborators.add(collaborator);
    }

    public Collaborator findCollaborator(String name) {
        return collaborators.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Collaborator> getCollaborators() { return collaborators; }
}