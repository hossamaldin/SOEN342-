## Contract CO6: `createProject`

**Operation:**  
createProject(name, description)

**Cross References:**  
Use Case 6: Create Project  

**Preconditions:**  
- `name` is provided and not empty  
- No existing Project has the same name `name`  

**Postconditions:**  
- A new Project instance `p` is created  
- `p.name = name`  
- `p.description = description` (or null if not provided)  
- `p` is stored in the system  
