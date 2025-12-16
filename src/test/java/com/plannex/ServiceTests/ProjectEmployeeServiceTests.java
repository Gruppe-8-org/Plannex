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
import com.plannex.Model.EmployeeSkill;
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

    @Test
    void deleteEmployeeByUsername_CallsRepository() {
        when(repo.deleteEmployeeByUsername("johnDoe")).thenReturn(1);

        int result = service.deleteEmployeeByUsername("johnDoe");

        assertEquals(1, result);
        verify(repo).deleteEmployeeByUsername("johnDoe");
    }
    @Test
    void getSkillsForEmployee_ReturnsListFromRepository() {
        List<EmployeeSkill> skills = List.of(
                new EmployeeSkill("johnDoe", "Java", "Expert"),
                new EmployeeSkill("johnDoe", "HTML", "Intermediate")
        );

        when(repo.getSkillsForEmployee("johnDoe")).thenReturn(skills);

        List<EmployeeSkill> result = service.getSkillsForEmployee("johnDoe");

        assertEquals(skills, result);
        verify(repo).getSkillsForEmployee("johnDoe");
    }

    @Test
    void addSkill_CallsRepository_WhenSkillLevelIsValid() {
        service.addSkill("Java");

        verify(repo).addSkillUnlessItAlreadyExists("Java");
    }

    @Test
    void removeSkill_CallsRepository() {
        service.removeSkill("Java");
        verify(repo).removeSkillIfExists("Java");
    }

    @Test
    void getBaseWage_ReturnsConstantValue() {
        float wage = service.getBaseWage("johnDoe");

        assertEquals(300.0f, wage);
    }

    @Test
    void calculateHourlyWage_UsesRepositoryCountsCorrectly() {
        when(repo.countExpertSkills("johnDoe")).thenReturn(2);
        when(repo.countIntermediateSkills("johnDoe")).thenReturn(1);

        float result = service.calculateHourlyWage("johnDoe");

        float expected = (float) (300 * Math.pow(1.10, 2) * Math.pow(1.05, 1));

        assertEquals(expected, result, 0.0001);
    }

    @Test
    void calculateHourlyWage_NoSkillsMeansBaseWage() {
        when(repo.countExpertSkills("johnDoe")).thenReturn(0);
        when(repo.countIntermediateSkills("johnDoe")).thenReturn(0);

        float result = service.calculateHourlyWage("johnDoe");

        assertEquals(300.0f, result);
    }


    @Test
    void getAllWorkersReturnsList() {
        List<ProjectEmployee> list = List.of(emp);
        when(repo.getAllWorkers()).thenReturn(list);

        List<ProjectEmployee> result = service.getAllWorkers();

        assertEquals(list, result);
        verify(repo).getAllWorkers();
    }
}
