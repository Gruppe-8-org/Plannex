package com.plannex;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.InvalidValueException;
import com.plannex.Exception.NotSupportedException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.Repository.TaskRepository;
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
public class TaskRepositoryTests {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final AssertThrowsHelper assertThrowsHelper = new AssertThrowsHelper();

    @Test
    public void addTaskAddsTaskIfProjectExists() {
        Task newTask = new Task(17, 1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        int rowsAffected = taskRepository.addTask(newTask);
        assertEquals(1, rowsAffected);
        Task newTaskFromDB = taskRepository.getTaskByIDOrThrow(17); // 16 exist by default in test DB
        assertEquals(newTask, newTaskFromDB);
    }

    @Test
    public void addTaskThrowsOnInvalidProjectID() {
        Task newTask = new Task(0, -1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addTask(newTask));
    }

    @Test
    public void addTaskThrowsOnTryingToAddSubtask() {
        Task newTask = new Task(0, 1, 2, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may not use addTask for adding subtasks.", NotSupportedException.class, () -> taskRepository.addTask(newTask));
    }

    @Test
    public void addSubtaskAddsSubtaskGivenValidValues() {
        Task newTask = new Task(17, 1, 1, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        int rowsAdded = taskRepository.addSubtask(newTask);
        assertEquals(1, rowsAdded);
        Task newTaskFromDB = taskRepository.getTaskByIDOrThrow(17);
        assertEquals(newTask, newTaskFromDB);
    }

    @Test
    public void addSubtaskThrowsOnInvalidProjectID() {
        Task newTask = new Task(0, -1, 1, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No project with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addSubtaskThrowsOnInvalidParentTask() {
        Task newTask = new Task(0, 1, -1, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addSubtaskThrowsOnAddingSubtaskToAnotherSubtask() {
        Task newTask = new Task(0, 1, 2, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("Only tasks can have subtasks, not subtasks.", NotSupportedException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addSubtaskThrowsOnTryingToAddTaskThroughAddSubtask() {
        Task newTask = new Task(0, 1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may not add tasks with addSubtask.", NotSupportedException.class, () -> taskRepository.addSubtask(newTask));
    }

    @Test
    public void addFollowDependencyAddsDependencyWhenGivenValidTaskIDs() {
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
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may not set a task as blocking itself.", NotSupportedException.class, () -> taskRepository.addFollowsDependency(5, 5));
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
    public void assignEmployeeThrowsOnDuplicateAttempt() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The employee with username lildawg is already assigned the task with ID 16.", EntityAlreadyExistsException.class, () -> taskRepository.assignTaskToEmployee(16, "lildawg"));
    }

    @Test
    public void assignEmployeeThrowsOnNonSubtask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may only assign workers to subtasks.", NotSupportedException.class, () -> taskRepository.assignTaskToEmployee(1, "lildawg"));
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
    public void unassignEmployeeThrowsWhenTryingToDeleteNonExistentAssignment() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The employee with username marqs is not assigned the task with ID 16.", EntityDoesNotExistException.class, () -> taskRepository.unassignTaskFromEmployee(16, "marqs"));
    }

    @Test
    public void getTaskByIDOrThrowGetsTaskIfItExists() {
        Task expectedTask = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        Task actualTask = taskRepository.getTaskByIDOrThrow(2);
        assertNotNull(actualTask);
        assertEquals(expectedTask, actualTask);
    }

    @Test
    public void getTaskByIDOrThrowThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(-1));
    }

    @Test
    public void getAllSubtasksForParentTaskWorksOnExistingProjectWithTasks() {
        assertEquals(6, taskRepository.getAllSubtasksForParentTask(1).size());
    }

    @Test
    public void getAllSubtasksForParentTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllSubtasksForParentTask(-1));
    }

    @Test
    public void getAllSubtasksForParentTaskThrowsOnTryingToUseASubtaskAsParent() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("A subtask must have no subtasks.", NotSupportedException.class, () -> taskRepository.getAllSubtasksForParentTask(2));
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
        List<TaskRepository.ConstPair<String, String>> actualArtifacts = taskRepository.getAllArtifactsForTask(15);
        assertTrue(expectedArtifacts.size() == actualArtifacts.size() && expectedArtifacts.containsAll(actualArtifacts));
    }

    @Test
    public void getAllArtifactsForTaskAlsoWorksOnParentTasks() {
        List<TaskRepository.ConstPair<String, String>> expectedArtifacts = List.of(
                new TaskRepository.ConstPair<>("marqs", "github.com/Gruppe-8-org/plannex"),
                new TaskRepository.ConstPair<>("marqs", "docs.google.com/rapport"),
                new TaskRepository.ConstPair<>("lildawg", "docs/domain_model_plannex.png"),
                new TaskRepository.ConstPair<>("marqs", "docs.google.com/rapport"),
                new TaskRepository.ConstPair<>("lildawg", "figma.com/")
        );
        List<TaskRepository.ConstPair<String, String>> actualArtifacts = taskRepository.getAllArtifactsForTask(1);
        assertTrue(expectedArtifacts.size() == actualArtifacts.size() && expectedArtifacts.containsAll(actualArtifacts));
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
        List<Float> expectedContributions = List.of(4.0f, 4.0f);
        List<Float> actualContributions = taskRepository.getAllTimeContributionsForTask(16);
        assertEquals(expectedContributions, actualContributions);
    }

    @Test
    public void getAllTimeContributionsForTaskGetsTimeSpentOfAllChildrenOfASupertask() {
        List<Float> expectedContributions = List.of(0.5f, 0.1667f, 0.75f, 0.75f, 0.5f, 1.0f, 1.0f, 1.0f, 5.0f, 5.0f, 5.0f, 2.0f);
        List<Float> actualContributions = taskRepository.getAllTimeContributionsForTask(1);
        assertTrue(expectedContributions.size() == actualContributions.size() && expectedContributions.containsAll(actualContributions));
    }

    @Test
    public void getAllTimeContributionsForTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.getAllTimeContributionsForTask(-1));
    }


    @Test
    public void getAllAssigneesGetsAllAsigneesIfSubtaskHasThem() {
        List<ProjectEmployee> expectedAssigneeList = List.of(new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)));
        List<ProjectEmployee> actualAssigneeList = taskRepository.getAllAssigneesForSubtask(10);
        assertEquals(expectedAssigneeList, actualAssigneeList);
    }

    @Test // Only unique assignees, try the query in MySQL workbench without DISTINCT and see difference
    public void getAllAssigneesGetsAllAssignessFromAllSubtasksIfUsedOnTaskWithSubtasks() {
        List<ProjectEmployee> expectedAssignees = List.of(
                new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("bigdawg", "Max", "MRK@gmail.com", "0uFF!nÆr", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)),
                new ProjectEmployee("marqs", "Markus", "MBR@gmail.com", "HhQEsN4t", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0))
        );

        List<ProjectEmployee> actualAssignees = taskRepository.getAllAssigneesForTask(1);
        assertTrue(expectedAssignees.size() == actualAssignees.size() && expectedAssignees.containsAll(actualAssignees)); // Order differs in DB
    }

    @Test
    public void getAllAssigneesReturnsNullOnTaskWithNoAssigneesOrIfTaskDoesNotExist() {
        assertEquals(0, taskRepository.getAllAssigneesForSubtask(-1).size()); // Doesn't exist (yet)
    }

    @Test
    public void updateTaskUpdatesOnlyWantedFieldsIfTaskExists() {
        Task expectedTask = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 13), LocalDate.of(2025, 11, 14), 2.0f);
        Task taskFromDBBefore = taskRepository.getTaskByIDOrThrow(2);
        assertEquals(1, taskRepository.updateTask(expectedTask, 2));
        Task taskFromDBAfter = taskRepository.getTaskByIDOrThrow(2);
        assertNotEquals(taskFromDBAfter, taskFromDBBefore);
        assertEquals(expectedTask, taskFromDBAfter);
    }

    @Test
    public void updateTaskThrowsOnNonExistentTask() {
        Task newTask = new Task(-1, 1, 0, "TaskTitle", "TaskDescription", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.updateTask(newTask, -1));
    }

    @Test
    public void addArtifactAddsAnArtifactProvidedItDoesNotAlreadyExist() {
        int rowsAffected = taskRepository.addArtifact(13, "lildawg", "resources/testArtifact.a");
        assertEquals(1, rowsAffected);
        Object[] artifactValues = jdbcTemplate.queryForObject("SELECT * FROM Artifacts WHERE TaskID = ? AND ArtifactAuthor = ? AND PathToArtifact = ?", (rs, rowNum) -> new Object[] {rs.getInt("TaskID"), rs.getString("ArtifactAuthor"), rs.getString("PathToArtifact")}, 13, "lildawg", "resources/testArtifact.a");
        assert artifactValues != null;
        assertEquals(13, artifactValues[0]);
        assertEquals("lildawg", artifactValues[1]);
        assertEquals("resources/testArtifact.a", artifactValues[2]);
    }

    @Test
    public void addArtifactThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.addArtifact(-1, "lildawg", "resources/testArtifact.a"));
    }

    @Test
    public void addArtifactThrowsIfArtifactAlreadyExistsBySameEmployeeOnSameTask() {
        // The following does not mean that different users can't upload the same artifact to the same task!
        assertThrowsHelper.verifyExceptionThrownWithMessage("Employee lildawg has already uploaded this artifact to this task. Update it if you want to change it.", EntityAlreadyExistsException.class, () -> taskRepository.addArtifact(5, "lildawg", "docs/domain_model_plannex.png"));
    }

    @Test
    public void addArtifactThrowsOnTryingToAddToTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may only add artifacts to subtasks.", NotSupportedException.class, () -> taskRepository.addArtifact(1, "lildawg", "test/test.png"));
    }

    @Test
    public void updateArtifactWorksOnNewPathWhichDoesNotAlreadyExist() {
        int rowsAffected = taskRepository.updateArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex2");
        assertEquals(1, rowsAffected);
        assertNotNull(jdbcTemplate.queryForObject("SELECT PathToArtifact FROM Artifacts WHERE ArtifactAuthor = ? AND TaskID = ? AND PathToArtifact = ?;",
                String.class, "marqs", 2, "github.com/Gruppe-8-org/plannex2"));
    }

    @Test
    public void updateArtifactThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The artifact with path github.com/Gruppe-8-org/plannex does not exist, uploaded by marqs for task with ID -1.", EntityDoesNotExistException.class, () -> taskRepository.updateArtifact(-1, "marqs", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex"));
    }

    @Test
    public void updateArtifactThrowsOnNonExistentArtifact() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The artifact with path github.com/Gruppe-8-org/plannex3 does not exist, uploaded by marqs for task with ID 2.", EntityDoesNotExistException.class, () -> taskRepository.updateArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex3", "github.com/Gruppe-8-org/plannex4"));
    }

    @Test
    public void updateArtifactThrowsIfNewPathAlreadyExists() {
        int rowsAdded = taskRepository.addArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex2");
        assertEquals(1, rowsAdded);
        assertThrowsHelper.verifyExceptionThrownWithMessage("The artifact with path github.com/Gruppe-8-org/plannex2 already exists. Change its name or delete and replace it.", EntityAlreadyExistsException.class, () -> taskRepository.updateArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex", "github.com/Gruppe-8-org/plannex2"));
    }

    @Test
    public void deleteArtifactDeletesExistingArtifact() {
        int rowsDeleted = taskRepository.deleteArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex");
        assertEquals(1, rowsDeleted);
        assertThrowsHelper.verifyExceptionThrownWithMessage("The artifact with path github.com/Gruppe-8-org/plannex does not exist, uploaded by marqs for task with ID 2.", EntityDoesNotExistException.class, () -> taskRepository.deleteArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex"));
    }

    @Test
    public void deleteArtifactThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The artifact with path github.com/Gruppe-8-org/plannex does not exist, uploaded by marqs for task with ID -1.", EntityDoesNotExistException.class, () -> taskRepository.deleteArtifact(-1, "marqs", "github.com/Gruppe-8-org/plannex"));
    }

    @Test
    public void deleteArtifactThrowsOnNonExistentArtifact() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("The artifact with path github.com/Gruppe-8-org/plannex2 does not exist, uploaded by marqs for task with ID 2.", EntityDoesNotExistException.class, () -> taskRepository.deleteArtifact(2, "marqs", "github.com/Gruppe-8-org/plannex2"));
    }

    @Test
    public void contributeTimeAddsTimeSpent() {
        int rowsAffected = taskRepository.contributeTime("marqs", 2, 2.0f);
        assertEquals(1, rowsAffected);
        assertNotNull(jdbcTemplate.queryForObject("SELECT COUNT(*) > 0 FROM TimeSpent WHERE OnTaskID = ? AND ByEmployee = ? AND HoursSpent = ?", Boolean.class, 1, "marqs", 2));
    }

    @Test
    public void contributeTimeThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.contributeTime("marqs", -1, 2.0f));
    }

    @Test
    public void contributeTimeThrowsOnNegativeTimeSpent() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("Hours spent should be more than zero.", InvalidValueException.class, () -> taskRepository.contributeTime("marqs", 17, -2.0f));
    }

    @Test
    public void contributeTimeThrowsOnNonSubtask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("You may only add time spent to subtasks.", NotSupportedException.class, () -> taskRepository.contributeTime("marqs", 1, 2.0f));
    }

    @Test
    public void contributeTimeThrowsOnDuplicate() {
        taskRepository.contributeTime("lildawg", 2, 2.0f);
        assertThrowsHelper.verifyExceptionThrownWithMessage("You just added a similar time contribution. If this is intentional, wait a couple of seconds before trying again.", EntityAlreadyExistsException.class, () -> taskRepository.contributeTime("lildawg", 2, 2.0f));
    }

    @Test
    public void updateTimeContributionUpdatesWithValidValues() {
        int rowsAffected = taskRepository.updateTimeContribution("marqs", 2, 3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0)));
        assertEquals(1, rowsAffected);
        assertEquals(3.0f, jdbcTemplate.queryForObject("SELECT HoursSpent FROM TimeSpent WHERE ByEmployee = ? AND OnTaskID = ? AND _When = ?;",
                float.class, "marqs", 2, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))));
    }

    @Test
    public void updateTimeContributionThrowsOnNegativeTimeSpent() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("Hours spent should be zero or more.", InvalidValueException.class, () -> taskRepository.updateTimeContribution("marqs", 2, -3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))));
    }

    @Test
    public void updateTimeContributionThrowsOnNonExistentContribution() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No time contribution by marqs on task with ID 2 at 2025-11-12T12:00 exists.", EntityDoesNotExistException.class, () -> taskRepository.updateTimeContribution("marqs", 2, 3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(12, 0, 0))));
    }

    @Test
    public void updateTimeContributionThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No time contribution by marqs on task with ID -1 at 2025-11-12T10:00 exists.", EntityDoesNotExistException.class, () -> taskRepository.updateTimeContribution("marqs", -1, 3.0f, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))));
    }

    @Test
    public void deleteTimeContributionDeletesGivenValidValues() {
        int rowsAffected = taskRepository.deleteTimeContribution("marqs", 2, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0)));
        assertEquals(1, rowsAffected);
        assertThrows(EmptyResultDataAccessException.class, () -> jdbcTemplate.queryForObject("SELECT HoursSpent FROM TimeSpent WHERE ByEmployee = ? AND OnTaskID = ? AND _When = ?;",
                float.class, "marqs", 2, LocalTime.of(10, 0, 0)));
    }

    @Test
    public void deleteTimeContributionThrowsOnNonExistentTimeContribution() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No time contribution by marqs on task with ID 2 at 2025-11-12T14:00 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteTimeContribution("marqs", 2, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(14, 0, 0))));
    }

    @Test
    public void deleteTimeContributionThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No time contribution by marqs on task with ID -1 at 2025-11-12T10:00 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteTimeContribution("marqs", -1, LocalDateTime.of(LocalDate.of(2025, 11, 12), LocalTime.of(10, 0, 0))));
    }

    @Test
    public void deleteTaskDeletesOnExistingTask() {
        int rowsDeleted = taskRepository.deleteTaskByID(1);
        assertEquals(1, rowsDeleted); // Cascades don't count
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID 1 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteTaskByID(1));
    }

    @Test
    public void deleteTaskThrowsOnNonExistentTask() {
        assertThrowsHelper.verifyExceptionThrownWithMessage("No task with ID -1 exists.", EntityDoesNotExistException.class, () -> taskRepository.deleteTaskByID(-1));
    }

    @Test
    public void deleteTaskCascadesCorrectly() {
        int rowsDeleted = taskRepository.deleteTaskByID(1);
        assertEquals(1, rowsDeleted); // Cascading doesn't count
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(1));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(2));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(3));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(4));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(5));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(6));
        assertThrows(EntityDoesNotExistException.class, () -> taskRepository.getTaskByIDOrThrow(7));
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
