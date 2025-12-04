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
            throw new EntityAlreadyExistsException("A different employee with username " + updatedProjectEmployee.getEmployeeUsername() + " already exists.");
        }
    }

    public int deleteEmployeeByUsername(String targetUsername) {
        if (getEmployeeByUsername(targetUsername) == null) {
            throw new EntityDoesNotExistException("No employee with username " + targetUsername + " exists.");
        }

        return jdbcTemplate.update("DELETE FROM ProjectEmployees WHERE EmployeeUsername = ?;", targetUsername); // rowsAffected
    }

    public boolean login(String username, String pw) {
        ProjectEmployee employee = getEmployeeByUsername(username);
        return employee.getEmployeeUsername().equals(username) && employee.getEmployeePassword().equals(pw);
    }
}
