package com.plannex.Repository;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.InvalidValueException;
import com.plannex.Exception.NotSupportedException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.RowMapper.ProjectEmployeeRowMapper;
import com.plannex.RowMapper.TaskRowMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TaskRepository {
    public record ConstPair<T, S>(T first, S second) { }

    protected final JdbcTemplate jdbcTemplate;
    protected final TaskRowMapper taskRowMapper;
    protected final ProjectEmployeeRowMapper projectEmployeeRowMapper;

    public TaskRepository(JdbcTemplate jdbcTemplate, TaskRowMapper taskRowMapper, ProjectEmployeeRowMapper projectEmployeeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskRowMapper = taskRowMapper;
        this.projectEmployeeRowMapper = projectEmployeeRowMapper;
    }

    public boolean isSubtask(Task t) { // Parameter may need changing to ID
        return t.getParentTaskID() != 0;
    }

    public int addTask(Task t) {
        if (isSubtask(t)) {
            throw new NotSupportedException("You may not use addTask for adding subtasks.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO Tasks (ProjectID, ParentTaskID, TaskTitle, TaskDescription, TaskStart, TaskEnd, TaskDurationHours)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);",
                    t.getParentProjectID(), t.getParentTaskID() == 0 ? null : t.getParentTaskID(), t.getTaskTitle(), t.getTaskDescription(), t.getTaskStart(), t.getTaskEnd(), t.getTaskDurationHours());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityDoesNotExistException("No project with ID " + t.getParentProjectID() + " exists.");
        }
    }

    public int addSubtask(Task t) {
        if (!isSubtask(t)) {
            throw new NotSupportedException("You may not add tasks with addSubtask.");
        }

        Task target = getTaskByID(t.getParentTaskID());

        if (target == null) {
            throw new EntityDoesNotExistException("No task with ID " + t.getParentTaskID() + " exists.");
        }

        if (target.getParentTaskID() != 0) {
            throw new NotSupportedException("Only tasks can have subtasks, not subtasks.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO Tasks (ProjectID, ParentTaskID, TaskTitle, TaskDescription, TaskStart, TaskEnd, TaskDurationHours)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);",
                    t.getParentProjectID(), t.getParentTaskID(), t.getTaskTitle(), t.getTaskDescription(), t.getTaskStart(), t.getTaskEnd(), t.getTaskDurationHours());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityDoesNotExistException("No project with ID " + t.getParentProjectID() + " exists.");
        }
    }

    public int addFollowsDependency(int forTaskID, int blockedByID) {
        if (forTaskID == blockedByID) {
            throw new NotSupportedException("You may not set a task as blocking itself.");
        }

        if (getTaskByID(forTaskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + forTaskID + " exists.");
        }

        if (getTaskByID(blockedByID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + blockedByID + " exists.");
        }

        // Possibly add isSubtask() call here if dependencies get cluttered by allowing both tasks and subtasks to have them.

        try {
            return jdbcTemplate.update("INSERT INTO TaskDependencies (TaskIDFor, MustComeAfterTaskWithID) VALUES (?, ?);",
                    forTaskID, blockedByID);
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("The task with ID " + forTaskID + " is already marked as blocked by the task with ID " + blockedByID + ".");
        }
    }

    public int deleteFollowsDependency(int forTaskID, int blockedByID) {
        if (getTaskByID(forTaskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + forTaskID + " exists.");
        }

        if (getTaskByID(blockedByID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + blockedByID + " exists.");
        }

        int rowsAffected = jdbcTemplate.update("DELETE FROM TaskDependencies WHERE TaskIDFor = ? AND MustComeAfterTaskWithID = ?;",
                    forTaskID, blockedByID);

        if (rowsAffected != 1) {
            throw new EntityDoesNotExistException("The task with ID " + forTaskID + " is not marked as blocked by the task with ID " + blockedByID + ".");
        }

        return rowsAffected;
    }

    public int assignTaskToEmployee(int taskID, String employeeUsername) {
        if (getTaskByID(taskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        if (!isSubtask(getTaskByID(taskID))) {
            throw new NotSupportedException("You may only assign workers to subtasks.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO TaskAssignees (EmployeeUsername, TaskID) VALUES (?, ?);",
                    employeeUsername, taskID);
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("The employee with username " + employeeUsername + " is already assigned the task with ID " + taskID + ".");
        }
    }

    public int unassignTaskFromEmployee(int taskID, String employeeUsername) {
        if (getTaskByID(taskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        int rowsDeleted = jdbcTemplate.update("DELETE FROM TaskAssignees WHERE EmployeeUsername = ? AND TaskID = ?;",
                    employeeUsername, taskID);

        if (rowsDeleted != 1) throw new EntityDoesNotExistException("The employee with username " + employeeUsername + " is not assigned the task with ID " + taskID + ".");
        return rowsDeleted;
    }

    public Task getTaskByID(int taskID) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Tasks WHERE TaskID = ?;", taskRowMapper, taskID);
        } catch (EmptyResultDataAccessException erdae) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }
    }

    public List<Task> getAllSubtasksForParentTask(int parentTaskID) {
        Task parentTask = getTaskByID(parentTaskID);
        if (parentTask == null) {
            throw new EntityDoesNotExistException("No task with ID " + parentTaskID + " exists.");
        }

        if (parentTask.getParentTaskID() != 0) {
            throw new NotSupportedException("A subtask must have no subtasks.");
        }

        return jdbcTemplate.query("SELECT * FROM Tasks WHERE ParentTaskID = ?;", taskRowMapper, parentTaskID);
    }

    public List<ConstPair<String, String>> getAllArtifactsForTask(int taskID) {
        if (getTaskByID(taskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        if (isSubtask(getTaskByID(taskID))) {
            return jdbcTemplate.query("SELECT * FROM Artifacts WHERE TaskID = ?", (rs, rowNum) -> new ConstPair<>(rs.getString("ArtifactAuthor"), rs.getString("PathToArtifact")), taskID);
        }

        return getAllSubtasksForParentTask(taskID).stream().map(t -> getAllArtifactsForTask(t.getID())).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<ConstPair<Integer, Integer>> getAllDependenciesForTask(int taskID) {
        if (getTaskByID(taskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        return jdbcTemplate.query("SELECT * FROM TaskDependencies WHERE TaskIDFor = ?", (resultSet, rowNum) -> new ConstPair<>(resultSet.getInt("TaskIDFor"), resultSet.getInt("MustComeAfterTaskWithID")), taskID);
    }

    public List<ProjectEmployee> getAllAssigneesForSubtask(int subtaskID) {
        try {
            return jdbcTemplate.query("SELECT DISTINCT ProjectEmployees.* FROM TaskAssignees\n" +
                            "LEFT JOIN ProjectEmployees on TaskAssignees.EmployeeUsername = ProjectEmployees.EmployeeUsername WHERE TaskID = ?;",
                    projectEmployeeRowMapper, subtaskID);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public List<ProjectEmployee> getAllAssigneesForTask(int taskID) {
        try {
            return jdbcTemplate.query("SELECT DISTINCT ProjectEmployees.* FROM Tasks\n" +
                            "LEFT JOIN TaskAssignees ON TaskAssignees.TaskID = Tasks.TaskID\n" +
                            "LEFT JOIN ProjectEmployees ON ProjectEmployees.EmployeeUsername = TaskAssignees.EmployeeUsername\n" +
                            "WHERE ParentTaskID = ?;",
                    projectEmployeeRowMapper, taskID);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    private boolean artifactWithValuesExists(int taskID, String username, String pathToArtifact) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM Artifacts WHERE TaskID = ? AND ArtifactAuthor = ? AND PathToArtifact = ?;",
                Boolean.class, taskID, username, pathToArtifact));
    }

    public int addArtifact(int taskID, String username, String pathToArtifact) {
        if (!isSubtask(getTaskByID(taskID))) {
            throw new NotSupportedException("You may only add artifacts to subtasks.");
        }

        if (artifactWithValuesExists(taskID, username, pathToArtifact)) {
            throw new EntityAlreadyExistsException("Employee " + username + " has already uploaded this artifact to this task. Update it if you want to change it.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO Artifacts (TaskID, ArtifactAuthor, PathToArtifact) VALUES (?, ?, ?);",
                    taskID, username, pathToArtifact);
        } catch (DataIntegrityViolationException dive) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }
    }

    public int updateArtifact(int taskID, String username, String pathToArtifact, String newPath) {
        if (!artifactWithValuesExists(taskID, username, pathToArtifact)) {
            throw new EntityDoesNotExistException("The artifact with path " + pathToArtifact + " does not exist, uploaded by " + username + " for task with ID " + taskID + ".");
        }

        if (artifactWithValuesExists(taskID, username, newPath)) {
            throw new EntityAlreadyExistsException("The artifact with path " + newPath + " already exists. Change its name or delete and replace it.");
        }

        return jdbcTemplate.update("UPDATE Artifacts SET PathToArtifact = ? WHERE PathToArtifact = ?;", newPath, pathToArtifact);
    }

    public int deleteArtifact(int taskID, String username, String path) {
        if (!artifactWithValuesExists(taskID, username, path)) {
            throw new EntityDoesNotExistException("The artifact with path " + path + " does not exist, uploaded by " + username + " for task with ID " + taskID + ".");
        }

        return jdbcTemplate.update("DELETE FROM Artifacts WHERE TaskID = ? AND ArtifactAuthor = ? AND PathToArtifact = ?;", taskID, username, path);
    }

    private boolean timeContributionWithValuesDoesNotExist(String username, int taskID, LocalDateTime when) {
        return !Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TimeSpent WHERE OnTaskID = ? AND ByEmployee = ? AND _When = ?;",
                Boolean.class, taskID, username, when));
    }

    public int contributeTime(String username, int taskID, float howManyHours) {
        if (howManyHours < 0.0) {
            throw new InvalidValueException("Hours spent should be zero or more.");
        }

        if (!isSubtask(getTaskByID(taskID))) {
            throw new NotSupportedException("You may only add time spent to subtasks.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO TimeSpent (OnTaskID, ByEmployee, HoursSpent, _When) VALUES (?, ?, ?, ?);",
                    taskID, username, howManyHours, LocalDateTime.now());
        } catch (DataIntegrityViolationException dive) { // Also possible that a duplicate exists, but extremely unlikely without bad short term memory loss
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }
    }

    public int updateTimeContribution(String username, int taskID, float howManyHours, LocalDateTime when) {
        if (howManyHours < 0.0) {
            throw new InvalidValueException("Hours spent should be zero or more.");
        }

        if (timeContributionWithValuesDoesNotExist(username, taskID, when)) {
            throw new EntityDoesNotExistException("No time contribution by " + username + " on task with ID " + taskID + " at " + when + " exists.");
        }
        // For update, delete, no check of isSubtask since made impossible by check in contributeTime(), also reinforced by UI.
        return jdbcTemplate.update("UPDATE TimeSpent SET HoursSpent = ? WHERE OnTaskID = ? AND ByEmployee = ? AND _When = ?;",
                howManyHours, taskID, username, when);
    }

    public int deleteTimeContribution(String username, int taskID, LocalDateTime when) {
        if (timeContributionWithValuesDoesNotExist(username, taskID, when)) {
            throw new EntityDoesNotExistException("No time contribution by " + username + " on task with ID " + taskID + " at " + when + " exists.");
        }

        return jdbcTemplate.update("DELETE FROM TimeSpent WHERE ByEmployee = ? AND _When = ?;", username, when);
    }

    public List<Float> getAllTimeContributionsForTask(int taskID) {
        Task task = getTaskByID(taskID);
        if (task == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        if (isSubtask(task)) {
            return jdbcTemplate.query("SELECT HoursSpent FROM TimeSpent WHERE OnTaskID = ?", (resultSet, rowNum) -> resultSet.getFloat("HoursSpent"), taskID);
        }

        // Get list of time contributions from all the subtasks of the parent task
        return getAllSubtasksForParentTask(taskID).stream().map(t -> getAllTimeContributionsForTask(t.getID())).flatMap(List::stream).collect(Collectors.toList());
    }

    public int updateTask(Task modifiedTask, int targetTaskID) {
        if (getTaskByID(targetTaskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + targetTaskID + " exists.");
        }

        return jdbcTemplate.update("UPDATE Tasks " +
                "SET ProjectID = ?, ParentTaskID = ?, TaskTitle = ?, TaskDescription = ?, TaskStart = ?," +
                        " TaskEnd = ?, TaskDurationHours = ? WHERE TaskID = ?;",
                modifiedTask.getParentProjectID(),
                modifiedTask.getParentTaskID() == 0 ? null : modifiedTask.getParentTaskID(),
                modifiedTask.getTaskTitle(), modifiedTask.getTaskDescription(), modifiedTask.getTaskStart(),
                modifiedTask.getTaskEnd(), modifiedTask.getTaskDurationHours(), targetTaskID
        );
    }

    public int deleteTaskByID(int taskID) {
        if (getTaskByID(taskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        return jdbcTemplate.update("DELETE FROM Tasks WHERE TaskID = ?;", taskID);
    }
}
