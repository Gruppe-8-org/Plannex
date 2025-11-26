USE Plannex;

INSERT INTO Projects (ProjectID, ProjectTitle, ProjectDescription, ProjectStart, ProjectEnd) VALUES
	(1, 'The Plannex Project', 'A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management', '20251112', '20251217'),
    (2, 'Coffee machine repairs on the second floor', 'The coffee machine has been broken for a grueling three days now.\nCalls to the repairman revealed that we could do this ourselves to save money.\nShould the machine remain in disrepair, none of our projects will be released on schedule.', '20251112', '20251113'),
    (3, 'Secret Santa but in the Danish way', 'Christmas is around the corner and it is an office tradition.\nHere, employees sign up to torment others and to being tormented by others.\nWarning: Extreme pranks (razor blades hidden in otherwise delicious fudge, irritants placed on toilet paper, ordering colleagues to write valid tar commands without access to the manual pages, etc.) will subject you to disciplinary action.', '20251201', '20251220'),
    (4, 'Calculator SaaS', 'Our customers desparately want a calculator stored in the cloud, this is our answer to their prayers.\nIn essence, it is an expression lexer, parser and evaluator. It is to support:\n* Parenthesized expressions\n* User-defined and built in functions\n* Testable components', '20260101', '20260108');

INSERT INTO ProjectEmployees (EmployeeUsername, EmployeeName, EmployeeEmail, EmployeePassword, EmployeeWorkingHoursFrom, EmployeeWorkingHoursTo) VALUES
	('lildawg', 'Max-Emil', 'MES@gmail.com', 'fAbc#21Y', '08:00:00', '16:00:00'),
    ('bigdawg', 'Max', 'MRK@gmail.com', '0uFF!nÆr','08:00:00', '16:00:00'),
    ('marqs', 'Markus', 'MBR@gmail.com', 'HhQEsN4t','08:00:00', '16:00:00'),
    ('RandomWorker', 'Random', 'RW@gmail.com', 'notSecure','08:00:00', '16:00:00');

INSERT INTO Permissions (PermissionTitle, PermissionHolder) VALUES
	('Manager', 'lildawg'),
    ('Manager', 'bigdawg'),
    ('Manager', 'marqs'),
    ('Worker', 'RandomWorker');

INSERT INTO Tasks (TaskID, ProjectID, ParentTaskID, TaskTitle, TaskDescription, TaskStart, TaskEnd, TaskDurationHours) VALUES
	(1, 1, NULL, 'Project startup', 'Building a good foundation for the actual work to come later.', '20251112', '20251113', 22.667),
	(2, 1, 1, 'Set up GitHub project', 'Go to github.com, register an organization if not already done, then create a project with title "plannex"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.', '20251112', '20251112', 0.5),
    (3, 1, 1, 'Set up report document for collaborative work', 'Go to docs.google.com, create a new document and then a front page. Invite collaborators.\nFill in as you work.', '20251112', '20251112', 0.1667),
    (4, 1, 1, 'Feature planning', 'Discuss potential features using the slide deck from our customer. Write these broad ideas down', '20251112', '20251112', 2),
    (5, 1, 1, 'Domain model', 'Make a domain model diagram from the notes written for potential features.', '20251112', '20251112', 1),
    (6, 1, 1, 'User stories', 'Write user stories using the notes from potential features', '20251113', '20251113', 2),
    (7, 1, 1, 'UI prototype','Make a UI prototype with Figma based on the notes from the potential features.', '20251112', '20251118', 17),
    
    (8, 1, NULL, 'Database setup and implementation (repository)', 'You need:\n* ER-diagram\n* Schema-script\n* Data-script\n* Repository class\n\t* Model classes\n\t* RowMappers\n* Configuration script for the database\n* Integration tests', '20251121', '20251125', 14.667),
    (9, 1, 8, 'ER-diagram for the database', 'Use the domain model and feature notes + user stories.', '20251121', '20251121', 1),
    (10, 1, 8, 'Schema script', 'Implement tables inspired by the domain model and the UI-prototype from the planning phase.', '20251121', '20251121', 0.5),
    (11, 1, 8, 'Data script', 'Fill the tables with hardcoded data to be used for demonstration.', '20251123', '20251123', 3),
    (12, 1, 8, 'Model classes', 'Use your new tables to build models in Java.', '20251123', '20251123', 1),
    (13, 1, 8, 'RowMappers', 'Build RowMappers for your model classes', '20251123', '20251123', 1),
    (14, 1, 8, 'Configuration script', 'Write a database configuration script and set the main script to point to it.', '20251124', '20251124', 0.1667),
    (15, 1, 8, 'Repository classes', 'Build repository classes for the models you just built.', '20251124', '20251124', 3),
    (16, 1, 8, 'Integration tests', 'Write integration tests for each of the repository classes.', '20251124', '20251125', 5);

-- Assuming this is best since a later task is likely created after an earlier one
-- Possibly makes optimal path calculation more difficult as it is to be done in reverse
-- War between user experience and convenience when planning.
INSERT INTO TaskDependencies (TaskIDFor, MustComeAfterTaskWithID) VALUES
	(5, 4),
    (6, 5),
    (7, 6),
    (8, 1),
    (10, 9),
    (11, 10),
    (12, 11),
    (13, 12),
    (15, 13),
    (16, 15);

INSERT INTO TaskAssignees (TaskID, EmployeeUsername) VALUES
	(2, 'marqs'),
    (3, 'marqs'),
    (4, 'marqs'),
    (4, 'lildawg'),
    (4, 'bigdawg'),
    (5, 'lildawg'),
    (6, 'marqs'),
    (6, 'lildawg'),
    (6, 'bigdawg'),
    (7, 'lildawg'),
    (9, 'lildawg'),
    (10, 'lildawg'),
    (11, 'lildawg'),
    (12, 'lildawg'),
    (13, 'lildawg'),
    (14, 'lildawg'),
    (15, 'lildawg'),
    (16, 'lildawg');

INSERT INTO Artifacts (TaskID, ArtifactAuthor, PathToArtifact) VALUES
	(2, 'marqs', 'github.com/Gruppe-8-org/plannex'),
    (3, 'marqs', 'docs.google.com/rapport'),
    (5, 'lildawg', 'docs/domain_model_plannex.png'),
    (6, 'marqs', 'docs.google.com/rapport'),
    (7, 'lildawg', 'figma.com/'),
    (9, 'lildawg', 'docs/ER_diagram_plannex.png'),
    (10, 'lildawg', 'resources/schema.sql'),
    (11, 'lildawg', 'resources/data.sql'),
    (12, 'lildawg', 'src/main/plannex/Model/Project.java'),
    (12, 'lildawg', 'src/main/plannex/Model/Task.java'),
    (12, 'lildawg', 'src/main/plannex/Model/ProjectEmployee.java'),
	(13, 'lildawg', 'src/main/plannex/RowMapper/ProjectRowMapper.java'),
    (13, 'lildawg', 'src/main/plannex/RowMapper/TaskRowMapper.java'),
    (13, 'lildawg', 'src/main/plannex/RowMapper/ProjectEmployeeRowMapper.java'),
    (14, 'lildawg', 'resources/config-test.cfg'),
    (15, 'lildawg', 'src/main/plannex/Repository/ProjectRepository.java'),
    (15, 'lildawg', 'src/main/plannex/Repository/TaskRepository.java'),
    (15, 'lildawg', 'src/test/plannex/Repository/ProjectEmployeeRepository.java');
    
INSERT INTO TimeSpent (OnTaskID, ByEmployee, HoursSpent, _When) VALUES 
(2, 'marqs', 0.5, '2025-11-12 10:00:00'), (3, 'marqs', 0.1667, '2025-11-12 10:30:00'), (4, 'marqs', 0.75, '2025-11-12 10:45:00'), (4, 'lildawg', 0.75, '2025-11-12 10:45:00'), (4, 'bigdawg', 0.5, '2025-11-12 10:45:00'), 
(5, 'lildawg', 1, '2025-11-12 11:30:00'), (6, 'marqs', 1, '2025-11-13 11:15:00'), (6, 'lildawg', 1, '2025-11-13 11:15:00'), 
(7, 'lildawg', 5, '2025-11-13 13:30:00'), (7, 'lildawg', 5, '2025-11-14 08:00:00'), (7, 'lildawg', 5, '2025-11-16 08:00:00'), (7, 'lildawg', 2, '2025-11-17 08:00:00'),
(9, 'lildawg', 1, '2025-11-17 11:30:00'), (10, 'lildawg', 0.5, '2025-11-17 12:30:00'), (11, 'lildawg', 3, '2025-11-17 13:00:00'), (12, 'lildawg', 1, '2025-11-18 08:00:00'),
(13, 'lildawg', 1, '2025-11-18 09:00:00'), (14, 'lildawg', 0.1667, '2025-11-18 10:00:00'), (15, 'lildawg', 3, '2025-11-18 10:10:00'), (16, 'lildawg', 4, '2025-11-19 08:00:00'),
(16, 'lildawg', 4, '2025-11-19 14:00:00');