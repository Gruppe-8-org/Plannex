package com.plannex;

import com.plannex.Controller.TaskController;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.Repository.TaskRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTests {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    TaskService taskService;
    @MockitoBean
    AuthAndPermissionsService authAndPermissionsService;
    @MockitoBean
    ProjectEmployeeService projectEmployeeService;
    @MockitoBean
    ProjectService projectService;

    private MockHttpSession sessionWithUser(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        return session;
    }

    @Test
    void showAddTaskRoutesCorrectlyOnSufficientPermissions() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/add-task").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("task"))
                .andExpect(view().name("add_task"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddTaskThrowsOnNonManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/add-task").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may add tasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddTaskRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/add-task"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveTaskRedirectsOnSuccessAndHasRightValues() throws Exception {
        mockMvc.perform(post("/projects/1/add-task")
                .param("parentTaskID", "0")
                .param("taskTitle", "Title")
                .param("taskDescription", "Description")
                .param("taskStart", "2025-11-12")
                .param("taskEnd", "2025-11-12")
                .param("taskDurationHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));
        verify(taskService, times(1)).addTask(new Task(0, 1, 0, "Title", "Description", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f));
    }

    @Test
    void showAddSubtaskRoutesCorrectlyOnSufficientPermissions() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/2/add-subtask").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("subtask"))
                .andExpect(view().name("add_subtask"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddSubtaskThrowsOnNonManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/tasks/1/add-subtask").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may add subtasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddSubtaskRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/add-subtask"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveSubtaskRedirectsOnSuccess() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/add-subtask")
                        .param("taskTitle", "Title")
                        .param("taskDescription", "Description")
                        .param("taskStart", "2025-11-12")
                        .param("taskEnd", "2025-11-12")
                        .param("taskDurationHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1"));
        verify(taskService, times(1)).addSubtask(new Task(0, 1, 1, "Title", "Description", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f));
    }

    @Test
    void showAddDependencyRoutesCorrectlyOnLoggedIn() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/add-dependency").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("allTasks"))
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(view().name("add_dependencies"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddDependencyRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/add-dependency"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveAddDependencyWorks() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/add-dependency")
                .param("blockedByTaskIDs", "1", "3", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).addFollowsDependency(2, 1);
        verify(taskService, times(1)).addFollowsDependency(2, 3);
        verify(taskService, times(1)).addFollowsDependency(2, 4);
    }

    @Test
    void deleteDependencyRedirectsAfterDeletion() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/dependencies/delete-4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/5"));

        verify(taskService, times(1)).deleteFollowsDependency(5, 4);
    }

    @Test
    void showAssingmentPageWorksWithManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/5/assign-workers").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("allWorkers"))
                .andExpect(model().attributeExists("assigneeDTO"))
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(view().name("add_assignee"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAssingmentPageThrowsWithWorker() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/5/assign-workers").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may assign workers tasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAssignmentPageRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/5/assign-workers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveAssignmentRedirectsAndPostsCorrectValues() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/assign-workers")
                .param("allUsers", "lildawg", "marqs", "bigdawg")
                .param("usernames", "lildawg", "marqs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/5"));

        verify(taskService, times(1)).assignTaskToEmployee(5, "lildawg");
        verify(taskService, times(1)).assignTaskToEmployee(5, "marqs");
        verify(taskService, times(0)).assignTaskToEmployee(5, "bigdawg"); // Not selected
    }

    @Test
    void saveAssignmentRemovesAssignementsRemoved() throws Exception {
        List<ProjectEmployee> prev = List.of(new ProjectEmployee("lildawg", "Max-Emil", "MES@gmail.com", "fAbc#21Y", LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0)));
        when(taskService.getAllAssigneesForSubtask(5)).thenReturn(prev);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/assign-workers")
                        .param("allUsers", "lildawg", "marqs", "bigdawg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/5"));

        verify(taskService, never()).assignTaskToEmployee(anyInt(), eq("lildawg"));
        verify(taskService, times(1)).unassignTaskFromEmployee(5, "lildawg");
    }

    @Test
    void unassignWorkerWorksWithManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/unassign-worker/lildawg").session(sessionWithUser("MRY")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/5"));

        verify(taskService, times(1)).unassignTaskFromEmployee(5, "lildawg");
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void unassignWorkerThrowsWithWorker() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/unassign-worker/lildawg").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may unassign workers."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void unassignWorkerRedirectsOnMissingLogin() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/unassign-worker/lildawg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showTaskPageHasRightAttrsAndRoutesAsExpected() throws Exception {
        Task task = new Task(1, 1, 0, "Project startup", "Building a good foundation for the actual work to come later.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 13), 22.667f);
        when(taskService.getTaskByID(1)).thenReturn(task);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("subtasks"))
                .andExpect(model().attributeExists("assignees"))
                .andExpect(model().attributeExists("timeSpent"))
                .andExpect(model().attributeExists("artifacts"))
                .andExpect(model().attributeExists("dependencies"))
                .andExpect(model().attributeExists("subtaskAssignees"))
                .andExpect(model().attributeExists("subtaskTimeSpents"))
                .andExpect(model().attributeExists("isManager"))
                .andExpect(model().attributeExists("sessionUser"))
                .andExpect(view().name("task_window"));

        verify(taskService, times(1)).getTaskByID(1);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showTaskPageRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showSubtaskPageHasRightAttrsAndRoutesAsExpected() throws Exception {
        Task sub = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(taskService.getTaskByID(2)).thenReturn(sub);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("subtask"))
                .andExpect(model().attributeExists("assignees"))
                .andExpect(model().attributeExists("dependencies"))
                .andExpect(model().attributeExists("artifacts"))
                .andExpect(model().attributeExists("sessionUser"))
                .andExpect(model().attributeExists("isManager"))
                .andExpect(view().name("subtask_window"));

        verify(taskService, times(1)).getTaskByID(2);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showSubtaskPageRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2").session(sessionWithUser("MRY")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showEditSubtaskPageRoutesCorrectlyOnValidPermissions() throws Exception {
        Task sub = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(2)).thenReturn(sub);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isManager", true))
                .andExpect(model().attribute("subtask", sub))
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(view().name("edit_subtask"));

        verify(taskService, times(1)).getTaskByID(2);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(2)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString()))); // Passed to template
    }

    @Test
    void showEditSubtaskPageThrowsOnNonManager() throws Exception {
        Task sub = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(2)).thenReturn(sub);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may edit subtasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showEditSubtaskPageRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/edit").session(sessionWithUser("MRY")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void updateSubtaskRoutesAndRedirectsCorrectlyAndPassesRightValues() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/edit")
                        .param("ID", "2")
                        .param("parentProjectID", "1")
                        .param("parentTaskID", "1")
                        .param("taskTitle", "Title")
                        .param("taskDescription", "Description")
                        .param("taskStart", "2025-11-12")
                        .param("taskEnd", "2025-11-12")
                        .param("taskDurationHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).updateTask(new Task(2, 1, 1, "Title", "Description", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f), 2);
    }

    @Test
    void showEditTaskPageRoutesCorrectlyOnValidCredentials() throws Exception {
        Task t = new Task(1, 1, 0, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(1)).thenReturn(t);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("task", t))
                .andExpect(view().name("edit_task"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(2)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString()))); // Also passed to template.
    }

    @Test
    void showEditTaskThrowsOnNonManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/tasks/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may edit tasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showEditTaskRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void updateTaskRoutesAndRedirectsCorrectly() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/edit")
                        .param("ID", "2")
                        .param("parentProjectID", "1")
                        .param("parentTaskID", "0")
                        .param("taskTitle", "Title")
                        .param("taskDescription", "Description")
                        .param("taskStart", "2025-11-12")
                        .param("taskEnd", "2025-11-12")
                        .param("taskDurationHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1"));

        verify(taskService, times(1)).updateTask(new Task(2, 1, 0, "Title", "Description", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f), 2);
    }

    @Test
    void showAddTimeContributionFormRoutesCorrectlyWhenLoggedIn() throws Exception {
        Task t = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(2)).thenReturn(t);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/contribute-time").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(model().attribute("subtaskTitle", "Set up GitHub project"))
                .andExpect(view().name("add_time_contribution"));

        verify(taskService, times(1)).getTaskByID(2);
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddTimeContributionRedirectsToLoginIfNoSession() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/contribute-time"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveTimeContributionWorksAsExpected() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/contribute-time").session(sessionWithUser("MRY"))
                .param("timeSpent", "2.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).contributeTime(eq("MRY"), eq(2), eq(2.0f));
    }

    @Test
    void deleteTimeContributionWorksAsExpectedWhenOwnerDoesIt() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-time-contribution").session(sessionWithUser("MRY"))
                .param("byEmployee", "MRY")
                .param("when", "2025-11-12T10:00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));

        verify(taskService, times(1)).deleteTimeContribution(eq("MRY"), eq(2), eq(LocalDateTime.of(2025, 11, 12, 10, 0, 0)));
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteTimeContributionWorksAsExpectedWhenManagerDoesIt() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-time-contribution").session(sessionWithUser("lildawg"))
                        .param("byEmployee", "MRY")
                        .param("when", "2025-11-12T10:00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));

        verify(taskService, times(1)).deleteTimeContribution(eq("MRY"), eq(2), eq(LocalDateTime.of(2025, 11, 12, 10, 0, 0)));
        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "lildawg".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "lildawg".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteTimeContributionRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-time-contribution")
                        .param("byEmployee", "MRY")
                        .param("when", "2025-11-12T10:00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void deleteTimeContributionThrowsOnWorkerTryingToDeleteOtherWorkersTimeContribution() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-time-contribution").session(sessionWithUser("MRY"))
                        .param("byEmployee", "lildawg")
                        .param("when", "2025-11-12T10:00:00"))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Managers may delete all time contributions, workers may only delete their own."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteArtifactWorksAsExpectedOnManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-artifact").session(sessionWithUser("MRY"))
                    .param("author", "MRY")
                    .param("pathToArtifact", "/src/main/resources/example_artifact.pdf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).deleteArtifact(2, "MRY", "/src/main/resources/example_artifact.pdf");

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteArtifactWorksAsExpectedOnAuthor() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-artifact").session(sessionWithUser("MRY"))
                        .param("author", "MRY")
                        .param("pathToArtifact", "/src/main/resources/example_artifact.pdf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).deleteArtifact(2, "MRY", "/src/main/resources/example_artifact.pdf");

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteArtifactThrowsOnWorkerTryingToDeleteAnothersArtifact() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-artifact").session(sessionWithUser("MRY"))
                        .param("author", "lildawg")
                        .param("pathToArtifact", "/src/main/resources/example_artifact.pdf"))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Managers may delete all artifacts, workers may only delete their own."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void deleteArtifactRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete-artifact")
                        .param("author", "lildawg")
                        .param("pathToArtifact", "/src/main/resources/example_artifact.pdf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showDeleteSubtaskRoutesAsExpectedOnManagerSession() throws Exception {
        Task sub = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(2)).thenReturn(sub);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(model().attribute("title", "Set up GitHub project"))
                .andExpect(model().attributeExists("description"))
                .andExpect(model().attribute("start", LocalDate.of(2025, 11, 12)))
                .andExpect(model().attribute("end", LocalDate.of(2025, 11, 12)))
                .andExpect(model().attribute("mainEntityType", "subtask"))
                .andExpect(model().attribute("whereToSubmit", "/projects/1/tasks/1/subtasks/2/delete"))
                .andExpect(model().attribute("whereToGoOnCancel", "/projects/1/tasks/1/subtasks/2"))
                .andExpect(view().name("delete_main_entity"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeleteSubtaskThrowsOnNonManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may delete subtasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeleteSubtaskRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void deleteSubtaskCallsServiceAndRoutesCorrectly() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1"));
        verify(taskService, times(1)).deleteTaskByID(2);
    }

    @Test
    void showAddArtifactRoutesCorrectlyOnLoggedInUser() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/add-artifact").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pid", 1))
                .andExpect(model().attribute("tid", 1))
                .andExpect(model().attribute("sid", 2))
                .andExpect(view().name("add_artefact"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddArtifactRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/add-artifact").session(sessionWithUser("MRY")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveArtifactCallsServiceAndRoutesAsExpected() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/add-artifact")
                .param("byUsername", "MRY")
                .param("pathToArtifact", "path/to/artifact"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).addArtifact(2, "MRY", "path/to/artifact");
    }

    @Test
    void showDeleteTaskRoutesCorrectlyOnManager() throws Exception {
        Task t = new Task(2, 1, 1, "A title", "A description", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 4.0f);
        when(taskService.getTaskByID(1)).thenReturn(t);
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sessionUser", "MRY"))
                .andExpect(model().attribute("title", "A title"))
                .andExpect(model().attribute("description", "A description"))
                .andExpect(model().attribute("start", LocalDate.of(2025, 11, 12)))
                .andExpect(model().attribute("end", LocalDate.of(2025, 11, 12)))
                .andExpect(model().attribute("mainEntityType", "task"))
                .andExpect(model().attribute("whereToSubmit", "/projects/1/tasks/1/delete"))
                .andExpect(model().attribute("whereToGoOnCancel", "/projects/1/tasks/1"))
                .andExpect(view().name("delete_main_entity"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeleteTaskThrowsOnNonManager() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);
        when(authAndPermissionsService.isManager(any())).thenReturn(false);

        mockMvc.perform(get("/projects/1/tasks/1/delete").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may delete tasks."))
                .andExpect(view().name("error"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
        verify(authAndPermissionsService, times(1)).isManager(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showDeleteTaskRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void deleteTaskRoutesCallsServiceAndRoutesAsExpected() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));
        verify(taskService, times(1)).deleteTaskByID(1);
    }

    @Test
    void showAddDependencyTaskRoutesCorrectlyIfLoggedIn() throws Exception {
        when(authAndPermissionsService.isLoggedIn(any())).thenReturn(true);

        mockMvc.perform(get("/projects/1/tasks/1/add-dependency").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(view().name("add_dependencies_task"));

        verify(authAndPermissionsService, times(1)).isLoggedIn(argThat(s -> "MRY".equals(s.getAttribute("username").toString())));
    }

    @Test
    void showAddDependencyTaskRedirectsOnNotLoggedIn() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/add-dependency"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void saveDependencyTaskRoutesCallsServiceAndRoutesAsExpected() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/add-dependency")
                .param("blockedByTaskIDs", "2", "3", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1"));
        verify(taskService, times(1)).addFollowsDependency(1, 2);
        verify(taskService, times(1)).addFollowsDependency(1, 3);
        verify(taskService, times(1)).addFollowsDependency(1, 4);
        // Used invalid IDs in this test (2 is a subtask, for instance),
        // but user will only be able to choose among actual tasks, not subtasks.
    }

    @Test
    void saveDependencyRemovesDependenciesNotInSubmittedList() throws Exception {
        when(taskService.getAllDependenciesForTask(1))
                .thenReturn(List.of(
                        new TaskRepository.ConstPair<>(1, 2),
                        new TaskRepository.ConstPair<>(1, 3),
                        new TaskRepository.ConstPair<>(1, 4)
                ));
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/1/add-dependency")
                        .param("blockedByTaskIDs", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/1"));

        verify(taskService, never()).addFollowsDependency(1, 2);
        verify(taskService, times(1)).deleteFollowsDependency(1, 3);
        verify(taskService, times(1)).deleteFollowsDependency(1, 4);
    }



}
