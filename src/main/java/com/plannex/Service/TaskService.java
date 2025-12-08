package com.plannex.Service;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.Repository.TaskRepository;
import org.springframework.stereotype.Service;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public boolean isSubtask(Task t) {
        return taskRepository.isSubtask(t);
    }

    public int addTask(Task task) throws OperationNotSupportedException {
        return taskRepository.addTask(task);
    }

    public int addSubtask(Task task) throws OperationNotSupportedException {
        return taskRepository.addSubtask(task);
    }

    public int addFollowsDependency(int forTaskID, int blockedByID) throws OperationNotSupportedException {
        return taskRepository.addFollowsDependency(forTaskID, blockedByID);
    }

    public int deleteFollowsDependency(int forTaskID, int blockedByID) {
        return taskRepository.deleteFollowsDependency(forTaskID, blockedByID);
    }

    public int assignTaskToEmployee(int taskID, String employeeUsername) {
        return taskRepository.assignTaskToEmployee(taskID, employeeUsername);
    }

    public int unassignTaskFromEmployee(int taskID, String employeeUsername) {
        return taskRepository.unassignTaskFromEmployee(taskID, employeeUsername);
    }

    public Task getTaskByID(int taskID) {
        return taskRepository.getTaskByID(taskID);
    }

    public List<Task> getAllSubtasksForParentTask(int parentTaskID) {
        return taskRepository.getAllSubtasksForParentTask(parentTaskID);
    }

    public List<TaskRepository.ConstPair<String, String>> getAllArtifactsForTask(int taskID) {
        return taskRepository.getAllArtifactsForTask(taskID);
    }

    public List<TaskRepository.ConstPair<Integer, Integer>> getAllDependenciesForTask(int taskID) {
        return taskRepository.getAllDependenciesForTask(taskID);
    }

    public List<Float> getAllTimeContributionsForTask(int taskID) {
        return taskRepository.getAllTimeContributionsForTask(taskID);
    }

    public int updateTask(Task modifiedTask, int targetTaskID) {
        if (taskRepository.getTaskByID(targetTaskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + targetTaskID + " exists.");
        }

        return taskRepository.updateTask(modifiedTask, targetTaskID);
    }

    public int deleteTaskByID(int taskID) {
        if (taskRepository.getTaskByID(taskID) == null) {
            throw new EntityDoesNotExistException("No task with ID " + taskID + " exists.");
        }

        return taskRepository.deleteTaskByID(taskID);
    }

    public int addArtifact(int taskID, String username, String pathToArtifact) {
        return taskRepository.addArtifact(taskID, username, pathToArtifact);
    }

    public int updateArtifact(int taskID, String username, String oldPath, String newPath) {
        return taskRepository.updateArtifact(taskID, username, oldPath, newPath);
    }

    public int deleteArtifact(int taskID, String username, String path) {
        return taskRepository.deleteArtifact(taskID, username, path);
    }

    public int contributeTime(String username, int taskID, float hours) {
        return taskRepository.contributeTime(username, taskID, hours);
    }

    public int updateTimeContribution(String username, int taskID, float hours, LocalDateTime when) {
        return taskRepository.updateTimeContribution(username, taskID, hours, when);
    }

    public int deleteTimeContribution(String username, int taskID, LocalDateTime when) {
        return taskRepository.deleteTimeContribution(username, taskID, when);
    }

    public List<ProjectEmployee> getAllAssigneesForSubtask(int subtaskID) {
        return taskRepository.getAllAssigneesForSubtask(subtaskID);
    }

    public List<ProjectEmployee> getAllAssigneesForTask(int taskID) {
        return taskRepository.getAllAssigneesForTask(taskID);
    }

    public Integer getAllInvolved(int taskID) {
        return taskRepository.getAllInvolved(taskID);
    }

    public float getTotalTimeSpent(int taskID) {
        return taskRepository.getTotalTimeSpent(taskID);
    }
}
