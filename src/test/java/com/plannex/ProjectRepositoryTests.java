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
    private final AssertThrowsHelper assertThrowsHelper = new AssertThrowsHelper();

    @Test
    public void addProjectAddsAProject() {
        Project newProject = new Project(5, "Write integration tests", "Write integration tests for all repository classes. Aim for 100% code coverage.", LocalDate.of(2025, 11, 25), LocalDate.of(2025, 11, 26));
        int rowsAffected = projectRepository.addProject(newProject);
        assertEquals(1, rowsAffected);
        assertNotNull(projectRepository.getProjectByIDOrThrow(5)); // Four projects exist by default in the test DB.
    }

    @Test
    public void getProjectByIDGetsAProjectIfItExists() {
        Project targetProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        Project projectFromDB = projectRepository.getProjectByIDOrThrow(1);
        assertNotNull(projectFromDB);
        assertEquals(targetProject, projectFromDB);
    }

    @Test
    public void getProjectByIDThrowsOnNonExistentProjectID() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> projectRepository.getProjectByIDOrThrow(-1));
    }

    @Test
    public void getAllProjectsGetsAllProjects() {
        List<Project> expectedProjects = List.of(
                new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17)),
                new Project(2, "Coffee machine repairs on the second floor", "The coffee machine has been broken for a grueling three days now.\nCalls to the repairman revealed that we could do this ourselves to save money.\nShould the machine remain in disrepair, none of our projects will be released on schedule.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 13)),
                new Project(3, "Secret Santa but in the Danish way", "Christmas is around the corner and it is an office tradition.\nHere, employees sign up to torment others and to being tormented by others.\nWarning: Extreme pranks (razor blades hidden in otherwise delicious fudge, irritants placed on toilet paper, ordering colleagues to write valid tar commands without access to the manual pages, etc.) will subject you to disciplinary action.", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20)),
                new Project(4, "Calculator SaaS", "Our customers desparately want a calculator stored in the cloud, this is our answer to their prayers.\nIn essence, it is an expression lexer, parser and evaluator. It is to support:\n* Parenthesized expressions\n* User-defined and built in functions\n* Testable components", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8))
        );
        List<Project> projectList = projectRepository.getAllProjects();
        assertEquals(expectedProjects, projectList);
    }

    @Test
    public void getAllTasksForProjectWithIDReturnsAllTasksFromAProject() {
        assertEquals(2, projectRepository.getAllTasksForProject(1).size());
    }

    @Test
    public void getAllTasksForProjectWithIDReturnsEmptyListIfNoTasks() {
        assertEquals(0, projectRepository.getAllTasksForProject(2).size());
    }

    @Test
    public void getAllTasksForProjectWithIDThrowsOnNonExistentProject() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> projectRepository.getAllTasksForProject(-1));

    }

    @Test
    public void updateProjectUpdatesProjectIfExistsAndOnlyDesiredFields() {
        Project modifiedProject = new Project(1, "The PlanProject", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 12, 12), LocalDate.of(2025, 12, 18));
        Project projectFromDBBefore = projectRepository.getProjectByIDOrThrow(1);
        assertNotNull(projectFromDBBefore);
        int rowsUpdated = projectRepository.updateProject(modifiedProject, 1);
        assertEquals(1, rowsUpdated);
        Project projectFromDBAfter = projectRepository.getProjectByIDOrThrow(1);
        assertNotEquals(projectFromDBBefore, projectFromDBAfter);
        assertEquals(modifiedProject, projectFromDBAfter);
    }

    @Test
    public void updateProjectThrowsOnNonExistentProject() {
        Project modifiedProject = new Project(1, "The PlanProject", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 12, 12), LocalDate.of(2025, 12, 18));
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> projectRepository.updateProject(modifiedProject, -1));

    }

    @Test
    public void deleteProjectByIDDeletesProjectIfExists() {
        int rowsDeleted = projectRepository.deleteProjectByID(1);
        assertTrue(rowsDeleted >= 1);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID 1 exists.", EntityDoesNotExistException.class, () -> projectRepository.deleteProjectByID(1));
        assertEquals(3, projectRepository.getAllProjects().size());
    }

    @Test
    public void deleteProjectByIDThrowsOnNonExistentProject() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> projectRepository.deleteProjectByID(-1));
    }

    @Test
    public void getAllInvolvedReturnsTheCountOfDistinctEmployeesAssignedToTasksOrSubtasks() {
        List<Integer> expectedCounts = List.of(3, 0, 0, 0); // See DB

        for (int i = 1; i <= expectedCounts.size(); i++) {
            assertEquals(expectedCounts.get(i - 1), projectRepository.getAllInvolved(i));
        }
    }

    @Test
    public void getAllInvolvedThrowsOnNonExistentProjectID() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> projectRepository.getAllInvolved(-1));

    }

    @Test
    public void getTotalTimeSpentReturnsTheSumOfTimeSpentsOnTasksAndSubtasksOfTheProject() {
        List<Float> expectedSums = List.of(40.3334f, 0.0f);

        for (int i = 1; i <= expectedSums.size(); i++) {
            assertEquals(expectedSums.get(i - 1), projectRepository.getTotalTimeSpent(i), 1e-6);
        }
    }

    @Test
    public void getTotalTimeSpentThrowsOnNonExistentProjectID() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> projectRepository.getTotalTimeSpent(-1));
    }
}
