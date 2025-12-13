package com.plannex.Repository;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.NotSupportedException;
import com.plannex.Model.EmployeeSkill;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Skill;
import com.plannex.Model.Task;
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
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
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
            throw new EntityDoesNotExistException("No permissions registered for user with username " + username + ".");
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

    public List<Skill> getAllSkills() {
        try {
            return jdbcTemplate.query("SELECT FROM * Skills", skillRowMapper);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public Skill getSkillFromAllSkills(List<Skill> allSkills, String chosenSkill) {
        for (Skill s: allSkills) {
            if (s.getSkillTitle().equals(chosenSkill)) {
                return s;
            }
        }
        return null;
    }

    public List<EmployeeSkill> getSkillsForEmployee(String username) {
        return jdbcTemplate.query(
                "SELECT es.EMPLOYEEUSERNAME AS EmployeeUsername, " +
                        "       s.SKILLTITLE AS SkillTitle, " +
                        "       es.SKILLLEVEL AS SkillLevel, " +
                        "       s.SKILLID AS SkillID " +
                        "FROM EMPLOYEESKILLS es " +
                        "JOIN SKILLS s ON es.SKILLID = s.SKILLID " +
                        "WHERE es.EMPLOYEEUSERNAME = ?",
                employeeSkillRowMapper,
                username
        );
    }

    public Skill getSkillByID(int skillID) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Skills WHERE SkillID = ?;", skillRowMapper, skillID);
        } catch (EmptyResultDataAccessException erdae) {
            throw new EntityDoesNotExistException("No task with ID " + skillID + " exists.");
        }
    }

    public int assignSkillToEmployee(int skillID, String employeeUsername, String skillLevel) {
        if (getSkillByID(skillID) == null) {
            throw new EntityDoesNotExistException("No skill with ID " + skillID + " exists.");
        }

        try {
            return jdbcTemplate.update("INSERT INTO EmployeeSkills (EmployeeUsername, SkillID, SkillLevel) VALUES (?, ?, ?);",
                    employeeUsername, skillID, skillLevel);
        } catch (DataIntegrityViolationException dive) {
            throw new EntityAlreadyExistsException("The employee with username " + employeeUsername + " is already assigned the skill with ID " + skillID + ".");
        }
    }

    public int unassignTaskFromEmployee(int skillID, String employeeUsername, String skillLevel) {
        if (getSkillByID(skillID) == null) {
            throw new EntityDoesNotExistException("No skill with ID " + skillID + " exists.");
        }

        int rowsDeleted = jdbcTemplate.update("DELETE FROM TaskAssignees WHERE EmployeeUsername = ? AND SkillID = ? AND SkillLevel = ?;",
                employeeUsername, skillID, skillLevel);

        if (rowsDeleted != 1) throw new EntityDoesNotExistException("The employee with username " + employeeUsername + " is not assigned the skill with ID " + skillID + ".");
        return rowsDeleted;
    }


    public int addSkill(String username, String skillTitle, String level) {

        Integer skillId = jdbcTemplate.queryForObject(
                "SELECT SkillID FROM Skills WHERE SkillTitle = ?",
                Integer.class,
                skillTitle
        );

        return jdbcTemplate.update(
                "INSERT INTO EmployeeSkills (EmployeeUsername, SkillID, SkillLevel) VALUES (?, ?, ?)",
                username, skillId, level
        );
    }

    public int removeSkill(String username, String skillTitle) {

        Integer skillId = jdbcTemplate.queryForObject(
                "SELECT SkillID FROM Skills WHERE SkillTitle = ?",
                Integer.class,
                skillTitle
        );

        return jdbcTemplate.update(
                "DELETE FROM EmployeeSkills WHERE EmployeeUsername = ? AND SkillID = ?",
                username, skillId
        );
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
