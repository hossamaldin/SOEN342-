## Contract CO5: `viewTasks`

**Operation:**  
viewTasks(filterType)

**Cross References:**  
Use Case 5: View Tasks  

**Preconditions:**  
- `filterType` specifies a supported viewing mode (e.g., by due date, by priority, by status, by project, by tag, by specific date, or by time range)  
- If `filterType` requires a date or time range, the provided date(s) are valid  
- If `filterType` refers to a Project, the Project exists  
- If `filterType` refers to a Tag, the Tag exists  

**Postconditions:**  
- A list `taskList` is returned containing all Tasks that match `filterType`  
