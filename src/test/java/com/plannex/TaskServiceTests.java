package com.plannex;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.Repository.TaskRepository;
import com.plannex.Service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task(0, 1, 0, "TaskTitle", "TaskDescription",
                LocalDate.of(2025, 11, 28), LocalDate.of(2025, 12, 5), 5.0f);
    }

    @Test
    void addTaskCallsRepository() throws OperationNotSupportedException {
        when(taskRepository.addTask(task)).thenReturn(1);
        int result = taskService.addTask(task);
        assertEquals(1, result);
        verify(taskRepository).addTask(task);
    }

    @Test
    void addSubtaskCallsRepository() throws OperationNotSupportedException {
        when(taskRepository.addSubtask(task)).thenReturn(1);
        int result = taskService.addSubtask(task);
        assertEquals(1, result);
        verify(taskRepository).addSubtask(task);
    }

    @Test
    void addFollowsDependencyCallsRepository() throws OperationNotSupportedException {
        when(taskRepository.addFollowsDependency(1, 2)).thenReturn(1);
        int result = taskService.addFollowsDependency(1, 2);
        assertEquals(1, result);
        verify(taskRepository).addFollowsDependency(1, 2);
    }

    @Test
    void deleteFollowsDependencyCallsRepository() {
        when(taskRepository.deleteFollowsDependency(1, 2)).thenReturn(1);
        int result = taskService.deleteFollowsDependency(1, 2);
        assertEquals(1, result);
        verify(taskRepository).deleteFollowsDependency(1, 2);
    }

    @Test
    void assignTaskToEmployeeCallsRepository() {
        when(taskRepository.assignTaskToEmployee(1, "user")).thenReturn(1);
        int result = taskService.assignTaskToEmployee(1, "user");
        assertEquals(1, result);
        verify(taskRepository).assignTaskToEmployee(1, "user");
    }

    @Test
    void unassignTaskFromEmployeeCallsRepository() {
        when(taskRepository.unassignTaskFromEmployee(1, "user")).thenReturn(1);
        int result = taskService.unassignTaskFromEmployee(1, "user");
        assertEquals(1, result);
        verify(taskRepository).unassignTaskFromEmployee(1, "user");
    }

    @Test
    void getTaskByIDReturnsTaskFromRepository() {
        when(taskRepository.getTaskByID(1)).thenReturn(task);
        Task result = taskService.getTaskByID(1);
        assertEquals(task, result);
        verify(taskRepository).getTaskByID(1);
    }

    @Test
    void updateTaskThrowsIfTaskDoesNotExist() {
        when(taskRepository.getTaskByID(1)).thenReturn(null);
        assertThrows(EntityDoesNotExistException.class, () -> taskService.updateTask(task, 1));
        verify(taskRepository, never()).updateTask(any(), anyInt());
    }

    @Test
    void updateTaskCallsRepositoryIfExists() {
        when(taskRepository.getTaskByID(1)).thenReturn(task);
        when(taskRepository.updateTask(task, 1)).thenReturn(1);
        int result = taskService.updateTask(task, 1);
        assertEquals(1, result);
        verify(taskRepository).updateTask(task, 1);
    }

    @Test
    void deleteTaskByIDThrowsIfTaskDoesNotExist() {
        when(taskRepository.getTaskByID(1)).thenReturn(null);
        assertThrows(EntityDoesNotExistException.class, () -> taskService.deleteTaskByID(1));
        verify(taskRepository, never()).deleteTaskByID(anyInt());
    }

    @Test
    void deleteTaskByIDCallsRepositoryIfExists() {
        when(taskRepository.getTaskByID(1)).thenReturn(task);
        when(taskRepository.deleteTaskByID(1)).thenReturn(1);
        int result = taskService.deleteTaskByID(1);
        assertEquals(1, result);
        verify(taskRepository).deleteTaskByID(1);
    }

    @Test
    void getAllSubtasksForParentTaskCallsRepository() throws OperationNotSupportedException {
        when(taskRepository.getAllSubtasksForParentTask(1)).thenReturn(List.of(task));
        List<Task> result = taskService.getAllSubtasksForParentTask(1);
        assertEquals(1, result.size());
        verify(taskRepository).getAllSubtasksForParentTask(1);
    }

    @Test
    void getAllArtifactsForTaskCallsRepository() {
        TaskRepository.ConstPair<String, String> artifact = new TaskRepository.ConstPair<>("user", "path");
        when(taskRepository.getAllArtifactsForTask(1)).thenReturn(List.of(artifact));
        List<TaskRepository.ConstPair<String, String>> result = taskService.getAllArtifactsForTask(1);
        assertEquals(1, result.size());
        verify(taskRepository).getAllArtifactsForTask(1);
    }

    @Test
    void getAllDependenciesForTaskCallsRepository() {
        TaskRepository.ConstPair<Integer, Integer> dep = new TaskRepository.ConstPair<>(1, 2);
        when(taskRepository.getAllDependenciesForTask(1)).thenReturn(List.of(dep));
        List<TaskRepository.ConstPair<Integer, Integer>> result = taskService.getAllDependenciesForTask(1);
        assertEquals(1, result.size());
        verify(taskRepository).getAllDependenciesForTask(1);
    }

    @Test
    void getAllTimeContributionsForTaskCallsRepository() {
        when(taskRepository.getAllTimeContributionsForTask(1)).thenReturn(List.of(3.0f, 5.0f));
        List<Float> result = taskService.getAllTimeContributionsForTask(1);
        assertEquals(2, result.size());
        verify(taskRepository).getAllTimeContributionsForTask(1);
    }

    @Test
    void addArtifact_CallsRepository() {
        when(taskRepository.addArtifact(7, "johnDoe", "path")).thenReturn(1);

        int result = taskService.addArtifact(7, "johnDoe", "path");

        assertEquals(1, result);
        verify(taskRepository).addArtifact(7, "johnDoe", "path");
    }

    @Test
    void updateArtifact_CallsRepository() {
        when(taskRepository.updateArtifact(7, "johnDoe", "old", "new")).thenReturn(1);

        int result = taskService.updateArtifact(7, "johnDoe", "old", "new");

        assertEquals(1, result);
        verify(taskRepository).updateArtifact(7, "johnDoe", "old", "new");
    }

    @Test
    void deleteArtifact_CallsRepository() {
        when(taskRepository.deleteArtifact(7, "johnDoe", "path")).thenReturn(1);

        int result = taskService.deleteArtifact(7, "johnDoe", "path");

        assertEquals(1, result);
        verify(taskRepository).deleteArtifact(7, "johnDoe", "path");
    }

    @Test
    void contributeTime_CallsRepository() {
        when(taskRepository.contributeTime("johnDoe", 5, 3f)).thenReturn(1);

        int result = taskService.contributeTime("johnDoe", 5, 3f);

        assertEquals(1, result);
        verify(taskRepository).contributeTime("johnDoe", 5, 3f);
    }

    @Test
    void updateTimeContribution_CallsRepository() {
        LocalDateTime now = LocalDateTime.now();
        when(taskRepository.updateTimeContribution("johnDoe", 5, 3f, now)).thenReturn(1);

        int result = taskService.updateTimeContribution("johnDoe", 5, 3f, now);

        assertEquals(1, result);
        verify(taskRepository).updateTimeContribution("johnDoe", 5, 3f, now);
    }

    @Test
    void deleteTimeContribution_CallsRepository() {
        LocalDateTime now = LocalDateTime.now();
        when(taskRepository.deleteTimeContribution("johnDoe", 5, now)).thenReturn(1);

        int result = taskService.deleteTimeContribution("johnDoe", 5, now);

        assertEquals(1, result);
        verify(taskRepository).deleteTimeContribution("johnDoe", 5, now);
    }

    @Test
    void getAllAssigneesForSubtask_ReturnsList() {
        when(taskRepository.getAllAssigneesForSubtask(10)).thenReturn(List.of(new ProjectEmployee(
                "johnDoe",
                "John Doe",
                "john@example.com",
                "password",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        )));

        List<ProjectEmployee> result = taskService.getAllAssigneesForSubtask(10);

        assertEquals(1, result.size());
        verify(taskRepository).getAllAssigneesForSubtask(10);
    }

    @Test
    void getAllAssigneesForTask_ReturnsList() {
        when(taskRepository.getAllAssigneesForTask(5)).thenReturn(List.of(new ProjectEmployee(
                "johnDoe",
                "John Doe",
                "john@example.com",
                "password",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        )));

        List<ProjectEmployee> result = taskService.getAllAssigneesForTask(5);

        assertEquals(1, result.size());
        verify(taskRepository).getAllAssigneesForTask(5);
    }

    @Test
    void getAllInvolved_ReturnsCorrectCount() {
        when(taskRepository.getAllInvolved(1)).thenReturn(3);

        Integer result = taskService.getAllInvolved(1);

        assertEquals(3, result);
        verify(taskRepository).getAllInvolved(1);
    }

    @Test
    void getAllInvolved_ThrowsExceptionIfTaskDoesNotExist() {
        when(taskRepository.getAllInvolved(99))
                .thenThrow(new EntityDoesNotExistException("No project with ID 99 exists."));

        assertThrows(EntityDoesNotExistException.class,
                () -> taskService.getAllInvolved(99));

        verify(taskRepository).getAllInvolved(99);
    }

    @Test
    void getTotalTimeSpent_ReturnsCorrectTotal() {
        when(taskRepository.getTotalTimeSpent(1)).thenReturn(12.5f);

        float result = taskService.getTotalTimeSpent(1);

        assertEquals(12.5f, result);
        verify(taskRepository).getTotalTimeSpent(1);
    }

    @Test
    void getTotalTimeSpent_ReturnsZeroIfNoTimeRegistered() {
        when(taskRepository.getTotalTimeSpent(2)).thenReturn(0.0f);

        float result = taskService.getTotalTimeSpent(2);

        assertEquals(0.0f, result);
        verify(taskRepository).getTotalTimeSpent(2);
    }

    @Test
    void getTotalTimeSpent_ThrowsExceptionIfTaskDoesNotExist() {
        when(taskRepository.getTotalTimeSpent(99))
                .thenThrow(new EntityDoesNotExistException("No project with projectID 99 exists."));

        assertThrows(EntityDoesNotExistException.class,
                () -> taskService.getTotalTimeSpent(99));

        verify(taskRepository).getTotalTimeSpent(99);
    }




}
