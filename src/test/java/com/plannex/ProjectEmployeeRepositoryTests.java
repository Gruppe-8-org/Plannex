package com.plannex;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.InvalidValueException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Repository.ProjectEmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        assertNull(projectEmployeeRepository.getEmployeeByUsername("jh5024"));
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
        assertNull(projectEmployeeRepository.getEmployeeByUsername("marqs"));
    }

    @Test
    public void deleteEmployeeByUsernameThrowsOnNonExistentUsername() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username marqois exists.", EntityDoesNotExistException.class, () -> projectEmployeeRepository.deleteEmployeeByUsername("marqois"));
    }

    @Test
    public void deleteEmployeeByUsernameCascadesCorrectly() {
        projectEmployeeRepository.deleteEmployeeByUsername("marqs");
        assertNull(projectEmployeeRepository.getEmployeePermissions("marqs"));
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TaskAssignees WHERE EmployeeUsername = ?", boolean.class, "marqs"));
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TimeSpent WHERE ByEmployee = ?", boolean.class, "marqs"));
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM Artifacts WHERE ArtifactAuthor = ?", boolean.class, "marqs"));
    }
}
