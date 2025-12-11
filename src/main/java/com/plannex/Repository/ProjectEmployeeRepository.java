package com.plannex.Repository;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.RowMapper.ProjectEmployeeRowMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        int rowsAffectedTotal;

        try {
            rowsAffectedTotal = jdbcTemplate.update("INSERT INTO ProjectEmployees (EmployeeUsername, EmployeeName, EmployeeEmail, EmployeePassword, EmployeeWorkingHoursFrom, EmployeeWorkingHoursTo) VALUES (?, ?, ?, ?, ?, ?);",
                    employee.getEmployeeUsername(), employee.getEmployeeName(), employee.getEmployeeEmail(), employee.getEmployeePassword(), employee.getWorkingHoursFrom(), employee.getWorkingHoursTo());
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("An employee with username " + employee.getEmployeeUsername() + " already exists.");
        }

        return rowsAffectedTotal + jdbcTemplate.update("INSERT INTO Permissions (PermissionTitle, PermissionHolder) VALUES (?, ?);",
                    permissions, employee.getEmployeeUsername());
    }

    public ProjectEmployee getEmployeeByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM ProjectEmployees WHERE EmployeeUsername = ?;", projectEmployeeRowMapper, username);
        } catch (EmptyResultDataAccessException erdae) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }
    }

    public List<ProjectEmployee> getAllEmployees() {
        return jdbcTemplate.query("SELECT * FROM ProjectEmployees;", projectEmployeeRowMapper);
    }

    public String getEmployeePermissions(String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT PermissionTitle FROM Permissions WHERE PermissionHolder = ?;", String.class, username);
        } catch (EmptyResultDataAccessException erdae) {
            throw new EntityDoesNotExistException("No permissions registered for user with username " + username + ".");
        }
    }

    public int updateEmployee(ProjectEmployee updatedProjectEmployee, String targetUsername) {
        getEmployeeByUsername(targetUsername);

        try {
            return jdbcTemplate.update("UPDATE ProjectEmployees" +
                    " SET EmployeeUsername = ?, EmployeeName = ?, EmployeeEmail = ?, EmployeePassword = ?, EmployeeWorkingHoursFrom = ?, EmployeeWorkingHoursTo = ?" +
                    " WHERE EmployeeUsername = ?;", updatedProjectEmployee.getEmployeeUsername(), updatedProjectEmployee.getEmployeeName(),
                                                   updatedProjectEmployee.getEmployeeEmail(), updatedProjectEmployee.getEmployeePassword(),
                                                   updatedProjectEmployee.getWorkingHoursFrom(), updatedProjectEmployee.getWorkingHoursTo(), targetUsername); // rowsAffected
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("A different employee with username " + updatedProjectEmployee.getEmployeeUsername() + " already exists.");
        }
    }

    public int deleteEmployeeByUsername(String targetUsername) {
        getEmployeeByUsername(targetUsername);

        return jdbcTemplate.update("DELETE FROM ProjectEmployees WHERE EmployeeUsername = ?;", targetUsername); // rowsAffected
    }

    public boolean login(String username, String pw) {
        ProjectEmployee employee = getEmployeeByUsername(username);
        // Why no username check? Because the above would throw if no such employee existed.
        return employee.getEmployeePassword().equals(pw);
    }

    public List<ProjectEmployee> getAllWorkers() {
        return jdbcTemplate.query("SELECT pe.* FROM ProjectEmployees pe JOIN Permissions p ON p.PermissionHolder = pe.EmployeeUsername WHERE PermissionTitle = 'Worker';", projectEmployeeRowMapper);
    }
}
