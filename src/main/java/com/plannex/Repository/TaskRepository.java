package com.plannex.Repository;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Task;
import com.plannex.RowMapper.TaskRowMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.naming.OperationNotSupportedException;
import java.util.List;

@Repository
public class TaskRepository {
    public record ConstPair<T, S>(T first, S second) { }

    protected final JdbcTemplate jdbcTemplate;
    protected final TaskRowMapper taskRowMapper;

    public TaskRepository(JdbcTemplate jdbcTemplate, TaskRowMapper taskRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskRowMapper = taskRowMapper;
    }

    public int addTask(Task t) throws OperationNotSupportedException {
        if (t.getParentTaskID() != 0) {
            throw new OperationNotSupportedException("You may not use addTask for adding subtasks.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO Tasks (ProjectID, ParentTaskID, TaskTitle, TaskDescription, TaskStart, TaskEnd, TaskDurationHours)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);",
                    t.getParentProjectID(), t.getParentTaskID() == 0 ? null : t.getParentTaskID(), t.getTaskTitle(), t.getTaskDescription(), t.getTaskStart(), t.getTaskEnd(), t.getTaskDurationHours());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityDoesNotExistException("No project with ID " + t.getParentProjectID() + " exists.");
        }
    }

    public int addSubtask(Task t) throws OperationNotSupportedException {
        Task target = getTaskByID(t.getParentTaskID());
        if (t.getParentTaskID() == 0) {
            throw new OperationNotSupportedException("You may not add tasks with addSubtask.");
        }

        if (target.getParentTaskID() != 0) {
            throw new OperationNotSupportedException("Only tasks can have subtasks, not subtasks.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO Tasks (ProjectID, ParentTaskID, TaskTitle, TaskDescription, TaskStart, TaskEnd, TaskDurationHours)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);",
                    t.getParentProjectID(), t.getParentTaskID(), t.getTaskTitle(), t.getTaskDescription(), t.getTaskStart(), t.getTaskEnd(), t.getTaskDurationHours());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityDoesNotExistException("No project with ID " + t.getParentProjectID() + " exists.");
        }
    }

    public int addFollowsDependency(int forTaskID, int blockedByID) throws OperationNotSupportedException {
        if (forTaskID == blockedByID) {
            throw new OperationNotSupportedException("You may not set a task as blocking itself.");
        }

        if (getTaskByID(forTaskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + forTaskID + " exists.");
        }

        if (getTaskByID(blockedByID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + blockedByID + " exists.");
        }

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

    private boolean employeeDoesNotExist(String username) {
        return !jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM ProjectEmployees WHERE EmployeeUsername = ?;", Boolean.class, username);
    }

    public int assignTaskToEmployee(int taskID, String employeeUsername) {

        try {
            return jdbcTemplate.update("INSERT INTO TaskAssignees (EmployeeUsername, TaskID) VALUES (?, ?);",
                    employeeUsername, taskID);
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("The employee with username " + employeeUsername + " is already assigned the task with ID " + taskID + ".");
        }
    }

    public int unassignTaskFromEmployee(int taskID, String employeeUsername) {

        int rowsDeleted = jdbcTemplate.update("DELETE FROM TaskAssignees WHERE EmployeeUsername = ? AND TaskID = ?;",
                    employeeUsername, taskID);

        if (rowsDeleted != 1) throw new EntityDoesNotExistException("The employee with username " + employeeUsername + " is not assigned the task with ID " + taskID + ".");
        return rowsDeleted;
    }

    public Task getTaskByID(int taskID) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Tasks WHERE TaskID = ?;", taskRowMapper, taskID);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public List<Task> getAllTasksForProject(int projectID) {

        return jdbcTemplate.query("SELECT * FROM Tasks WHERE ProjectID = ? AND ParentTaskID IS NULL;", taskRowMapper, projectID);
    }

    public List<Task> getAllSubtasksForParentTask(int parentTaskID) throws OperationNotSupportedException {
        Task parentTask = getTaskByID(parentTaskID);

        if (parentTask.getParentTaskID() != 0) {
            throw new OperationNotSupportedException("A subtask must have no subtasks.");
        }

        return jdbcTemplate.query("SELECT * FROM Tasks WHERE ParentTaskID = ?;", taskRowMapper, parentTaskID);
    }

    public List<ConstPair<String, String>> getAllArtifactsForTask(int taskID) {

        return jdbcTemplate.query("SELECT * FROM Artifacts WHERE TaskID = ?", (rs, rowNum) -> new ConstPair<>(rs.getString("ArtifactAuthor"), rs.getString("PathToArtifact")), taskID);
    }

    public List<ConstPair<Integer, Integer>> getAllDependenciesForTask(int taskID) {

        return jdbcTemplate.query("SELECT * FROM TaskDependencies WHERE TaskIDFor = ?", (resultSet, rowNum) -> new ConstPair<>(resultSet.getInt("TaskIDFor"), resultSet.getInt("MustComeAfterTaskWithID")), taskID);
    }

    public List<Integer> getAllTimeContributionsForTask(int taskID) {

        return jdbcTemplate.query("SELECT HoursSpent FROM TimeSpent WHERE OnTaskID = ?", (resultSet, rowNum) -> resultSet.getInt("HoursSpent"), taskID);
    }

    public int updateTask(Task modifiedTask, int targetTaskID) {

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

        return jdbcTemplate.update("DELETE FROM Tasks WHERE TaskID = ?;", taskID);
    }
}
