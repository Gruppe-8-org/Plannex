package com.plannex.Repository;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.EmployeeSkill;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Skill;
import com.plannex.RowMapper.EmployeeSkillRowMapper;
import com.plannex.RowMapper.ProjectEmployeeRowMapper;
import com.plannex.RowMapper.SkillRowMapper;
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
    protected final EmployeeSkillRowMapper employeeSkillRowMapper;
    protected final SkillRowMapper skillRowMapper;

    public ProjectEmployeeRepository(JdbcTemplate jdbcTemplate, ProjectEmployeeRowMapper projectEmployeeRowMapper, EmployeeSkillRowMapper employeeSkillRowMapper, SkillRowMapper skillRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectEmployeeRowMapper = projectEmployeeRowMapper;
        this.employeeSkillRowMapper = employeeSkillRowMapper;
        this.skillRowMapper = skillRowMapper;

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

    public List<Skill> getAllSkills() {
        return jdbcTemplate.query("SELECT * FROM Skills", skillRowMapper);
    }


    public List<EmployeeSkill> getSkillsForEmployee(String username) {
        return jdbcTemplate.query(
                "SELECT es.EmployeeUsername AS EmployeeUsername, " +
                        "       s.SkillTitle AS SkillTitle, " +
                        "       es.SkillLevel AS SkillLevel " +
                        "FROM EmployeeSkills es " +
                        "JOIN Skills s ON es.SkillTitle = s.SkillTitle " +
                        "WHERE es.EmployeeUsername = ?",
                employeeSkillRowMapper,
                username
        );
    }

    public Skill getSkillByTitle(String skillTitle) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Skills WHERE SkillTitle = ?;", skillRowMapper, skillTitle);
        } catch (EmptyResultDataAccessException erdae) {
            throw new EntityDoesNotExistException("No skill with title " + skillTitle + " exists.");
        }
    }

    public boolean skillWithTitleExists(String skillTitle) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM Skills WHERE SkillTitle = ?;", boolean.class, skillTitle));
    }

    public int assignSkillToEmployee(String skillTitle, String employeeUsername, String skillLevel) {
        getSkillByTitle(skillTitle); // or throw...

        try {
            return jdbcTemplate.update("INSERT INTO EmployeeSkills (EmployeeUsername, SkillTitle, SkillLevel) VALUES (?, ?, ?);",
                    employeeUsername, skillTitle, skillLevel);
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("The employee with username " + employeeUsername + " is already assigned the skill with title " + skillTitle + ".");
        }
    }

    public int unassignSkillFromEmployee(String skillTitle, String employeeUsername, String skillLevel) {
        getSkillByTitle(skillTitle); // or throw...

        int rowsDeleted = jdbcTemplate.update("DELETE FROM EmployeeSkills WHERE EmployeeUsername = ? AND SkillTitle = ? AND SkillLevel = ?;",
                employeeUsername, skillTitle, skillLevel);

        if (rowsDeleted != 1) throw new EntityDoesNotExistException("The employee with username " + employeeUsername + " is not assigned the skill with title " + skillTitle + ".");
        return rowsDeleted;
    }


    public int addSkillUnlessItAlreadyExists(String skillTitle) {
        if (skillWithTitleExists(skillTitle)) {
            return 0;
        }

        return jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES (?);", skillTitle);
    }

    public int removeSkillIfExists(String skillTitle) {
        if (!skillWithTitleExists(skillTitle)) {
            return 0;
        }

        return jdbcTemplate.update("DELETE FROM Skills WHERE SkillTitle = ?;", skillTitle);
    }


    public int countExpertSkills(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM EmployeeSkills WHERE EmployeeUsername=? AND SkillLevel='Expert'",
                Integer.class,
                username
        );
    }

    public int countIntermediateSkills(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM EmployeeSkills WHERE EmployeeUsername=? AND SkillLevel='Intermediate'",
                Integer.class,
                username
        );
    }
}
