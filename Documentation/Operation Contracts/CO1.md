## Contract CO1: `createTask`

**Operation:**  
createTask(title, description, priority, dueDate)

**Cross References:**  
Use Case 1: Create Task  

**Preconditions:**  
- title is provided and not empty  
- priority is a valid priority level  
- if dueDate is provided, it is a valid date  

**Postconditions:**  
- A new Task instance `t` is created  
- `t.title = title`  
- `t.description = description` (or null if not provided)  
- `t.priority = priority`  
- `t.dueDate = dueDate` (or null if not provided)  
- `t.status = open`  
- `t.creationDate` is set to the current system date/time  
- `t` is stored in the system  
- A new ActivityEntry instance `ae` is created  
- `ae.timestamp` is set to the current system date/time  
- `ae.description` records that the task was created  
- `ae` is associated with `t`  
