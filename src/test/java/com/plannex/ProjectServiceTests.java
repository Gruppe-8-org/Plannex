package com.plannex;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.Model.Task;
import com.plannex.Repository.ProjectRepository;
import com.plannex.Service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        project = new Project(
                5,
                "Title",
                "Description",
                LocalDate.of(2025, 11, 28),
                LocalDate.of(2025, 12, 31)
        );
    }

    @Test
    void addProjectCallsRepository() {
        when(projectRepository.addProject(project)).thenReturn(1);

        int result = projectService.addProject(project);
        assertEquals(1, result);
        verify(projectRepository, times(1)).addProject(project);
    }

    @Test
    void getProjectByIDReturnsProject() {
        when(projectRepository.getProjectByIDOrThrow(1)).thenReturn(project);

        Project result = projectService.getProjectByID(1);
        assertEquals(project, result);
        verify(projectRepository, times(1)).getProjectByIDOrThrow(1);
    }

    @Test
    void getAllProjectsReturnsList() {
        when(projectRepository.getAllProjects()).thenReturn(List.of(project));

        List<Project> result = projectService.getAllProjects();
        assertEquals(1, result.size());
        assertEquals(project, result.get(0));
        verify(projectRepository, times(1)).getAllProjects();
    }

    @Test
    void updateProjectThrowsIfProjectDoesNotExist() {
        when(projectRepository.updateProject(project, 2)).thenThrow(new EntityDoesNotExistException("No project with ID 2 exists."));
        EntityDoesNotExistException exception = assertThrows(
                EntityDoesNotExistException.class,
                () -> projectService.updateProject(project, 2)
        );

        assertEquals("No project with ID 2 exists.", exception.getMessage());
        verify(projectRepository, times(1)).updateProject(any(), anyInt());
    }

    @Test
    void updateProjectCallsRepositoryIfExists() {
        when(projectRepository.getProjectByIDOrThrow(1)).thenReturn(project);
        when(projectRepository.updateProject(project, 1)).thenReturn(1);

        int result = projectService.updateProject(project, 1);
        assertEquals(1, result);
        verify(projectRepository, times(1)).updateProject(project, 1);
    }

    @Test
    void deleteProjectThrowsIfProjectDoesNotExist() {
        when(projectRepository.deleteProjectByID(2)).thenThrow(new EntityDoesNotExistException("No project with ID 2 exists."));
        EntityDoesNotExistException exception = assertThrows(
                EntityDoesNotExistException.class,
                () -> projectService.deleteProjectByID(2)
        );

        assertEquals("No project with ID 2 exists.", exception.getMessage());
        verify(projectRepository, times(1)).deleteProjectByID(anyInt());
    }

    @Test
    void deleteProjectCallsRepositoryIfExists() {
        when(projectRepository.getProjectByIDOrThrow(1)).thenReturn(project);
        when(projectRepository.deleteProjectByID(1)).thenReturn(1);

        int result = projectService.deleteProjectByID(1);
        assertEquals(1, result);
        verify(projectRepository, times(1)).deleteProjectByID(1);
    }

    @Test
    void getAllTasksForProjectCallsRepository() {
        when(projectRepository.getAllTasksForProject(1)).thenReturn(List.of(new Task(0, 1, 0, "TaskTitle", "TaskDescription",
                LocalDate.of(2025, 11, 28), LocalDate.of(2025, 12, 5), 5.0f)));
        List<Task> result = projectRepository.getAllTasksForProject(1);
        assertEquals(1, result.size());
        verify(projectRepository).getAllTasksForProject(1);
    }
}

