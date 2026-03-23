package taskmanager.model.core;

public class Tag {
    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return name.equals(((Tag) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}