DROP SCHEMA IF EXISTS Plannex;
CREATE SCHEMA IF NOT EXISTS Plannex;
USE Plannex;

DROP TABLE IF EXISTS TaskDependencies;
DROP TABLE IF EXISTS TaskAssignees;
DROP TABLE IF EXISTS Artifacts;
DROP TABLE IF EXISTS TimeSpent;
DROP TABLE IF EXISTS Tasks;
DROP TABLE IF EXISTS Projects;
DROP TABLE IF EXISTS Permissions;
DROP TABLE IF EXISTS ProjectEmployees;

CREATE TABLE IF NOT EXISTS Projects (
	ProjectID INT PRIMARY KEY AUTO_INCREMENT,
    ProjectTitle TEXT,
    ProjectDescription TEXT,
    ProjectStart DATE,
    ProjectEnd DATE    
);

CREATE TABLE IF NOT EXISTS ProjectEmployees (
	EmployeeUsername VARCHAR(16) PRIMARY KEY,
    EmployeeName TEXT,
    EmployeeEmail TEXT,
    EmployeePassword TEXT,
    EmployeeWorkingHoursFrom TIME,
    EmployeeWorkingHoursTo TIME
);

CREATE TABLE IF NOT EXISTS Permissions (
	PermissionTitle VARCHAR(7),
    PermissionHolder VARCHAR(16),
    PRIMARY KEY (PermissionTitle, PermissionHolder),
    FOREIGN KEY (PermissionHolder) REFERENCES ProjectEmployees(EmployeeUsername) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Tasks (
	TaskID INT PRIMARY KEY AUTO_INCREMENT,
    ProjectID INT,
    ParentTaskID INT NULL,
    TaskTitle TEXT,
    TaskDescription TEXT,
    TaskStart DATE,
    TaskEnd DATE,
    TaskDurationHours FLOAT,
    FOREIGN KEY (ProjectID) REFERENCES Projects(ProjectID) ON DELETE CASCADE,
    FOREIGN KEY (ParentTaskID) REFERENCES Tasks(TaskID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS TaskDependencies (
	TaskIDFor INT,
    MustComeAfterTaskWithID INT,
    PRIMARY KEY (TaskIDFor, MustComeAfterTaskWithID),
    FOREIGN KEY (TaskIDFor) REFERENCES Tasks(TaskID) ON DELETE CASCADE,
    FOREIGN KEY (MustComeAfterTaskWithID) REFERENCES Tasks(TaskID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS TaskAssignees (
	EmployeeUsername VARCHAR(16),
    TaskID INT,
    PRIMARY KEY (EmployeeUsername, TaskID),
    FOREIGN KEY (EmployeeUsername) REFERENCES ProjectEmployees(EmployeeUsername) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (TaskID) REFERENCES Tasks(TaskID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS TimeSpent (
	OnTaskID INT,
    ByEmployee VARCHAR(16),
    HoursSpent FLOAT,
    _When TIMESTAMP,
    PRIMARY KEY (ByEmployee, _When),
    FOREIGN KEY (OnTaskID) REFERENCES Tasks(TaskID) ON DELETE CASCADE,
    FOREIGN KEY (ByEmployee) REFERENCES ProjectEmployees(EmployeeUsername) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Artifacts (
	TaskID INT,
    ArtifactAuthor VARCHAR(16),
    PathToArtifact VARCHAR(256),
    PRIMARY KEY (TaskID, ArtifactAuthor, PathToArtifact),
    FOREIGN KEY (TaskID) REFERENCES Tasks(TaskID) ON DELETE CASCADE,
    FOREIGN KEY (ArtifactAuthor) REFERENCES ProjectEmployees(EmployeeUsername) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Skills (
    SkillID INT PRIMARY KEY AUTO_INCREMENT,
    SkillTitle VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS EmployeeSkills (
    EmployeeUsername VARCHAR(16),
    SkillID INT,
    SkillLevel ENUM('Intermediate', 'Expert') NOT NULL,
    PRIMARY KEY (EmployeeUsername, SkillID),
    FOREIGN KEY (EmployeeUsername) REFERENCES ProjectEmployees(EmployeeUsername) ON DELETE CASCADE,
    FOREIGN KEY (SkillID) REFERENCES Skills(SkillID) ON DELETE CASCADE
);
