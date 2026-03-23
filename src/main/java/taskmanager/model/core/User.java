package taskmanager.model.core;

import taskmanager.model.interfaces.TaskActor;

public class User implements TaskActor {
    private String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}