package com.plannex;

import com.plannex.Controller.ProjectEmployeeController;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Service.AuthAndPermissionsService;
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
    @MockitoBean
    AuthAndPermissionsService authAndPermissionsService;

    private MockHttpSession sessionWithUser(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        return session;
    }

    @Test
    void showAddProjectEmployeeDisplaysIfDoneByManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/employees/add-employee").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("create_user"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddProjectEmployeeDoesNotDisplayIfNotDoneByManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/employees/add-employee").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attributeExists("message"))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddProjectEmployeeRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/employees/add-employee"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
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
    void showProfileShowsProfileIfUserWithUsernameExistsAndIsLoggedIn() throws Exception {
        ProjectEmployee nonExistentEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(nonExistentEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("employee", nonExistentEmployee))
                .andExpect(model().attribute("isOwnerOrManager", false))
                .andExpect(view().name("project_worker_page"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showProfileDisplaysErrorPageIfNoUserWithUsername() throws Exception {
        when(projectEmployeeService.getEmployeeByUsername("hj2451")).thenThrow(new EntityDoesNotExistException("No employee with username hj2451 exists."));
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/employees/hj2451").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No employee with username hj2451 exists."))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2451");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showProfileRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/employees/hj2451"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showAllUsersRoutesCorrectlyWhenLoggedIn() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/employees").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("allUsers"))
                .andExpect(view().name("teams_users_depts"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAllUsersRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void editEmployeeWorksWithExistingEmployeeEqualToTarget() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(true);

        mockMvc.perform(get("/employees/hj2450/edit").session(sessionWithUser("hj2450")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", existingEmployee))
                .andExpect(view().name("edit_user"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2450".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2450".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editEmployeeWorksWithManager() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/employees/hj2450/edit").session(sessionWithUser("hj2450")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", existingEmployee))
                .andExpect(view().name("edit_user"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2450".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "hj2450".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editEmployeeThrowsOnNonExistentEmployee() throws Exception {
        when(projectEmployeeService.getEmployeeByUsername("hj2451")).thenThrow(new EntityDoesNotExistException(("No employee with username hj2451 exists.")));
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/employees/hj2451/edit").session(sessionWithUser("hj2451")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No employee with username hj2451 exists."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editEmployeeThrowsOnNonOwner() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450/edit").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may edit employee information."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editEmployeeThrowsOnNonOwnerAndNonManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450/edit").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may edit employee information."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editEmployeeRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/employees/hj2450/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
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
    void showDeletePageRoutesCorrectlyOnOwner() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(true);

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2450")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", existingEmployee))
                .andExpect(view().name("delete_user"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2450".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2450".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeletePageRoutesCorrectlyOnManager() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2451")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", existingEmployee))
                .andExpect(view().name("delete_user"));

        verify(projectEmployeeService, times(1)).getEmployeeByUsername("hj2450");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeletePageThrowsOnNonManagerAndNonOwner() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may delete employee information."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeletePageThrowsOnNonManager() throws Exception {
        ProjectEmployee existingEmployee = new ProjectEmployee("hj2450", "Hans Jørgen", "HJE@gmail.com", "abcdefgh", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        when(projectEmployeeService.getEmployeeByUsername("hj2450")).thenReturn(existingEmployee);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may delete employee information."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeletePageThrowsOnNeitherManagerNorOwner() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(false);

        mockMvc.perform(get("/employees/hj2450/delete").session(sessionWithUser("hj2451")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers or profile owners may delete employee information."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isOwnerOfAccount(eq("hj2450"), argThat(s -> "hj2451".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeletePageThrowsOnNonExistentUser() throws Exception {
        when(projectEmployeeService.getEmployeeByUsername("MRY")).thenThrow(new EntityDoesNotExistException("No employee with username MRY exists."));
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);
        when(authAndPermissionsService.isOwnerOfAccount(any(), any())).thenReturn(true);

        mockMvc.perform(get("/employees/MRY/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No employee with username MRY exists."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteEmployeeRoutesCorrectlyAndDeletesRightValue() throws Exception {
        mockMvc.perform(post("/employees/hj2450/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));
        verify(projectEmployeeService, times(1)).deleteEmployeeByUsername("hj2450");
    }
}
