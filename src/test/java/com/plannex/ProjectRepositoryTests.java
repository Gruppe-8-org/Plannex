package com.plannex;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.Repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = { "classpath:schemah2.sql", "classpath:datah2.sql" }, executionPhase=BEFORE_TEST_METHOD)
public class ProjectRepositoryTests {
    @Autowired
    private ProjectRepository projectRepository;

    @Test
    public void addProjectAddsAProject() {
        Project newProject = new Project("Write integration tests", "Write integration tests for all repository classes. Aim for 100% code coverage.", LocalDate.of(2025, 11, 25), LocalDate.of(2025, 11, 26));
        int rowsAffected = projectRepository.addProject(newProject);
        assertEquals(1, rowsAffected);
        assertNotNull(projectRepository.getProjectByID(5)); // Four projects exist by default in the test DB.
    }

    @Test
    public void getProjectByIDGetsAProjectIfItExists() {
        Project targetProject = new Project("The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        Project projectFromDB = projectRepository.getProjectByID(1);
        assertNotNull(projectFromDB);
        assertEquals(targetProject, projectFromDB);
    }

    @Test
    public void getProjectByIDReturnsNullOnNonExistentProjectID() {
        assertNull(projectRepository.getProjectByID(-1));
    }

    @Test
    public void getAllProjectsGetsAllProjects() {
        List<Project> expectedProjects = List.of(
                new Project("The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17)),
                new Project("Coffee machine repairs on the second floor", "The coffee machine has been broken for a grueling three days now.\nCalls to the repairman revealed that we could do this ourselves to save money.\nShould the machine remain in disrepair, none of our projects will be released on schedule.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 13)),
                new Project("Secret Santa but in the Danish way", "Christmas is around the corner and it is an office tradition.\nHere, employees sign up to torment others and to being tormented by others.\nWarning: Extreme pranks (razor blades hidden in otherwise delicious fudge, irritants placed on toilet paper, ordering colleagues to write valid tar commands without access to the manual pages, etc.) will subject you to disciplinary action.", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20)),
                new Project("Calculator SaaS", "Our customers desparately want a calculator stored in the cloud, this is our answer to their prayers.\nIn essence, it is an expression lexer, parser and evaluator. It is to support:\n* Parenthesized expressions\n* User-defined and built in functions\n* Testable components", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8))
        );
        List<Project> projectList = projectRepository.getAllProjects();
        assertEquals(expectedProjects, projectList);
    }

    @Test
    public void updateProjectUpdatesProjectIfExistsAndOnlyDesiredFields() {
        Project modifiedProject = new Project("The PlanProject", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 12, 12), LocalDate.of(2025, 12, 18));
        Project projectFromDBBefore = projectRepository.getProjectByID(1);
        assertNotNull(projectFromDBBefore);
        int rowsUpdated = projectRepository.updateProject(modifiedProject, 1);
        assertEquals(1, rowsUpdated);
        Project projectFromDBAfter = projectRepository.getProjectByID(1);
        assertNotEquals(projectFromDBBefore, projectFromDBAfter);
        assertEquals(modifiedProject, projectFromDBAfter);
    }

    @Test
    public void updateProjectThrowsOnNonExistentProject() {
        Project modifiedProject = new Project("The PlanProject", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 12, 12), LocalDate.of(2025, 12, 18));
        String expectedMessage = "No project with projectID 5 exists.";

        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectRepository.updateProject(modifiedProject, 5),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void deleteProjectByIDDeletesProjectIfExists() {
        int rowsDeleted = projectRepository.deleteProjectByID(1);
        assertTrue(rowsDeleted >= 1);
        assertNull(projectRepository.getProjectByID(1));
        assertEquals(3, projectRepository.getAllProjects().size());
    }

    @Test
    public void deleteProjectByIDThrowsOnNonExistentProject() {
        String expectedMessage = "No project with projectID 5 exists.";
        Exception thrown = assertThrows(EntityDoesNotExistException.class,
                () -> projectRepository.deleteProjectByID(5),
                expectedMessage);
        assertEquals(expectedMessage, thrown.getMessage());
    }
}
