## Contract CO3: `markTaskCompleted`

**Operation:**  
markTaskCompleted(taskId)

**Cross References:**  
Use Case 3: Mark Complete Task  

**Preconditions:**  
- A Task with id `taskId` exists  
- The Task’s status is not `cancelled`  
- The Task’s status is not already `completed`  

**Postconditions:**  
- Let `t` be the existing Task with id `taskId`  
- `t.status` is set to `completed`  
- A new ActivityEntry instance `ae` is created  
- `ae.timestamp` is set to the current system date/time  
- `ae.description` records that the task was marked completed  
- `ae` is associated with `t`
