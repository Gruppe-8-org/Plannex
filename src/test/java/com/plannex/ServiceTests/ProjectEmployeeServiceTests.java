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
}
