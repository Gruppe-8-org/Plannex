package com.plannex.Service;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Model.Task;
import com.plannex.Repository.TaskRepository;
import org.springframework.stereotype.Service;

import javax.naming.OperationNotSupportedException;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public int addTask(Task task) throws OperationNotSupportedException {
        return taskRepository.addTask(task);
    }

    public int addSubtask(Task task) throws OperationNotSupportedException {
        return taskRepository.addSubtask(task);
    }

    public int addFollowsDependency(int forTaskID, int blockedByID) {
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

    public List<Task> getAllTasksForProject(int projectID) {
        return taskRepository.getAllTasksForProject(projectID);
    }

    public List<Task> getAllSubtasksForParentTask(int parentTaskID) throws OperationNotSupportedException {
        return taskRepository.getAllSubtasksForParentTask(parentTaskID);
    }

    public List<TaskRepository.ConstPair<String, String>> getAllArtifactsForTask(int taskID) {
        return taskRepository.getAllArtifactsForTask(taskID);
    }

    public List<TaskRepository.ConstPair<Integer, Integer>> getAllDependenciesForTask(int taskID) {
        return taskRepository.getAllDependenciesForTask(taskID);
    }

    public List<Integer> getAllTimeContributionsForTask(int taskID) {
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
}
