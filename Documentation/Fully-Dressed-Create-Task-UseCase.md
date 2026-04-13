### Use case UC-01:        Create Task  

### Primary Actor:         User  

### Stakeholders and Interests:  
#### User: 
- Wants quick and accurate task creation, with optional details saved correctly.  

#### System: 
- Must store valid task data and record activity history.  

### Preconditions:  
- User is using the system and has initiated task creation.  

### Success Guarantee (Postconditions):  
- Task is saved in the system.  
- Creation date is set automatically.  
- Status is set to open.  
- Optional fields (description, due date, project, tags) are stored if provided.  
- An activity entry is recorded with a timestamp (“Task created”).  
  
### Technology and Data Variations List:
- Due date via manual entry;
- tags typed or selected;
- Projects are optional.
  
### Open issues:
- Allow duplicate titles?
- Allow past due dates?

---

### Fully-dressed example: Create Task /cont.

### Main success scenario (or basic flow):

- The User selects Create Task.  
- The System displays a task creation form (title, optional description, priority, optional due date, optional project, optional tags).  
- The User enters a title and optionally fills other fields.  
- The User confirms creation.  
- The System validates the inputs (title present; priority valid; project exists if selected).  
- The System creates the task with status open and sets the creation date to the current date/time.  
- The System saves the task.  
- The System records an activity entry (“Task created”) with a timestamp.  
- The System displays the newly created task.

### Extensions (or alternative flows):

- If the title is missing. Indicate error. Do not create task.
  
- If selected project does not exist. Indicate error. Do not create task.

- If due date format is invalid. Indicate error. Do not create task.
