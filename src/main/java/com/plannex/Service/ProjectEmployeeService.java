package com.plannex.Service;

import com.plannex.Exception.EntityAlreadyExistsException;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.InvalidValueException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Repository.ProjectEmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository projectEmployeeRepository;

    public ProjectEmployeeService(ProjectEmployeeRepository projectEmployeeRepository) {
        this.projectEmployeeRepository = projectEmployeeRepository;
    }

    public int addEmployee(ProjectEmployee employee, String permissions) {
        return projectEmployeeRepository.addEmployee(employee, permissions);
    }

    public ProjectEmployee getEmployeeByUsername(String username) {
        return projectEmployeeRepository.getEmployeeByUsername(username);
    }

    public List<ProjectEmployee> getAllEmployees() {
        return projectEmployeeRepository.getAllEmployees();
    }

    public String getEmployeePermissions(String username) {
        return projectEmployeeRepository.getEmployeePermissions(username);
    }

    public int updateEmployee(ProjectEmployee updatedEmployee, String targetUsername) {
        return projectEmployeeRepository.updateEmployee(updatedEmployee, targetUsername);
    }

    public int deleteEmployeeByUsername(String username) {
        return projectEmployeeRepository.deleteEmployeeByUsername(username);
    }

    public List<ProjectEmployee> getAllAssigneesForSubtask(int subtaskID) {
        return projectEmployeeRepository.getAllAssigneesForSubtask(subtaskID);
    }

    public List<ProjectEmployee> getAllAssigneesForTask(int taskID) {
        return projectEmployeeRepository.getAllAssigneesForTask(taskID);
    }

    public int addArtifact(int taskID, String username, String pathToArtifact) {
        return projectEmployeeRepository.addArtifact(taskID, username, pathToArtifact);
    }

    public int updateArtifact(int taskID, String username, String oldPath, String newPath) {
        return projectEmployeeRepository.updateArtifact(taskID, username, oldPath, newPath);
    }

    public int deleteArtifact(int taskID, String username, String path) {
        return projectEmployeeRepository.deleteArtifact(taskID, username, path);
    }

    public int contributeTime(String username, int taskID, float hours) {
        return projectEmployeeRepository.contributeTime(username, taskID, hours);
    }

    public int updateTimeContribution(String username, int taskID, float hours, LocalDateTime when) {
        return projectEmployeeRepository.updateTimeContribution(username, taskID, hours, when);
    }

    public int deleteTimeContribution(String username, int taskID, float hours, LocalDateTime when) {
        return projectEmployeeRepository.deleteTimeContribution(username, taskID, hours, when);
    }
}
