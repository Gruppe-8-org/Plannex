package com.plannex;

import com.plannex.Controller.ProjectEmployeeController;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Service.ProjectEmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectEmployeeController.class)
public class ProjectEmployeeControllerTests {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProjectEmployeeService projectEmployeeService;

    private MockHttpSession sessionWithUser(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        return session;
    }

    @Test
    void addProjectEmployeeAddsEmployeeIfDoneByManager() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");

        mockMvc.perform(get("/employees/add-employee").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("create_user"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void addProjectEmployeeDoesNotAddEmployeeIfNotDoneByManager() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");

        mockMvc.perform(get("/employees/add-employee").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attributeExists("message"))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void addProjectEmployeeActuallyAddsAValidEmployeeAfterValidPermissions() throws Exception {
        ProjectEmployee nonExistentEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(nonExistentEmployee);
        mockMvc.perform(post("/employees/add-employee")
                        .param("employeeUsername", "hj2450")
                        .param("employeeName", "Hans Jørgen")
                        .param("employeeEmail", "HJE@gmail.com")
                        .param("employeePassword", "abcdefgh")
                        .param("workingHoursFrom", "08:00:00")
                        .param("workingHoursTo", "16:00:00")
                        .param("permissions", "Worker"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));
        verify(projectEmployeeService, times(1)).addEmployee(nonExistentEmployee, "Worker");
    }

    @Test
    void showProfileShowsProfileIfUserWithUsernameExists() throws Exception {
        ProjectEmployee nonExistentEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(nonExistentEmployee);
        when(projectEmployeeService.getPermissions("hj2450")).thenReturn("Worker");

        mockMvc.perform(get("/employees/hj2450").session(sessionWithUser("hj2450")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("isOwnerOrManager"))
                .andExpect(view().name("project_worker_page"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
    }

    @Test
    void showProfileDisplaysErrorPageIfNoUserWithUsername() throws Exception {
        when(projectEmployeeService.getEmployeeByUsername("hj2451")).thenThrow(new EntityDoesNotExistException("No employee with username hj2451 exists."));

        mockMvc.perform(get("/employees/hj2451").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No employee with username hj2451 exists."))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2451");
    }

    @Test
    void showAllUsersRoutesCorrectly() throws Exception {
        mockMvc.perform(get("/employees").session(sessionWithUser("MRY")))
                .andExpect(model().attributeExists("allUsers"))
                .andExpect(view().name("teams_users_depts"));
    }

    @Test
    void editEmployeeWorksWithExistingEmployeeAndTarget() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(projectEmployeeService.getPermissions("hj2450")).thenReturn("Worker");

        mockMvc.perform(get("/employees/hj2450/edit").session(sessionWithUser("hj2450")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", existingEmployee))
                .andExpect(view().name("edit_user"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
    }

    @Test
    void editEmployeeThrowsOnNonExistentEmployee() throws Exception {
        when(projectEmployeeService.getPermissions("hj2451")).thenThrow(new EntityDoesNotExistException(("No user with username hj2451 exists.")));

        mockMvc.perform(get("/employees/hj2451/edit").session(sessionWithUser("hj2451")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No user with username hj2451 exists."))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getPermissions("hj2451");
    }

    @Test
    void editEmployeeThrowsOnNonOwner() throws Exception {
        when(projectEmployeeService.getPermissions("hj2451")).thenReturn("Worker");
        mockMvc.perform(get("/employees/hj2450/edit").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may edit employee information."))
                .andExpect(view().name("error"));
    }

    @Test
    void saveEditedEmployeeRedirectsAfterStoringIfNewUsernameDoesNotExist() throws Exception {
        ProjectEmployee nonExistentEmployee = new ProjectEmployee("hj2451", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(nonExistentEmployee);
        mockMvc.perform(post("/employees/hj2450/edit")
                        .param("employeeUsername", "hj2451")
                        .param("employeeName", "Hans Jørgen")
                        .param("employeeEmail", "HJE@gmail.com")
                        .param("employeePassword", "abcdefgh")
                        .param("workingHoursFrom", "08:00:00")
                        .param("workingHoursTo", "16:00:00")
                        .param("permissions", "Worker")
                        .param("oldUsername", "hj2450"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/hj2451"));
        verify(projectEmployeeService, times(1)).updateEmployee(nonExistentEmployee, "hj2450");
    }

    @Test
    void showDeletePageRoutesCorrectly() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(projectEmployeeService.getPermissions("hj2450")).thenReturn("Worker");

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2450")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", existingEmployee))
                .andExpect(view().name("delete_user"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
    }

    @Test
    void showDeletePageThrowsOnNonManagerOrNonOwner() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(projectEmployeeService.getPermissions("hj2451")).thenReturn("Worker");

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may delete employee information."))
                .andExpect(view().name("error"));
    }

    @Test
    void showDeletePageThrowsOnNonExistentUser() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenThrow(new EntityDoesNotExistException("No user with username MRY exists."));

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No user with username MRY exists."))
                .andExpect(view().name("error"));
    }

    @Test
    void deleteEmployeeRoutesCorrectly() throws Exception {
        mockMvc.perform(post("/employees/hj2450/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));
        verify(projectEmployeeService, times(1)).deleteEmployeeByUsername("hj2450");
    }
}
