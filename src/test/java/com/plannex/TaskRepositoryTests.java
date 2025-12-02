package com.plannex;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Task;
import com.plannex.Repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = { "classpath:schemah2.sql", "classpath:datah2.sql" }, executionPhase=BEFORE_TEST_METHOD)
public class TaskRepositoryTests {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final AssertThrowsHelper assertThrowsHelper = new AssertThrowsHelper();

    @Test // I know we're supposed to wrap Java exceptions, but this one fits too well.
    public void addTaskAddsTaskIfProjectExists() throws OperationNotSupportedException {
        Task newTask = new Task(1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        int rowsAffected = taskRepository.addTask(newTask);
        assertEquals(1, rowsAffected);
        Task newTaskFromDB = taskRepository.getTaskByID(17); // 16 exist by default in test DB
        assertEquals(newTask, newTaskFromDB);
    }

    @Test
    public void addTaskThrowsOnInvalidProjectID() {
        Task newTask = new Task(-1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addTask(newTask));
    }

    @Test
    public void addTaskThrowsOnTryingToAddSubtask() {
        Task newTask = new Task(1, 2, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may not use addTask for adding subtasks.", OperationNotSupportedException.class, () -> taskRepository.addTask(newTask));
    }

    @Test
    public void addSubtaskAddsSubtaskGivenValidValues() throws OperationNotSupportedException {
        Task newTask = new Task(1, 1, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        int rowsAdded = taskRepository.addSubtask(newTask);
        assertEquals(1, rowsAdded);
        Task newTaskFromDB = taskRepository.getTaskByID(17);
        assertEquals(newTask, newTaskFromDB);
    }

    @Test
    public void addSubtaskThrowsOnInvalidProjectID() {
        Task newTask = new Task(-1, 1, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addSubtaskThrowsOnInvalidParentTask() {
        Task newTask = new Task(1, -1, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addSubtaskThrowsOnAddingSubtaskToAnotherSubtask() {
        Task newTask = new Task(1, 2, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("Only tasks can have subtasks, not subtasks.", OperationNotSupportedException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addSubtaskThrowsOnTryingToAddTaskThroughAddSubtask() {
        Task newTask = new Task(1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may not add tasks with addSubtask.", OperationNotSupportedException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addFollowDependencyAddsDependencyWhenGivenValidTaskIDs() throws OperationNotSupportedException {
        int rowsAdded = taskRepository.addFollowsDependency(16, 13);
        assertEquals(1, rowsAdded);
        assertTrue(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TaskDependencies WHERE TaskIDFor = ? AND MustComeAfterTaskWithID = ?;", Boolean.class, 16, 13));
    }

    @Test
    public void addFollowDependencyThrowsOnNonExistentDependentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addFollowsDependency(-1, 2));
    }

    @Test
    public void addFollowDependencyThrowsOnNonExistentBlockingTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addFollowsDependency(4, -1));
    }

    @Test
    public void addFollowDependencyThrowsOnDuplicateDependencyAttempt() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The task with ID 5 is already marked as blocked by the task with ID 4.", EntityAlreadyExistsException.class, () -> taskRepository.addFollowsDependency(5, 4));
    }

    @Test
    public void addFollowsDependencyThrowsOnTryingToSetTaskAsSelfBlocking() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may not set a task as blocking itself.", OperationNotSupportedException.class, () -> taskRepository.addFollowsDependency(5, 5));
    }

    @Test
    public void deleteFollowsDependencyWorksOnExistingDependency() {
        int rowsAffected = taskRepository.deleteFollowsDependency(5, 4);
        assertEquals(1, rowsAffected);
        assertEquals(0, taskRepository.getAllDependenciesForTask(5).size());
    }

    @Test
    public void deleteFollowsDependencyThrowsOnNonExistentDependentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID 17 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteFollowsDependency(17, 2));
    }

    @Test
    void deleteFollowsDependencyThrowsOnNonExistentBlockingTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteFollowsDependency(6, -1));
    }

    @Test
    void deleteFollowsDependencyThrowsOnNonExistentDependency() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The task with ID 6 is not marked as blocked by the task with ID 4.", EntityDoesNotExistException.class, () -> taskRepository.deleteFollowsDependency(6, 4));
    }

    @Test
    public void assignEmployeeWorksGivenValidTaskIDAndEmployeeID() {
        int rowsAdded = taskRepository.assignTaskToEmployee(16, "marqs");
        assertEquals(1, rowsAdded);
        assertTrue(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TaskAssignees WHERE EmployeeUsername = ? AND TaskID = ?;", Boolean.class, "marqs", 16));
    }

    @Test
    public void assignEmployeeThrowsOnNonExistentTaskID() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.assignTaskToEmployee(-1, "marqs"));
    }

    @Test
    public void assignEmployeeThrowsOnNonExistentUsername() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username marqois exists.", EntityDoesNotExistException.class, () -> taskRepository.assignTaskToEmployee(16, "marqois"));
    }

    @Test
    public void assignEmployeeThrowsOnDuplicateAttempt() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The employee with username lildawg is already assigned the task with ID 16.", EntityAlreadyExistsException.class, () -> taskRepository.assignTaskToEmployee(16, "lildawg"));
    }

    @Test
    public void unassignEmployeeWorksOnExistingTaskAndEmployeeAndAssignment() {
        int rowsDeleted = taskRepository.unassignTaskFromEmployee(16, "lildawg");
        assertEquals(1, rowsDeleted);
        assertFalse(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TaskAssignees WHERE EmployeeUsername = ? AND TaskID = ?;", Boolean.class, "lildawg", 16));
    }

    @Test
    public void unassignEmployeeThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.unassignTaskFromEmployee(-1, "marqs"));
    }

    @Test
    public void unassignEmployeeThrowsOnNonExistentUsername() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No employee with username marqois exists.", EntityDoesNotExistException.class, () -> taskRepository.unassignTaskFromEmployee(2, "marqois"));
    }

    @Test
    public void unassignEmployeeThrowsWhenTryingToDeleteNonExistentAssignment() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The employee with username marqs is not assigned the task with ID 16.", EntityDoesNotExistException.class, () -> taskRepository.unassignTaskFromEmployee(16, "marqs"));
    }

    @Test
    public void getTaskByIDGetsTaskIfItExists() {
        Task expectedTask = new Task(1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        Task actualTask = taskRepository.getTaskByID(2);
        assertNotNull(actualTask);
        assertEquals(expectedTask, actualTask);
    }

    @Test
    public void getTaskByIDReturnsNullOnNonExistentTask() {
        assertNull(taskRepository.getTaskByID(-1));
    }

    @Test
    public void getAllTasksForProjectWithIDReturnsAllTasksFromAProject() {
        assertEquals(2, taskRepository.getAllTasksForProject(1).size());
    }

    @Test
    public void getAllTasksForProjectWithIDReturnsEmptyListIfNoTasks() {
        assertEquals(0, taskRepository.getAllTasksForProject(2).size());
    }

    @Test
    public void getAllTasksForProjectWithIDThrowsOnNonExistentProject() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllTasksForProject(-1));

    }

    @Test
    public void getAllSubtasksForParentTaskWorksOnExistingProjectWithTasks() throws OperationNotSupportedException {
        assertEquals(6, taskRepository.getAllSubtasksForParentTask(1).size());
    }

    @Test
    public void getAllSubtasksForParentTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllSubtasksForParentTask(-1));
    }

    @Test
    public void getAllSubtasksForParentTaskThrowsOnTryingToUseASubtaskAsParent() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("A subtask must have no subtasks.", OperationNotSupportedException.class, () -> taskRepository.getAllSubtasksForParentTask(2));
    }

    @Test
    public void getAllArtifactsForTaskReturnsAllArtifactsWhenTaskIsValidAndHasThem() {
        List<TaskRepository.ConstPair<String, String>> expectedArtifacts = List.of(
                new TaskRepository.ConstPair<String, String>("marqs", "github.com/Gruppe-8-org/plannex")
        );

        assertEquals(expectedArtifacts, taskRepository.getAllArtifactsForTask(2));

        expectedArtifacts = List.of(
                new TaskRepository.ConstPair<>("lildawg", "src/main/plannex/Repository/ProjectRepository.java"),
                new TaskRepository.ConstPair<>("lildawg", "src/main/plannex/Repository/TaskRepository.java"),
                new TaskRepository.ConstPair<>("lildawg", "src/test/plannex/Repository/ProjectEmployeeRepository.java")
        );

        assertEquals(expectedArtifacts, taskRepository.getAllArtifactsForTask(15));
    }

    @Test
    public void getAllArtifactsForTaskReturnsNoArtifactsWhenTaskIsValidButHasNoArtifacts() {
        assertEquals(0, taskRepository.getAllArtifactsForTask(16).size());
    }

    @Test
    public void getAllArtifactsForTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllArtifactsForTask(-1));
    }

    @Test
    public void getAllDependenciesForTaskGetsListOfDependenciesForExistingTaskWithDependencies() {
        List<TaskRepository.ConstPair<Integer, Integer>> expectedDependencies = List.of(
                new TaskRepository.ConstPair<>(5, 4)
        );
        assertEquals(expectedDependencies, taskRepository.getAllDependenciesForTask(5));
    }

    @Test
    public void getAllDependenciesForTaskGetsListOfDependenciesForExistingTaskWithoutDependencies() {
        assertEquals(0, taskRepository.getAllDependenciesForTask(3).size());
    }

    @Test
    public void getAllDependenciesForTaskThrowsOnNonExistentTaskID() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllDependenciesForTask(-1));
    }

    @Test
    public void getAllTimeContributionsForTaskWorksForExistingTaskWithTimeSpent() {
        List<Integer> expectedContributions = List.of(4, 4);
        List<Integer> actualContributions = taskRepository.getAllTimeContributionsForTask(16);
        assertEquals(expectedContributions, actualContributions);
    }

    @Test
    public void getAllTimeContributionsForTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllTimeContributionsForTask(-1));
    }

    @Test
    public void updateTaskUpdatesOnlyWantedFieldsIfTaskExists() {
        Task expectedTask = new Task(1, 0, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 13), LocalDate.of(2025, 11, 14), 2.0f);
        Task taskFromDBBefore = taskRepository.getTaskByID(1);
        assertEquals(1, taskRepository.updateTask(expectedTask, 1));
        Task taskFromDBAfter = taskRepository.getTaskByID(1);
        assertNotEquals(taskFromDBAfter, taskFromDBBefore);
        assertEquals(expectedTask, taskFromDBAfter);
    }

    @Test
    public void updateTaskThrowsOnNonExistentTask() {
        Task newTask = new Task(1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.updateTask(newTask, -1));
    }

    @Test
    public void deleteTaskDeletesOnExistingTask() {
        int rowsDeleted = taskRepository.deleteTaskByID(1);
        assertEquals(1, rowsDeleted); // Cascades don't count
        assertNull(taskRepository.getTaskByID(1));
    }

    @Test
    public void deleteTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteTaskByID(-1));
    }

    @Test
    public void deleteTaskCascadesCorrectly() {
        int rowsDeleted = taskRepository.deleteTaskByID(1);
        assertEquals(1, rowsDeleted); // Cascading doesn't count
        assertNull(taskRepository.getTaskByID(1));
        assertNull(taskRepository.getTaskByID(2));
        assertNull(taskRepository.getTaskByID(3));
        assertNull(taskRepository.getTaskByID(4));
        assertNull(taskRepository.getTaskByID(5));
        assertNull(taskRepository.getTaskByID(6));
        assertNull(taskRepository.getTaskByID(7));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllDependenciesForTask(5));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllDependenciesForTask(6));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllDependenciesForTask(7));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllTimeContributionsForTask(5));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllTimeContributionsForTask(6));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllTimeContributionsForTask(7));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllArtifactsForTask(2));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllArtifactsForTask(2));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllArtifactsForTask(2));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllArtifactsForTask(2));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getAllArtifactsForTask(2));
    }
}
