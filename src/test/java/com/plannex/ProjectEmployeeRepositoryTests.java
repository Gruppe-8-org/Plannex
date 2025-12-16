package com.plannex;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.EmployeeSkill;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Repository.ProjectEmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = { "classpath:schemah2.sql", "classpath:datah2.sql" }, executionPhase=BEFORE_TEST_METHOD)
public class ProjectEmployeeRepositoryTests {
    @Autowired
    private ProjectEmployeeRepository projectEmployeeRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;
    private final AssertThrowsHelper assertThrowsHelper = new AssertThrowsHelper();

    @Test
    public void addEmployeeInsertsEmployeeIfItDoesNotAlreadyExist() {
        ProjectEmployee nonExistentEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        int rowsAffected = projectEmployeeRepository.addEmployee(nonExistentEmployee, "Worker");
        assertEquals(2, rowsAffected); // 1 in the employee table, 1 in permissions.
        ProjectEmployee nonExistentFromDB = projectEmployeeRepository.getEmployeeByUsername("hj2450");
        assertNotNull(nonExistentFromDB);
        assertEquals(nonExistentEmployee, nonExistentFromDB);
        assertEquals("Worker", projectEmployeeRepository.getEmployeePermissions("hj2450"));
    }

    @Test
    public void addEmployeeThrowsIfEmployeeAlreadyExists() {
        ProjectEmployee MES = new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        assertNotNull(projectEmployeeRepository.getEmployeeByUsername("lildawg"));
        assertThrowsHelper.verifyExceptionThrownWithMessage("An employee with username lildawg already exists.", EntityAlreadyExistsException.class, () -> projectEmployeeRepository.addEmployee(MES, "Manager"));
    }

    @Test
    public void getEmployeeByUsernameReturnsEmployeeWhenExists() {
        ProjectEmployee MES = new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        assertEquals(MES, projectEmployeeRepository.getEmployeeByUsername("lildawg"));
    }

    @Test
    public void getEmployeeByUsernameReturnsNullWhenEmployeeDoesNotExist() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username jh5024 exists.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.getEmployeeByUsername("jh5024"));
    }

    @Test
    public void getAllEmployeesReturnsAllEmployees() {
        List<ProjectEmployee> expectedEmployeeList = List.of(
                new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("bigdawg", "Max", "MRK@gmail.com", "0uFF!nÆr", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("marqs", "Markus", "MBR@gmail.com", "HhQEsN4t", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("RandomWorker", "Random", "RW@gmail.com", "notSecure", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0))
        );

        List<ProjectEmployee> actualEmployeeList = projectEmployeeRepository.getAllEmployees();
        assertEquals(4, actualEmployeeList.size());
        assertEquals(expectedEmployeeList, actualEmployeeList);
    }

    @Test
    public void getAllEmployeesReturnsEmptyListIfNoEmployees() {
        jdbcTemplate.update("DELETE FROM ProjectEmployees;");
        assertEquals(0, projectEmployeeRepository.getAllEmployees().size());
    }

    @Test
    public void updateEmployeeUpdatesOnExistingEmployeeAndOnlyModifiedFields() {
        ProjectEmployee newMES = new ProjectEmployee("lild4wg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(10, 0, 0));
        ProjectEmployee MESBefore = projectEmployeeRepository.getEmployeeByUsername("lildawg");
        int rowsAffected = projectEmployeeRepository.updateEmployee(newMES, "lildawg");
        assertTrue(rowsAffected >= 1); // Unsure if cascading counts
        ProjectEmployee MESAfter = projectEmployeeRepository.getEmployeeByUsername("lild4wg");
        assertEquals(newMES, MESAfter);
        assertNotEquals(MESBefore.getEmployeeUsername(), MESAfter.getEmployeeUsername());
        assertNotEquals(MESBefore.getWorkingHoursTo(), MESAfter.getWorkingHoursTo());

        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(PermissionHolder) > 0 FROM Permissions WHERE PermissionHolder = 'lild4wg'", Boolean.class));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(EmployeeUsername) > 0 FROM TaskAssignees WHERE EmployeeUsername = 'lild4wg'", Boolean.class));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(ByEmployee) > 0 FROM TimeSpent WHERE ByEmployee = 'lild4wg'", Boolean.class));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(ArtifactAuthor) > 0 FROM Artifacts WHERE ArtifactAuthor = 'lild4wg'", Boolean.class));
    }

    @Test
    public void updateEmployeeThrowsOnNonExistentEmployee() {
        ProjectEmployee newMES = new ProjectEmployee("lildowg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(10, 0, 0));
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username lildowg exists.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.updateEmployee(newMES, "lildowg"));
    }

    @Test
    public void updateEmployeeThrowsOnUsernameCollisionAfterUpdate() {
        ProjectEmployee newMES = new ProjectEmployee("marqs", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(10, 0, 0));
        ProjectEmployee MBRBefore = projectEmployeeRepository.getEmployeeByUsername("marqs");
        assertNotNull(MBRBefore);
        assertThrowsHelper.verifyExceptionThrownWithMessage("A different employee with username marqs already exists.", EntityAlreadyExistsException.class, () -> projectEmployeeRepository.updateEmployee(newMES, "lildawg"));
    }

    @Test
    public void deleteEmployeeByUsernameWorksOnExistingEmployee() {
        int rowsAffected = projectEmployeeRepository.deleteEmployeeByUsername("marqs");
        assertTrue(rowsAffected >= 1); // Unsure if cascading DELETES count
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username marqs exists.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.getEmployeeByUsername("marqs"));
    }

    @Test
    public void deleteEmployeeByUsernameThrowsOnNonExistentUsername() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username marqois exists.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.deleteEmployeeByUsername("marqois"));
    }

    @Test
    public void deleteEmployeeByUsernameCascadesCorrectly() {
        projectEmployeeRepository.deleteEmployeeByUsername("marqs");
        assertThrowsHelper.verifyExceptionThrownWithMessage("No permissions registered for user with username marqs.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.getEmployeePermissions("marqs"));
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TaskAssignees WHERE EmployeeUsername = ?", boolean.class, "marqs"));
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TimeSpent WHERE ByEmployee = ?", boolean.class, "marqs"));
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM Artifacts WHERE ArtifactAuthor = ?", boolean.class, "marqs"));
    }
    @Test
    public void getSkillsForEmployeeReturnsCorrectSkills() {
        List<EmployeeSkill> skillsExpected = List.of(new EmployeeSkill("lildawg", "Java-Coder", "Expert"));
        List<EmployeeSkill> skills = projectEmployeeRepository.getSkillsForEmployee("lildawg");
        assertEquals(skillsExpected, skills);
    }

    @Test
    public void addSkillAddsSkillIfItDoesNotAlreadyExist() {
        int rows = projectEmployeeRepository.addSkillUnlessItAlreadyExists("NotJava");
        assertEquals(1, rows);
    }

    @Test
    public void addSkillDoesNotAddSkillIfItExists() {
        jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES ('Java')");
        int rows = projectEmployeeRepository.addSkillUnlessItAlreadyExists("Java");
        assertEquals(0, rows);
    }

    @Test
    public void removeSkillRemovesSkillIfItExists() {
        jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES ('Java')");
        int rows = projectEmployeeRepository.removeSkillIfExists("Java");
        assertEquals(1, rows);
    }

    @Test
    public void removeSkillDoesNotRemoveSkillIfItDoesNotExist() {
        assertEquals(0, projectEmployeeRepository.removeSkillIfExists("Java"));
    }

    @Test
    public void countExpertSkillsReturnsCorrectAmount() {

        jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES ('C#')");
        jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES ('Java')");

        jdbcTemplate.update("INSERT INTO EmployeeSkills (EmployeeUsername, SkillTitle, SkillLevel) VALUES ('marqs', ?, 'Expert')", "C#");
        jdbcTemplate.update("INSERT INTO EmployeeSkills (EmployeeUsername, SkillTitle, SkillLevel) VALUES ('marqs', ?, 'Expert')", "Java");

        int count = projectEmployeeRepository.countExpertSkills("marqs");
        assertEquals(2, count);
    }

    @Test
    public void countIntermediateSkillsReturnsCorrectAmount() {

        jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES ('CSS')");
        jdbcTemplate.update("INSERT INTO Skills (SkillTitle) VALUES ('HTML')");

        jdbcTemplate.update("INSERT INTO EmployeeSkills (EmployeeUsername, SkillTitle, SkillLevel) VALUES ('bigdawg', ?, 'Intermediate')", "CSS");
        jdbcTemplate.update("INSERT INTO EmployeeSkills (EmployeeUsername, SkillTitle, SkillLevel) VALUES ('bigdawg', ?, 'Intermediate')", "HTML");

        int count = projectEmployeeRepository.countIntermediateSkills("bigdawg");
        assertEquals(3, count); // One already exists (Business Degree)
    }
    @Test
    public void loginWorksOnMatchingCredentialsAndRefusesInvalidOnes() {
        assertTrue(projectEmployeeRepository.login("lildawg", "fAbc#21Y"));
        assertFalse(projectEmployeeRepository.login("lildawg", "wrongPassword"));
    }

    @Test
    public void loginWorksOnMatchingCredentialFailsWithWrongUsername() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username lildowg exists.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.login("lildowg", ""));
    }

    @Test
    public void getAllWorkersReturnsAllWorkers() {
        List<ProjectEmployee> expectedWorkers = List.of(new ProjectEmployee("RandomWorker", "Random", "RW@gmail.com", "notSecure", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)));
        List<ProjectEmployee> actualWorkers = projectEmployeeRepository.getAllWorkers();
        assertEquals(expectedWorkers, actualWorkers);
    }
}
