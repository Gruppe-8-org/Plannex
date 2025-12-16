package com.plannex;

import com.plannex.Controller.ProjectController;
import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.Service.ProjectEmployeeService;
import com.plannex.Service.ProjectService;
import com.plannex.Service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    private TaskService taskService;

    private MockHttpSession sessionWithUser(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        return session;
    }

    @Test
    void addProjectWithManagerRedirectsToAddProject() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");

        mockMvc.perform(get("/projects/add-project").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(view().name("add_project_window"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void addProjectWithNonManagerUserRedirectsToProjectsWithError() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");

        mockMvc.perform(get("/projects/add-project").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attributeExists("message"))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void saveProjectWorksAsExpected() throws Exception {
        Project aProject = new Project(5, "Test Project", "An empty project for testing.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        mockMvc.perform(post("/projects/add-project")
                .param("Title", aProject.getProjectTitle())
                .param("Description", aProject.getProjectDescription())
                .param("Start", aProject.getProjectStart().toString())
                .param("End", aProject.getProjectEnd().toString()))
                .andExpect(status().is3xxRedirection()) // would be 201 for an API without browser interaction
                .andExpect(redirectedUrl("/projects"));
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectService, times(1)).addProject(captor.capture());
    }

    @Test
    void displayProjectsShowsAllProjectsAndReturnsExpectedStatus() throws Exception {
        List<Project> allProjects = List.of(
                new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17)),
                new Project(2, "Coffee machine repairs on the second floor", "The coffee machine has been broken for a grueling three days now.\nCalls to the repairman revealed that we could do this ourselves to save money.\nShould the machine remain in disrepair, none of our projects will be released on schedule.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 13)),
                new Project(3, "Secret Santa but in the Danish way", "Christmas is around the corner and it is an office tradition.\nHere, employees sign up to torment others and to being tormented by others.\nWarning: Extreme pranks (razor blades hidden in otherwise delicious fudge, irritants placed on toilet paper, ordering colleagues to write valid tar commands without access to the manual pages, etc.) will subject you to disciplinary action.", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20)),
                new Project(4, "Calculator SaaS", "Our customers desparately want a calculator stored in the cloud, this is our answer to their prayers.\nIn essence, it is an expression lexer, parser and evaluator. It is to support:\n* Parenthesized expressions\n* User-defined and built in functions\n* Testable components", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8))
        );

        when(projectService.getAllProjects()).thenReturn(allProjects);
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");

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
    }

    @Test
    void getProjectDisplaysDesiredProjectIfExists() throws Exception {
        Project aProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        when(projectService.getProjectByID(1)).thenReturn(aProject);
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");

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
    }

    @Test
    void getProjectShowsErrorPageIfPageDoesNotExist() throws Exception {
        when(projectService.getProjectByID(-1)).thenThrow(new EntityDoesNotExistException("No project with projectID -1 exists."));

        mockMvc.perform(get("/projects/-1").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No project with projectID -1 exists."))
                .andExpect(view().name("error"));

        verify(projectService, times(1)).getProjectByID(-1);
    }

    @Test
    void editProjectWorksAsExpectedWithSufficientPermissions() throws Exception {
        Project aProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        when(projectService.getProjectByID(1)).thenReturn(aProject);

        mockMvc.perform(get("/projects/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("sessionUser"))
                .andExpect(view().name("edit_project_window"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
        verify(projectService, times(1)).getProjectByID(1);
    }

    @Test
    void editProjectRedirectsOnInsufficientPermissions() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");

        mockMvc.perform(get("/projects/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may edit projects."))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void editProjectRedirectsOnInvalidTargetProjectID() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        when(projectService.getProjectByID(-1)).thenThrow(new EntityDoesNotExistException("No project with projectID -1 exists."));

        mockMvc.perform(get("/projects/-1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("message", "No project with projectID -1 exists."))
                .andExpect(view().name("error"));

        verify(projectService, times(1)).getProjectByID(-1);
    }

    @Test
    void deleteProjectWorksAsExpectedWithSufficientPermissions() throws Exception {
        Project aProject = new Project(1, "The Plannex Project", "A project planning tool for our customer.\nIs to allow splitting of projects into tasks with subtasks.\nNice to have features would be GANTT chart generation and resource management", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 12, 17));
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        when(projectService.getProjectByID(1)).thenReturn(aProject);

        mockMvc.perform(get("/projects/1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(view().name("delete_main_entity"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void deleteProjectRedirectsOnInsufficientPermissions() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");

        mockMvc.perform(get("/projects/1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may delete projects."))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void deleteProjectRedirectsOnInvalidTargetProjectID() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");

        mockMvc.perform(get("/projects/-1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isNotFound())
                .andExpect(model().attributeExists("message"))
                .andExpect(view().name("error"));

        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }
}