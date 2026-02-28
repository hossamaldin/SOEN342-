## Contract CO4: `cancelTask`

**Operation:**  
cancelTask(taskId)

**Cross References:**  
Use Case 4: Cancel Task  

**Preconditions:**  
- A Task with id `taskId` exists  
- The Task’s status is not `completed`  
- The Task’s status is not already `cancelled`  

**Postconditions:**  
- Let `t` be the existing Task with id `taskId`  
- `t.status` is set to `cancelled`  
- A new ActivityEntry instance `ae` is created  
- `ae.timestamp` is set to the current system date/time  
- `ae.description` records that the task was cancelled  
- `ae` is associated with `t`
