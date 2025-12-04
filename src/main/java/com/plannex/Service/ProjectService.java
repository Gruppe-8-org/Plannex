package com.plannex.Service;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.Repository.ProjectRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    public int addProject(Project project) {
        return projectRepository.addProject(project);
    }

    public Project getProjectByID(int projectID) {
        return projectRepository.getProjectByID(projectID);
    }

    public List<Project> getAllProjects() {
        return projectRepository.getAllProjects();
    }

    public int updateProject(Project modifiedProject, int targetProjectID) {
        if (projectRepository.getProjectByID(targetProjectID) == null) {
            throw new EntityDoesNotExistException("No project with projectID " + targetProjectID + " exists.");
        }

        return projectRepository.updateProject(modifiedProject, targetProjectID);
    }

    public int deleteProjectByID(int projectID) {
        if (projectRepository.getProjectByID(projectID) == null) {
            throw new EntityDoesNotExistException("No project with projectID " + projectID + " exists.");
        }

        return projectRepository.deleteProjectByID(projectID);
    }

    public List<Task> getAllTasksForProject(int projectID) {
        return projectRepository.getAllTasksForProject(projectID);
    }

    public Integer getAllInvolved(int projectID) {
        return projectRepository.getAllInvolved(projectID);
    }

    public float getTotalTimeSpent(int projectID) {
        return projectRepository.getTotalTimeSpent(projectID);
    }
}
