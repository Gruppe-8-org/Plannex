package com.plannex.ServiceTests;

import com.plannex.Exception.EntityDoesNotExistException;
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
        task = new Task(1, 0, "TaskTitle", "TaskDescription",
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
    void addFollowsDependencyCallsRepository() {
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
    void getAllTasksForProjectCallsRepository() {
        when(taskRepository.getAllTasksForProject(1)).thenReturn(List.of(task));
        List<Task> result = taskService.getAllTasksForProject(1);
        assertEquals(1, result.size());
        verify(taskRepository).getAllTasksForProject(1);
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
        when(taskRepository.getAllTimeContributionsForTask(1)).thenReturn(List.of(3, 5));
        List<Integer> result = taskService.getAllTimeContributionsForTask(1);
        assertEquals(2, result.size());
        verify(taskRepository).getAllTimeContributionsForTask(1);
    }
}
