# SOEN342 Project Winter 2026

## Team Members

- Hossam Mostafa - 40245337
- Muaaz Ahmed - 40299591
- Carlos Guevara - 40227586

---

## Requirements

- **JDK 17 or newer** 

- **Maven 3.6+** (required for Iteration 3 - iCal4j dependency)
  
## How to Run

### With Maven (recommended - required for iCal export)

```bash
mvn compile
mvn exec:java -Dexec.mainClass="taskmanager.Main"
```

### Without Maven (terminal only)

Note: iCal export options (16-18) will not work without Maven.

```bash
cd /path/to/SOEN342-
mkdir -p out
javac -d out $(find src/main/java -name "*.java")
java -cp out taskmanager.Main
```

After the first compile, you only need `java -cp out taskmanager.Main` to relaunch.
Use the numbered menu and type 0 to exit.

---

## Menu Options

| Option | Feature |
|--------|---------|
| 1 | Create Task |
| 2 | View All Tasks |
| 3 | Update Task |
| 4 | Mark Task Complete |
| 5 | Cancel Task |
| 6 | Create Project |
| 7 | Assign Task to Project |
| 8 | Add Subtask |
| 9 | Search Tasks (name, status, priority, date range, day of week) |
| 10 | Export all tasks to CSV |
| 11 | Import tasks from CSV |
| 12 | Set recurring pattern on task (daily / weekly / monthly) |
| 13 | Add collaborator to project |
| 14 | Assign task to collaborator |
| 15 | Export search results to CSV |
| 16 | Export Single Task to iCal (Iteration 3) |
| 17 | Export Project Tasks to iCal (Iteration 3) |
| 18 | Export Filtered Tasks to iCal (Iteration 3) |
| 19 | List Overloaded Collaborators (Iteration 3) |
| 0 | Exit |

---

## Iteration 2 Features

### Task Management
- Create, update, view, mark complete, and cancel tasks
- Tasks have a title, description, due date, priority (LOW / MEDIUM / HIGH), and status (OPEN / COMPLETED / CANCELLED)

### Subtask Management
- Add subtasks to any task (linked to parent)

### Project Management
- Create projects and assign tasks to them
- Add collaborators to projects with a category (JUNIOR / INTERMEDIATE / SENIOR)
- Assign tasks to collaborators - creates a linked subtask automatically

### Search
- Search tasks by name, status, priority, date range, and day of week

### Recurrence
- Set daily, weekly, or monthly recurrence patterns on tasks
- Generates occurrences between a start and end date

### CSV Import / Export
- Export all tasks or search results to a `.csv` file
- Import tasks from a `.csv` file (re-creates tasks, subtasks, projects, and collaborators)

### Collaborator Overload Rules

| Category | Max Open Tasks |
|----------|----------------|
| JUNIOR | 10 |
| INTERMEDIATE | 5 |
| SENIOR | 2 |

---

## Iteration 3 Features

### iCal Export - Gateway Pattern
- Options 16, 17, 18 export tasks to `.ics` files  using the iCal4j library
- Files are saved automatically to the `exports/` folder in the project root
- Only tasks with a due date are exported
- Implemented via `ICalGateway.java` using the Gateway design pattern - iCal4j is fully isolated from the rest of the application

### OCL Constraints

| Constraint | Limit |
|------------|-------|
| Subtasks per task | max 20 |
| Open tasks without a due date | max 50 |

### Overload Detection
- Option 19 lists any collaborator currently exceeding their category open-task limit

---

## CSV Notes

- Export always writes a header row; import automatically skips it
- Column order: `TaskName, Description, Subtask, Status, Priority, DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory`

---

## IDE Setup

Open the project in IntelliJ IDEA as a Maven project.
Run `mvn compile` first to pull in dependencies, then run `main` in `src/main/java/taskmanager/Main.java`.

