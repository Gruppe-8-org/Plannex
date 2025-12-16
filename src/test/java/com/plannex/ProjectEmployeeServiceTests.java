package com.plannex;

import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Skill;
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
    void getPermissions_ReturnsValue() {
        when(repo.getEmployeePermissions("johnDoe")).thenReturn("ADMIN");

        String result = service.getPermissions("johnDoe");

        assertEquals("ADMIN", result);
        verify(repo).getEmployeePermissions("johnDoe");
    }
    @Test
    void login_ReturnsTrue() {
        when(repo.login("johnDoe", "password")).thenReturn(true);

        boolean result = service.login("johnDoe", "password");

        assertTrue(result);
        verify(repo).login("johnDoe", "password");
    }

    @Test
    void login_ReturnsFalse() {
        when(repo.login("johnDoe", "wrong")).thenReturn(false);

        boolean result = service.login("johnDoe", "wrong");

        assertFalse(result);
        verify(repo).login("johnDoe", "wrong");
    }

    @Test
    void getSkillsForEmployee_ReturnsListFromRepository() {
        List<EmployeeSkill> skills = List.of(
                new EmployeeSkill("johnDoe", "Java", "Expert", 1),
                new EmployeeSkill("johnDoe", "HTML", "Intermediate", 2)
        );

        when(repo.getSkillsForEmployee("johnDoe")).thenReturn(skills);

        List<EmployeeSkill> result = service.getSkillsForEmployee("johnDoe");

        assertEquals(skills, result);
        verify(repo).getSkillsForEmployee("johnDoe");
    }

    @Test
    void addSkill_CallsRepository_WhenSkillLevelIsValid() {
        service.addSkill("johnDoe", "Java", "Expert");

        verify(repo).addSkill("johnDoe", "Java", "Expert");
    }

    @Test
    void addSkill_ThrowsException_WhenSkillLevelIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addSkill("johnDoe", "Java", "Master"));

        verify(repo, never()).addSkill(any(), any(), any());
    }

    @Test
    void removeSkill_CallsRepository() {
        service.removeSkill("johnDoe", "Java");

        verify(repo).removeSkill("johnDoe", "Java");
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
    void getAllSkills_ReturnsListFromRepository() {
        List<Skill> skills = List.of(
                new Skill("Java", 1),
                new Skill("SQL", 2)
        );

        when(repo.getAllSkills()).thenReturn(skills);

        List<Skill> result = service.getAllSkills();

        assertEquals(skills, result);
        verify(repo).getAllSkills();
    }

    @Test
    void getAllSkills_ReturnsEmptyListIfNoneExist() {
        when(repo.getAllSkills()).thenReturn(List.of());

        List<Skill> result = service.getAllSkills();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repo).getAllSkills();
    }

    @Test
    void getSkillFromAllSkills_ReturnsCorrectSkill() {
        Skill java = new Skill("Java", 1);
        Skill sql = new Skill("SQL", 2);
        List<Skill> allSkills = List.of(java, sql);

        when(repo.getSkillFromAllSkills(allSkills, "Java")).thenReturn(java);

        Skill result = service.getSkillFromAllSkills(allSkills, "Java");

        assertEquals(java, result);
        verify(repo).getSkillFromAllSkills(allSkills, "Java");
    }

    @Test
    void getSkillFromAllSkills_ReturnsNullIfSkillNotFound() {
        Skill java = new Skill("Java", 1);
        List<Skill> allSkills = List.of(java);

        when(repo.getSkillFromAllSkills(allSkills, "Python")).thenReturn(null);

        Skill result = service.getSkillFromAllSkills(allSkills, "Python");

        assertNull(result);
        verify(repo).getSkillFromAllSkills(allSkills, "Python");
    }

    @Test
    void assignSkillToEmployee_CallsRepositoryAndReturnsValue() {
        when(repo.assignSkillToEmployee(1, "johnDoe", "Expert")).thenReturn(1);

        int result = service.assignSkillToEmployee(1, "johnDoe", "Expert");

        assertEquals(1, result);
        verify(repo).assignSkillToEmployee(1, "johnDoe", "Expert");
    }

    @Test
    void unassignSkillFromEmployee_CallsRepositoryAndReturnsValue() {
        when(repo.unassignTaskFromEmployee(1, "johnDoe", "Expert")).thenReturn(1);

        int result = service.unassignSkillFromEmployee(1, "johnDoe", "Expert");

        assertEquals(1, result);
        verify(repo).unassignTaskFromEmployee(1, "johnDoe", "Expert");
    }

    @Test
    void getSkillByID_ReturnsSkillFromRepository() {
        Skill skill = new Skill("Java", 1);

        when(repo.getSkillByID(1)).thenReturn(skill);

        Skill result = service.getSkillByID(1);

        assertEquals(skill, result);
        verify(repo).getSkillByID(1);
    }

    @Test
    void getSkillByID_ReturnsNullIfSkillDoesNotExist() {
        when(repo.getSkillByID(99)).thenReturn(null);

        Skill result = service.getSkillByID(99);

        assertNull(result);
        verify(repo).getSkillByID(99);
    }


}
