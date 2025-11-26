package com.plannex.Repository;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.InvalidValueException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.RowMapper.ProjectEmployeeRowMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProjectEmployeeRepository {
    protected final JdbcTemplate jdbcTemplate;
    protected final ProjectEmployeeRowMapper projectEmployeeRowMapper;

    public ProjectEmployeeRepository(JdbcTemplate jdbcTemplate, ProjectEmployeeRowMapper projectEmployeeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectEmployeeRowMapper = projectEmployeeRowMapper;
    }

    @Transactional
    public int addEmployee(ProjectEmployee employee, String permissions) {
        int rowsAffectedTotal = 0;

        try {
            rowsAffectedTotal = jdbcTemplate.update("INSERT INTO ProjectEmployees (EmployeeUsername, EmployeeName, EmployeeEmail, EmployeePassword, EmployeeWorkingHoursFrom, EmployeeWorkingHoursTo) VALUES (?, ?, ?, ?, ?, ?);",
                    employee.getEmployeeUsername(), employee.getEmployeeName(), employee.getEmployeeEmail(), employee.getEmployeePassword(), employee.getWorkingHoursFrom(), employee.getWorkingHoursTo());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("An employee with username " + employee.getEmployeeUsername() + " already exists.");
        }

        try {
            rowsAffectedTotal += jdbcTemplate.update("INSERT INTO Permissions (PermissionTitle, PermissionHolder) VALUES (?, ?);",
                    permissions, employee.getEmployeeUsername());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityDoesNotExistException("No employee with username " + employee.getEmployeeUsername() + " exists.");
        }

        return rowsAffectedTotal;
    }

    public ProjectEmployee getEmployeeByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM ProjectEmployees WHERE EmployeeUsername = ?;", projectEmployeeRowMapper, username);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public List<ProjectEmployee> getAllEmployees() {
        try {
            return jdbcTemplate.query("SELECT * FROM ProjectEmployees;", projectEmployeeRowMapper);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public String getEmployeePermissions(String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT PermissionTitle FROM Permissions WHERE PermissionHolder = ?;", String.class, username);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
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

    @Transactional // Important because this cascades
    public int updateEmployee(ProjectEmployee updatedProjectEmployee, String targetUsername) {
        if (getEmployeeByUsername(targetUsername) == null) {
            throw new EntityDoesNotExistException("No employee with username " + targetUsername + " exists.");
        }

        try {
            return jdbcTemplate.update("UPDATE ProjectEmployees" +
                    " SET EmployeeUsername = ?, EmployeeName = ?, EmployeeEmail = ?, EmployeePassword = ?, EmployeeWorkingHoursFrom = ?, EmployeeWorkingHoursTo = ?" +
                    " WHERE EmployeeUsername = ?;", updatedProjectEmployee.getEmployeeUsername(), updatedProjectEmployee.getEmployeeName(),
                                                   updatedProjectEmployee.getEmployeeEmail(), updatedProjectEmployee.getEmployeePassword(),
                                                   updatedProjectEmployee.getWorkingHoursFrom(), updatedProjectEmployee.getWorkingHoursTo(), targetUsername); // rowsAffected
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("A different user with username " + updatedProjectEmployee.getEmployeeUsername() + " already exists.");
        }
    }

    private boolean artifactWithValuesExists(int taskID, String username, String pathToArtifact) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM Artifacts WHERE TaskID = ? AND ArtifactAuthor = ? AND PathToArtifact = ?;",
                Boolean.class, taskID, username, pathToArtifact));
    }

    public int addArtifact(int taskID, String username, String pathToArtifact) {
        if (getEmployeeByUsername(username) == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
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
        if (getEmployeeByUsername(username) == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }

        if (!artifactWithValuesExists(taskID, username, pathToArtifact)) {
            throw new EntityDoesNotExistException("The artifact with path " + pathToArtifact + " does not exist, uploaded by " + username + " for task with ID " + taskID + ".");
        }

        if (artifactWithValuesExists(taskID, username, newPath)) {
            throw new EntityAlreadyExistsException("The artifact with path " + newPath + " already exists. Change its name or delete and replace it.");
        }

        return jdbcTemplate.update("UPDATE Artifacts SET PathToArtifact = ? WHERE PathToArtifact = ?;", newPath, pathToArtifact);
    }

    public int deleteArtifact(int taskID, String username, String path) {
        if (getEmployeeByUsername(username) == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }

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
        if (getEmployeeByUsername(username) == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }

        if (howManyHours < 0.0) {
            throw new InvalidValueException("Hours spent should be zero or more.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO TimeSpent (OnTaskID, ByEmployee, HoursSpent, _When) VALUES (?, ?, ?, ?);",
                    taskID, username, howManyHours, LocalDateTime.now());
        } catch (DataIntegrityViolationException dive) { // Also possible that a duplicate exists, but extremely unlikely
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }
    }

    public int updateTimeContribution(String username, int taskID, float howManyHours, LocalDateTime when) {
        if (getEmployeeByUsername(username) == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }

        if (howManyHours < 0.0) {
            throw new InvalidValueException("Hours spent should be zero or more.");
        }

        if (timeContributionWithValuesDoesNotExist(username, taskID, when)) {
            throw new EntityDoesNotExistException("No time contribution by " + username + " on task with ID " + taskID + " at " + when + " exists.");
        }

        return jdbcTemplate.update("UPDATE TimeSpent SET HoursSpent = ? WHERE OnTaskID = ? AND ByEmployee = ? AND _When = ?;",
                    howManyHours, taskID, username, when);
    }

    public int deleteTimeContribution(String username, int taskID, float howManyHours, LocalDateTime when) {
        if (getEmployeeByUsername(username) == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }

        if (timeContributionWithValuesDoesNotExist(username, taskID, when)) {
            throw new EntityDoesNotExistException("No time contribution by " + username + " on task with ID " + taskID + " at " + when + " exists.");
        }

        return jdbcTemplate.update("DELETE FROM TimeSpent WHERE ByEmployee = ? AND _When = ?;", username, when);
    }

    @Transactional // Important because this cascades
    public int deleteEmployeeByUsername(String targetUsername) {
        if (getEmployeeByUsername(targetUsername) == null) {
            throw new EntityDoesNotExistException("No user with username " + targetUsername + " exists.");
        }

        return jdbcTemplate.update("DELETE FROM ProjectEmployees WHERE EmployeeUsername = ?;", targetUsername); // rowsAffected
    }
}
