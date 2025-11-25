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
        String expectedExceptionMessage = "An employee with username lildawg already exists.";
        Exception ex = assertThrows(EntityAlreadyExistsException.class,
                () -> projectEmployeeRepository.addEmployee(MES, "Manager"),
                expectedExceptionMessage);
        assertEquals(expectedExceptionMessage, ex.getMessage());
    }

    @Test
    public void addEmployeeThrowsIfEmployeeDoesNotExistButHasPermissions() {

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
    public void getAllAssigneesGetsAllAsigneesIfSubtaskHasThem() {
        List<ProjectEmployee> expectedAssigneeList = List.of(new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)));
        List<ProjectEmployee> actualAssigneeList = projectEmployeeRepository.getAllAssigneesForSubtask(10);
        assertEquals(expectedAssigneeList, actualAssigneeList);
    }

    @Test // Only unique assignees, try the query in MySQL workbench without DISTINCT and see difference
    public void getAllAssigneesGetsAllAssignessFromAllSubtasksIfUsedOnTaskWithSubtasks() {
        List<ProjectEmployee> expectedAssignees = List.of(
                new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("bigdawg", "Max", "MRK@gmail.com", "0uFF!nÆr", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("marqs", "Markus", "MBR@gmail.com", "HhQEsN4t", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0))
        );

        List<ProjectEmployee> actualAssignees = projectEmployeeRepository.getAllAssigneesForTask(1);
        assertTrue(expectedAssignees.size() == actualAssignees.size() && expectedAssignees.containsAll(actualAssignees)); // Order differs in DB
    }

    @Test
    public void getAllAssigneesReturnsNullOnTaskWithNoAssigneesOrIfTaskDoesNotExist() {
        assertEquals(0, projectEmployeeRepository.getAllAssigneesForSubtask(-1).size()); // Doesn't exist (yet)
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

        // Permissions, assignees, artifacts, time spent should be updated
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(PermissionHolder) > 0 FROM Permissions WHERE PermissionHolder = 'lild4wg'", Boolean.class));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(EmployeeUsername) > 0 FROM TaskAssignees WHERE EmployeeUsername = 'lild4wg'", Boolean.class));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(ByEmployee) > 0 FROM TimeSpent WHERE ByEmployee = 'lild4wg'", Boolean.class));
        assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject("SELECT COUNT(ArtifactAuthor) > 0 FROM Artifacts WHERE ArtifactAuthor = 'lild4wg'", Boolean.class));
    }

    @Test
    public void updateEmployeeThrowsOnNonExistentEmployee() {
        ProjectEmployee newMES = new ProjectEmployee("lildowg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(10, 0, 0));
        String expectedMessage = "No employee with username lildowg exists.";

        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateEmployee(newMES, "lildowg"),
                expectedMessage
        );

        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateEmployeeThrowsOnUsernameCollisionAfterUpdate() {
        ProjectEmployee newMES = new ProjectEmployee("marqs", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(10, 0, 0));
        ProjectEmployee MESBefore = projectEmployeeRepository.getEmployeeByUsername("marqs");
        assertNotNull(MESBefore);
        String expectedMessage = "A different user with username marqs already exists.";

        Exception thrown = assertThrows(EntityAlreadyExistsException.class,
                () -> projectEmployeeRepository.updateEmployee(newMES, "lildawg"),
                expectedMessage
        );

        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void addArtifactAddsAnArtifactProvidedItDoesNotAlreadyExist() {
        int rowsAffected = projectEmployeeRepository.addArtifact(13, "lildawg", "resources/testArtifact.a");
        assertEquals(1, rowsAffected);
        Object[] artifactValues = jdbcTemplate.queryForObject("SELECT * FROM Artifacts WHERE TaskID = ? AND ArtifactAuthor = ? AND PathToArtifact = ?", (rs, rowNum) -> new Object[] {rs.getInt("TaskID"), rs.getString("ArtifactAuthor"), rs.getString("PathToArtifact")}, 13, "lildawg", "resources/testArtifact.a");
        assert artifactValues != null;
        assertEquals(13, artifactValues[0]);
        assertEquals("lildawg", artifactValues[1]);
        assertEquals("resources/testArtifact.a", artifactValues[2]);
    }

    @Test
    public void addArtifactThrowsIfNonExistentEmployeeUsernameProvided() {
        String expectedMessage = "No employee with username lildowg exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.addArtifact(13, "lildowg", "resources/testArtifact.a"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void addArtifactThrowsOnNonExistentTask() {
        String expectedMessage = "No task with ID -1 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.addArtifact(-1, "lildawg", "resources/testArtifact.a"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void addArtifactThrowsIfArtifactAlreadyExistsBySameEmployeeOnSameTask() {
        // The following does not mean that different users can't upload the same artifact to the same task!
        String expectedMessage = "Employee lildawg has already uploaded this artifact to this task. Update it if you want to change it.";
        Exception thrown = assertThrows(EntityAlreadyExistsException.class,
                () -> projectEmployeeRepository.addArtifact(5, "lildawg", "docs/domain_model_plannex.png"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateArtifactWorksOnNewPathWhichDoesNotAlreadyExist() {
        int rowsAffected = projectEmployeeRepository.updateArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex2");
        assertEquals(1, rowsAffected);
        assertNotNull(jdbcTemplate.queryForObject("SELECT PathToArtifact FROM Artifacts WHERE ArtifactAuthor = ? AND TaskID = ? AND PathToArtifact = ?;",
                String.class, "marqs", 2, "github.com/Gruppe-8-org/plannex2"));
    }

    @Test
    public void updateArtifactThrowsOnNonExistentEmployee() {
        String expectedMessage = "No employee with username marqois exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateArtifact(2, "marqois", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateArtifactThrowsOnNonExistentTask() {
        String expectedMessage = "The artifact with path github.com/Gruppe-8-org/plannex does not exist, uploaded by marqs for task with ID 100000.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateArtifact(100000, "marqs", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateArtifactThrowsOnNonExistentArtifact() {
        String expectedMessage = "The artifact with path github.com/Gruppe-8-org/plannex3 does not exist, uploaded by marqs for task with ID 2.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex3", "github.com/Gruppe-8-org/plannex4"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateArtifactThrowsIfNewPathAlreadyExists() {
        int rowsAdded = projectEmployeeRepository.addArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex2");
        assertEquals(1, rowsAdded);

        String expectedMessage = "The artifact with path github.com/Gruppe-8-org/plannex2 already exists. Change its name or delete and replace it.";
        Exception thrown = assertThrows(EntityAlreadyExistsException.class,
                () -> projectEmployeeRepository.updateArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex2"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteArtifactDeletesExistingArtifact() {
        int rowsDeleted = projectEmployeeRepository.deleteArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex");
        assertEquals(1, rowsDeleted);

        String expectedMessage = "The artifact with path github.com/Gruppe-8-org/plannex does not exist, uploaded by marqs for task with ID 2.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteArtifactThrowsOnNonExistentEmployee() {
        String expectedMessage = "No employee with username marqois exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteArtifact(2, "marqois", "github.com/Gruppe-8-org/plannex"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteArtifactThrowsOnNonExistentTask() {
        String expectedMessage = "The artifact with path github.com/Gruppe-8-org/plannex does not exist, uploaded by marqs for task with ID -1.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteArtifact(-1, "marqs", "github.com/Gruppe-8-org/plannex"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteArtifactThrowsOnNonExistentArtifact() {
        String expectedMessage = "The artifact with path github.com/Gruppe-8-org/plannex2 does not exist, uploaded by marqs for task with ID 2.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex2"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void contributeTimeAddsTimeSpent() {
        int rowsAffected = projectEmployeeRepository.contributeTime("marqs", 1, 2.0f);
        assertEquals(1, rowsAffected);
        assertNotNull(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TimeSpent WHERE OnTaskID = ? AND ByEmployee = ? AND HoursSpent = ?", Boolean.class, 1, "marqs", 2));
    }

    @Test
    public void contributeTimeThrowsOnNonExistentEmployee() {
        String expectedMessage = "No employee with username marqois exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.contributeTime("marqois", 17, 2.0f),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void contributeTimeThrowsOnNonExistentTask() {
        String expectedMessage = "No task with ID 100000 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.contributeTime("marqs", 100000, 2.0f),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void contributeTimeThrowsOnNegativeTimeSpent() {
        String expectedMessage = "Hours spent should be zero or more.";
        Exception thrown = assertThrows(InvalidValueException.class,
                () -> projectEmployeeRepository.contributeTime("marqs", 17, -2.0f),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateTimeContributionUpdatesWithValidValues() {
        int rowsAffected = projectEmployeeRepository.updateTimeContribution("marqs", 2, 3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0)));
        assertEquals(1, rowsAffected);
        assertEquals(3.0f, jdbcTemplate.queryForObject("SELECT HoursSpent FROM TimeSpent WHERE ByEmployee = ? AND OnTaskID = ? AND _When = ?;",
                float.class, "marqs", 2, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))));
    }

    @Test
    public void updateTimeContributionThrowsOnNonExistentEmployee() {
        String expectedMessage = "No employee with username marqois exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateTimeContribution("marqois", 2, 0.5f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateTimeContributionThrowsOnNegativeTimeSpent() {
        String expectedMessage = "Hours spent should be zero or more.";
        Exception thrown = assertThrows(InvalidValueException.class,
                () -> projectEmployeeRepository.updateTimeContribution("marqs", 2, -3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateTimeContributionThrowsOnNonExistentContribution() {
        String expectedMessage = "No time contribution by marqs on task with ID 2 at 2025-11-12T12:00 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateTimeContribution("marqs", 2, 3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(12, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void updateTimeContributionThrowsOnNonExistentTask() {
        String expectedMessage = "No time contribution by marqs on task with ID -1 at 2025-11-12T10:00 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.updateTimeContribution("marqs", -1, 3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteTimeContributionDeletesGivenValidValues() {
        int rowsAffected = projectEmployeeRepository.deleteTimeContribution("marqs", 2, 0.5f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0)));
        assertEquals(1, rowsAffected);
        assertThrows(EmptyResultDataAccessException.class, () -> jdbcTemplate.queryForObject("SELECT HoursSpent FROM TimeSpent WHERE ByEmployee = ? AND OnTaskID = ? AND _When = ?;",
                float.class, "marqs", 2, LocalTime.of(10, 0, 0)));
    }

    @Test
    public void deleteTimeContributionThrowsOnNonExistentEmployee() {
        String expectedMessage = "No employee with username marqois exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteTimeContribution("marqois", 2, 0.5f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteTimeContributionThrowsOnNonExistentTimeContribution() {
        String expectedMessage = "No time contribution by marqs on task with ID 2 at 2025-11-12T14:00 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteTimeContribution("marqs", 2, 0.5f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(14, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteTimeContributionThrowsOnNonExistentTask() {
        String expectedMessage = "No time contribution by marqs on task with ID -1 at 2025-11-12T10:00 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteTimeContribution("marqs", -1, 0.5f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteEmployeeByUsernameWorksOnExistingEmployee() {
        int rowsAffected = projectEmployeeRepository.deleteEmployeeByUsername("marqs");
        assertTrue(rowsAffected >= 1); // Unsure if cascading DELETES count
        assertNull(projectEmployeeRepository.getEmployeeByUsername("marqs"));
    }

    @Test
    public void deleteEmployeeByUsernameThrowsOnNonExistentUsername() {
        String expectedMessage = "No user with username marqois exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectEmployeeRepository.deleteEmployeeByUsername("marqois"),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
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
