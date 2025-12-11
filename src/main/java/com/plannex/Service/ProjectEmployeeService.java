package com.plannex.Service;

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

    public String getPermissions(String username) {
        return projectEmployeeRepository.getEmployeePermissions(username);
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

    public List<ProjectEmployee> getAllWorkers() {
        return projectEmployeeRepository.getAllWorkers();
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

    public boolean login(String username, String pw) {
        return projectEmployeeRepository.login(username, pw);
    }
}
