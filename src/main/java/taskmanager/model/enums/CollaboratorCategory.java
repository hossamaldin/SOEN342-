package taskmanager.model.enums;

public enum CollaboratorCategory {
    JUNIOR(10),
    INTERMEDIATE(5),
    SENIOR(2);

    private int openTaskLimit;

    CollaboratorCategory(int openTaskLimit) {
        this.openTaskLimit = openTaskLimit;
    }

    public int getOpenTaskLimit() { return openTaskLimit; }
    public void setOpenTaskLimit(int openTaskLimit) { this.openTaskLimit = openTaskLimit; }
}