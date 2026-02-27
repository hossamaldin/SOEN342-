## Contract CO2: `updateTask`

**Operation:**  
updateTask(taskId, changes)

**Cross References:**  
Use Case 2: Update Task  

**Preconditions:**  
- A Task with id `taskId` exists  
- `changes` includes at least one modification  
- If a new title is provided, it is not empty  
- If a new priority is provided, it is a valid priority level  
- If a new status is provided, it is one of {open, completed, cancelled}  
- If a new due date is provided, it is a valid date  
- If a project change is provided, the target Project exists  
- If tag updates are provided, the specified tag names are not empty  

**Postconditions:**  
- Let `t` be the existing Task with id `taskId`  
- For each attribute included in `changes`, the corresponding attribute of `t` is set to the new value  
- If a project change is included:  
  - If the task is removed from a project, `t` becomes associated with no Project  
  - If the task is assigned or moved, `t` becomes associated with the specified Project and is no longer associated with any previous Project  
- If tag changes are included, the associations between `t` and Tag(s) are updated accordingly  
- A new ActivityEntry instance `ae` is created  
- `ae.timestamp` is set to the current system date/time  
- `ae.description` records that the task was updated  
- `ae` is associated with `t`
