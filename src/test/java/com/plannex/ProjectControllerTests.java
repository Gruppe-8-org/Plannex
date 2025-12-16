package com.plannex;

import com.plannex.Controller.ProjectController;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.Service.AuthAndPermissionsService;
import com.plannex.Service.ProjectEmployeeService;
import com.plannex.Service.ProjectService;
import com.plannex.Service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProjectEmployeeService projectEmployeeService;
    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private AuthAndPermissionsService authAndPermissionsService;
    @MockitoBean
    private TaskService taskService;

    private MockHttpSession sessionWithUser(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        return session;
    }

    @Test
    void addProjectWithManagerRedirectsToAddProject() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/add-project").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(view().name("add_project_window"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void addProjectWithNonManagerUserRedirectsToError() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/add-project").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may add projects."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void addProjectRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/add-project"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveProjectWorksAsExpected() throws Exception {
        Project aProject = new Project(5, "Test Project", "An empty project for testing.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        mockMvc.perform(post("/projects/add-project")
                .param("projectTitle", aProject.getProjectTitle())
                .param("projectDescription", aProject.getProjectDescription())
                .param("projectStart", aProject.getProjectStart().toString())
                .param("projectEnd", aProject.getProjectEnd().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));
        verify(projectService, times(1)).addProject(new Project(0, "Test Project", "An empty project for testing.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17)));
    }

    @Test
    void displayProjectsShowsAllProjectsAndReturnsExpectedStatusOnLoggedIn() throws Exception {
        List<Project> allProjects = List.of(
                new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17)),
                new Project(2, "Coffee machine repairs on the second floor", "The coffee machine has been broken for a grueling three days now.\nCalls to the repairman revealed that we could do this ourselves to save money.\nShould the machine remain in disrepair, none of our projects will be released on schedule.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 13)),
                new Project(3, "Secret Santa but in the Danish way", "Christmas is around the corner and it is an office tradition.\nHere, employees sign up to torment others and to being tormented by others.\nWarning: Extreme pranks (razor blades hidden in otherwise delicious fudge, irritants placed on toilet paper, ordering colleagues to write valid tar commands without access to the manual pages, etc.) will subject you to disciplinary action.", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20)),
                new Project(4, "Calculator SaaS", "Our customers desparately want a calculator stored in the cloud, this is our answer to their prayers.\nIn essence, it is an expression lexer, parser and evaluator. It is to support:\n* Parenthesized expressions\n* User-defined and built in functions\n* Testable components", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8))
        );

        when(projectService.getAllProjects()).thenReturn(allProjects);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("allProjects"))
                .andExpect(model().attribute("allProjects", allProjects))
                .andExpect(model().attributeExists("employeesInvolved"))
                .andExpect(model().attributeExists("startDates"))
                .andExpect(model().attributeExists("endDates"))
                .andExpect(model().attributeExists("isManager"))
                .andExpect(view().name("projects_window"));

        verify(projectService, times(1)).getAllProjects();
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void displayProjectsRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getProjectDisplaysDesiredProjectIfExistsAndIsLoggedIn() throws Exception {
        Project aProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        when(projectService.getProjectByID(1)).thenReturn(aProject);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("project", aProject))
                .andExpect(model().attributeExists("allTasks"))
                .andExpect(model().attributeExists("timeSpent"))
                .andExpect(model().attributeExists("taskAssignees"))
                .andExpect(model().attributeExists("taskTimeContributions"))
                .andExpect(model().attributeExists("isManager"))
                .andExpect(view().name("project_window"));

        verify(projectService, times(1)).getProjectByID(1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void getProjectRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getProjectShowsErrorPageIfPageDoesNotExist() throws Exception {
        when(projectService.getProjectByID(-1)).thenThrow(new EntityDoesNotExistException("No project with projectID -1 exists."));
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/-1").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No project with projectID -1 exists."))
                .andExpect(view().name("error"));

        verify(projectService, times(1)).getProjectByID(-1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editProjectWorksAsExpectedWithSufficientPermissionsValidID() throws Exception {
        Project aProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        when(projectService.getProjectByID(1)).thenReturn(aProject);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("project", aProject))
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(view().name("edit_project_window"));

        verify(projectService, times(1)).getProjectByID(1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editProjectRedirectsOnInsufficientPermissions() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may edit projects."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editProjectRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void editProjectRedirectsOnInvalidTargetProjectID() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);
        when(projectService.getProjectByID(-1)).thenThrow(new EntityDoesNotExistException("No project with projectID -1 exists."));

        mockMvc.perform(get("/projects/-1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No project with projectID -1 exists."))
                .andExpect(view().name("error"));

        verify(projectService, times(1)).getProjectByID(-1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void editProjectRoutesCorrectlyAndPassesValidArgs() throws Exception {
        mockMvc.perform(post("/projects/1/edit")
                        .param("projectTitle", "A title")
                        .param("projectDescription", "A description")
                        .param("projectStart", "2025-11-12")
                        .param("projectEnd", "2025-12-17"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));
        verify(projectService, times(1)).updateProject(new Project(0, "A title", "A description", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17)), 1);
    }

    @Test
    void deleteProjectWorksAsExpectedWithSufficientPermissions() throws Exception {
        Project aProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);
        when(projectService.getProjectByID(1)).thenReturn(aProject);

        mockMvc.perform(get("/projects/1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(view().name("delete_main_entity"));

        verify(projectService, times(1)).getProjectByID(1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteProjectShowsErrorOnInsufficientPermissions() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may delete projects."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteProjectRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void deleteProjectRedirectsOnInvalidTargetProjectID() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);
        when(projectService.getProjectByID(-1)).thenThrow(new EntityDoesNotExistException("No project with ID -1 exists."));

        mockMvc.perform(get("/projects/-1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No project with ID -1 exists."))
                .andExpect(view().name("error"));

        verify(projectService, times(1)).getProjectByID(-1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteProjectCallsDeleteProjectAndRoutesCorrectly() throws Exception {
        mockMvc.perform(post("/projects/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));
        verify(projectService, times(1)).deleteProjectByID(1);
    }
}