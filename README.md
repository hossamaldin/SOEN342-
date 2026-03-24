# SOEN342 Project Winter 2026

## Team members

- Hossam Mostafa — 40245337  
- Muaaz Ahmed — 40299591  
- Carlos Guevara — 40227586  

## What this is

A **console** personal task manager (Java). Tasks live in memory only: each run starts with an empty “database” unless you **import** from a CSV file you exported earlier.

## Requirements

- **JDK 17 or newer** recommended (`java` and `javac` on your PATH). Check with:

  ```bash
  java -version
  javac -version
  ```

## How to run (terminal)

From the project root (`SOEN342--1`):

```bash
cd /path/to/SOEN342--1
mkdir -p out
javac -d out $(find src/main/java -name "*.java")
java -cp out taskmanager.Main
```

- After the first successful compile, you only need `java -cp out taskmanager.Main` to start the app again (unless you change the code).
- Use the numbered menu; type **`0`** to exit.

## Main features (menu)

| Option | Feature |
|--------|--------|
| 2 | View all tasks |
| 9 | Search tasks (criteria: name, status, priority, date range, day of week) |
| 10 | **Export all tasks** to a CSV file |
| 11 | **Import tasks** from a CSV file (path you type when prompted) |
| 15 | Export **search results** to CSV (after entering search criteria) |

Other options cover creating/updating tasks, projects, subtasks, recurrence, and collaborators.

## CSV notes

- Export writes a header row; import skips that same header.
- Column order is defined in code (`CsvService.CSV_HEADER` in `src/main/java/taskmanager/service/CsvService.java`).

## IDE

Open the folder in IntelliJ IDEA, Cursor, or VS Code with a Java extension. Run the `main` method in `src/main/java/taskmanager/Main.java`, with the **output / classes directory** on the classpath and the **module/source root** set so `taskmanager` is the package root.
