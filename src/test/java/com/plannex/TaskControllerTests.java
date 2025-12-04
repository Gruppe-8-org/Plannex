package com.plannex;

import com.plannex.Controller.TaskController;
import com.plannex.Model.Task;
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
    ProjectEmployeeService projectEmployeeService;
    @MockitoBean
    ProjectService projectService;

    private MockHttpSession sessionWithUser(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", username);
        return session;
    }

    @Test
    void showAddTaskRoutesCorrectly() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        mockMvc.perform(get("/projects/1/add-task").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("task"))
                .andExpect(view().name("add_task"));
        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void showAddTaskThrowsOnNonManager() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");
        mockMvc.perform(get("/projects/1/add-task").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"));
        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void saveTaskRedirectsOnSuccess() throws Exception {
        mockMvc.perform(post("/projects/1/add-task")
                        .param("TaskID", "17")
                .param("ParentProjectID", "1")
                .param("ParentTaskID", "0")
                .param("TaskTitle", "Title")
                .param("TaskDescription", "Description")
                .param("Start", "2025-11-12")
                .param("End", "2025-11-12")
                .param("DurationInHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).addTask(captor.capture());
    }

    @Test
    void showAddSubtaskRoutesCorrectly() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        mockMvc.perform(get("/projects/1/tasks/2/add-subtask").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("subtask"))
                .andExpect(view().name("add_subtask"));
        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void showAddSubtaskThrowsOnNonManager() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");
        mockMvc.perform(get("/projects/1/tasks/1/add-subtask").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"));
        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void saveSubtaskRedirectsOnSuccess() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/add-subtask")
                        .param("ParentProjectID", "1")
                        .param("ParentTaskID", "1")
                        .param("TaskTitle", "Title")
                        .param("TaskDescription", "Description")
                        .param("Start", "2025-11-12")
                        .param("End", "2025-11-12")
                        .param("DurationInHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1"));
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).addSubtask(captor.capture());
    }

    @Test
    void showAddDependencyRoutesCorrectly() throws Exception {
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/add-dependency").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(view().name("add_dependencies"));
    }

    @Test
    void saveAddDependencyWorks() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/add-dependency")
                .param("blockedByTaskIDs", "1", "3", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        verify(taskService, times(1)).addFollowsDependency(2, 1);
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
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/5/assign-workers").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("allUsers"))
                .andExpect(view().name("add_assignee"));
        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void showAssingmentPageThrowsWithWorker() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/5/assign-workers").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"));
        verify(projectEmployeeService, times(1)).getPermissions("MRY");
    }

    @Test
    void saveAssignmentRedirects() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/assign-workers")
                .param("allUsers", "lildawg", "marqs", "bigdawg")
                .param("usernames", "lildawg", "marqs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/subtasks/5"));

        verify(taskService, times(1)).assignTaskToEmployee(5, "lildawg");
        verify(taskService, times(1)).assignTaskToEmployee(5, "marqs");
    }

    @Test
    void unassignWorkerWorksWithManager() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/unassign-worker/lildawg").session(sessionWithUser("MRY")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/5"));
        verify(taskService, times(1)).unassignTaskFromEmployee(5, "lildawg");
    }

    @Test
    void unassignWorkerThrowsWithWorker() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/5/unassign-worker/lildawg").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may unassign workers."))
                .andExpect(view().name("error"));
    }

    @Test
    void showTaskPageHasRightAttrsAndRoutesAsExpected() throws Exception {
        Task task = new Task(1, 1, 0, "Project startup", "Building a good foundation for the actual work to come later.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 13), 22.667f);
        when(taskService.getTaskByID(1)).thenReturn(task);
        mockMvc.perform(get("/projects/1/tasks/1").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("assignees"))
                .andExpect(model().attributeExists("subtasks"))
                .andExpect(model().attributeExists("timeSpent"))
                .andExpect(view().name("task_window"));
    }

    @Test
    void showSubtaskPageHasRightAttrsAndRoutesAsExpected() throws Exception {
        Task s = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(2)).thenReturn(s);
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("subtask"))
                .andExpect(model().attributeExists("assignees"))
                .andExpect(model().attributeExists("dependencies"))
                .andExpect(model().attributeExists("artifacts"));
    }

    @Test
    void showEditSubtaskPageRoutesCorrectly() throws Exception {
        Task s = new Task(2, 1, 1, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(2)).thenReturn(s);
        mockMvc.perform(get("/projects/1/tasks/1/subtasks/2/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("subtask"))
                .andExpect(view().name("edit_subtask"));
    }

    @Test
    void updateSubtaskRoutesAndRedirectsCorrectly() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/subtasks/2/edit")
                        .param("ID", "2")
                        .param("projectID", "1")
                        .param("parentTaskID", "1")
                        .param("taskTitle", "Title")
                        .param("taskDescription", "Description")
                        .param("taskStart", "2025-11-12")
                        .param("taskEnd", "2025-11-12")
                        .param("taskDurationInHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1/subtasks/2"));
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).updateTask(captor.capture(), eq(2));
    }

    @Test
    void showEditTaskPageRoutesCorrectly() throws Exception {
        Task s = new Task(1, 1, 0, "Set up GitHub project", "Go to github.com, register an organization if not already done, then create a project with title \"plannex\"\n Then create a new view for a backlog (a table) with fields title, type, progress, time estimate, and person responsible.\nFill out as we progress.", LocalDate.of(2025, 11, 12), LocalDate.of(2025, 11, 12), 0.5f);
        when(taskService.getTaskByID(1)).thenReturn(s);
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Manager");
        mockMvc.perform(get("/projects/1/tasks/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("task", s))
                .andExpect(view().name("edit_task"));
    }

    @Test
    void showEditTaskThrowsOnNonManager() throws Exception {
        when(projectEmployeeService.getPermissions("MRY")).thenReturn("Worker");
        mockMvc.perform(get("/projects/1/tasks/1/edit").session(sessionWithUser("MRY")))
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("message", "Only managers may edit tasks."))
                .andExpect(view().name("error"));
    }

    @Test
    void updateTaskRoutesAndRedirectsCorrectly() throws Exception {
        mockMvc.perform(post("/projects/1/tasks/1/edit")
                        .param("ID", "2")
                        .param("projectID", "1")
                        .param("parentTaskID", "0")
                        .param("taskTitle", "Title")
                        .param("taskDescription", "Description")
                        .param("taskStart", "2025-11-12")
                        .param("taskEnd", "2025-11-12")
                        .param("taskDurationInHours", "0.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1/tasks/1"));
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).updateTask(captor.capture(), eq(2));
    }
}
