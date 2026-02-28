## Contract CO7: `assignTaskToProject`

**Operation:**  
assignTaskToProject(taskId, projectId)

**Cross References:**  
Use Case 7: Assign / Move Task to Project  

**Preconditions:**  
- A Task with id `taskId` exists  
- A Project with id `projectId` exists  

**Postconditions:**  
- Let `t` be the existing Task with id `taskId`  
- Let `p` be the existing Project with id `projectId`  
- If `t` was previously associated with a Project, that association is removed  
- `t` becomes associated with `p` (and `p` includes `t` in its set of Tasks)  
- A new ActivityEntry instance `ae` is created  
- `ae.timestamp` is set to the current system date/time  
- `ae.description` records that the task was assigned/moved to a project  
- `ae` is associated with `t`
