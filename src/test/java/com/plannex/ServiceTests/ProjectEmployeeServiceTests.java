package com.plannex.ServiceTests;

import com.plannex.Model.ProjectEmployee;
import com.plannex.Repository.ProjectEmployeeRepository;
import com.plannex.Service.ProjectEmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectEmployeeServiceTests {

    @Mock
    private ProjectEmployeeRepository repo;

    @InjectMocks
    private ProjectEmployeeService service;

    private ProjectEmployee emp;

    @BeforeEach
    void setUp() {
        emp = new ProjectEmployee(
                "johnDoe",
                "John Doe",
                "john@example.com",
                "password",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
    }

    @Test
    void addEmployee_CallsRepositoryAndReturnsValue() {
        when(repo.addEmployee(emp, "LEADER")).thenReturn(1);

        int result = service.addEmployee(emp, "LEADER");

        assertEquals(1, result);
        verify(repo, times(1)).addEmployee(emp, "LEADER");
    }

    @Test
    void getEmployeeByUsername_ReturnsEmployee() {
        when(repo.getEmployeeByUsername("johnDoe")).thenReturn(emp);

        ProjectEmployee result = service.getEmployeeByUsername("johnDoe");

        assertEquals(emp, result);
        verify(repo).getEmployeeByUsername("johnDoe");
    }

    @Test
    void getEmployeeByUsername_ReturnsNullIfNotFound() {
        when(repo.getEmployeeByUsername("unknown")).thenReturn(null);

        assertNull(service.getEmployeeByUsername("unknown"));
        verify(repo).getEmployeeByUsername("unknown");
    }

    @Test
    void getAllEmployees_ReturnsList() {
        List<ProjectEmployee> list = List.of(emp);
        when(repo.getAllEmployees()).thenReturn(list);

        List<ProjectEmployee> result = service.getAllEmployees();

        assertEquals(list, result);
        verify(repo).getAllEmployees();
    }

    @Test
    void getEmployeePermissions_ReturnsPermissions() {
        when(repo.getEmployeePermissions("johnDoe")).thenReturn("LEADER");

        String result = service.getEmployeePermissions("johnDoe");

        assertEquals("LEADER", result);
        verify(repo).getEmployeePermissions("johnDoe");
    }

    @Test
    void updateEmployee_CallsRepository() {
        when(repo.updateEmployee(emp, "johnDoe")).thenReturn(1);

        int result = service.updateEmployee(emp, "johnDoe");

        assertEquals(1, result);
        verify(repo).updateEmployee(emp, "johnDoe");
    }

    // ----------------------------------------------------------
    // DELETE EMPLOYEE
    // ----------------------------------------------------------
    @Test
    void deleteEmployeeByUsername_CallsRepository() {
        when(repo.deleteEmployeeByUsername("johnDoe")).thenReturn(1);

        int result = service.deleteEmployeeByUsername("johnDoe");

        assertEquals(1, result);
        verify(repo).deleteEmployeeByUsername("johnDoe");
    }

    @Test
    void getAllAssigneesForSubtask_ReturnsList() {
        when(repo.getAllAssigneesForSubtask(10)).thenReturn(List.of(emp));

        List<ProjectEmployee> result = service.getAllAssigneesForSubtask(10);

        assertEquals(1, result.size());
        verify(repo).getAllAssigneesForSubtask(10);
    }

    @Test
    void getAllAssigneesForTask_ReturnsList() {
        when(repo.getAllAssigneesForTask(5)).thenReturn(List.of(emp));

        List<ProjectEmployee> result = service.getAllAssigneesForTask(5);

        assertEquals(1, result.size());
        verify(repo).getAllAssigneesForTask(5);
    }

    @Test
    void addArtifact_CallsRepository() {
        when(repo.addArtifact(7, "johnDoe", "path")).thenReturn(1);

        int result = service.addArtifact(7, "johnDoe", "path");

        assertEquals(1, result);
        verify(repo).addArtifact(7, "johnDoe", "path");
    }

    @Test
    void updateArtifact_CallsRepository() {
        when(repo.updateArtifact(7, "johnDoe", "old", "new")).thenReturn(1);

        int result = service.updateArtifact(7, "johnDoe", "old", "new");

        assertEquals(1, result);
        verify(repo).updateArtifact(7, "johnDoe", "old", "new");
    }

    @Test
    void deleteArtifact_CallsRepository() {
        when(repo.deleteArtifact(7, "johnDoe", "path")).thenReturn(1);

        int result = service.deleteArtifact(7, "johnDoe", "path");

        assertEquals(1, result);
        verify(repo).deleteArtifact(7, "johnDoe", "path");
    }

    @Test
    void contributeTime_CallsRepository() {
        when(repo.contributeTime("johnDoe", 5, 3f)).thenReturn(1);

        int result = service.contributeTime("johnDoe", 5, 3f);

        assertEquals(1, result);
        verify(repo).contributeTime("johnDoe", 5, 3f);
    }

    @Test
    void updateTimeContribution_CallsRepository() {
        LocalDateTime now = LocalDateTime.now();
        when(repo.updateTimeContribution("johnDoe", 5, 3f, now)).thenReturn(1);

        int result = service.updateTimeContribution("johnDoe", 5, 3f, now);

        assertEquals(1, result);
        verify(repo).updateTimeContribution("johnDoe", 5, 3f, now);
    }

    @Test
    void deleteTimeContribution_CallsRepository() {
        LocalDateTime now = LocalDateTime.now();
        when(repo.deleteTimeContribution("johnDoe", 5, 3f, now)).thenReturn(1);

        int result = service.deleteTimeContribution("johnDoe", 5, 3f, now);

        assertEquals(1, result);
        verify(repo).deleteTimeContribution("johnDoe", 5, 3f, now);
    }
}
