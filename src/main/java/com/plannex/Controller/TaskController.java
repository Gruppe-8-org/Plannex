package com.plannex.Controller;

import com.plannex.Exception.InsufficientPermissionsException;
import com.plannex.Model.Task;
import com.plannex.Service.ProjectEmployeeService;
import com.plannex.Service.ProjectService;
import com.plannex.Service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.naming.OperationNotSupportedException;
import java.util.List;

@Controller
@RequestMapping("/projects/{pid}")
public class TaskController {
    private final TaskService taskService;
    private final ProjectEmployeeService projectEmployeeService;
    private final ProjectService projectService;

    public TaskController(TaskService taskService, ProjectEmployeeService projectEmployeeService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectEmployeeService = projectEmployeeService;
        this.projectService = projectService;
    }

    private boolean isManager(HttpSession session) {
        String username = session.getAttribute("username").toString();
        return projectEmployeeService.getPermissions(username).equals("Manager");
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("username") != null;
    }

    @GetMapping("/add-task")
    public String showAddTask(@PathVariable int pid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may add tasks.");
        }

        model.addAttribute("parentProjectID", pid);
        model.addAttribute("task", new Task());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_task";
    }

    @PostMapping("/add-task")
    public String saveTask(@PathVariable int pid, @ModelAttribute Task task) throws OperationNotSupportedException {
        task.setParentProjectID(pid);
        taskService.addTask(task);
        return "redirect:/projects/" + pid;
    }

    @GetMapping("/tasks/{tid}/add-subtask")
    public String showAddSubtask(@PathVariable int pid, @PathVariable int tid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may add subtasks.");
        }

        model.addAttribute("parentProjectID", pid);
        model.addAttribute("parentTaskID", tid);
        model.addAttribute("subtask", new Task());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_subtask";
    }

    @PostMapping("/tasks/{tid}/add-subtask")
    public String saveSubtask(@PathVariable int pid, @PathVariable int tid, @ModelAttribute Task task) throws OperationNotSupportedException {
        taskService.addSubtask(task);
        return "redirect:/projects/" + pid + "/tasks/" + tid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/add-dependency")
    public String showAddDependency(@PathVariable int pid, @PathVariable int sid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("allTasks", taskService.getAllSubtasksForParentTask(pid));
        model.addAttribute("blockedByTaskIDs", 0);
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_dependencies";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/add-dependency")
    public String saveDependency(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @RequestParam(name="blockedByTaskIDs") List<Integer> blockedByTaskIDs) throws OperationNotSupportedException {
        for (Integer blockedByTaskID : blockedByTaskIDs) {
            taskService.addFollowsDependency(sid, blockedByTaskID);
        }

        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/add-dependency")
    public String showAddDependencyTask(@PathVariable int pid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("allTasks", projectService.getAllTasksForProject(pid));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_dependencies_task";
    }

    @PostMapping("/tasks/{tid}/add-dependency")
    public String saveDependencyTask(@PathVariable int tid, @RequestParam(name="blockedByTaskIDs") List<Integer> blockedByTaskIDs) throws OperationNotSupportedException {
        for (Integer blockedByTaskID : blockedByTaskIDs) {
            taskService.addFollowsDependency(tid, blockedByTaskID);
        }

        return "redirect:/tasks/" + tid;
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/dependencies/delete-{blockedBy}")
    public String deleteDependency(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @PathVariable int blockedBy) {
        taskService.deleteFollowsDependency(sid, blockedBy);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/assign-workers")
    public String showAddAssignment(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may assign workers tasks.");
        }

        model.addAttribute("allUsers", projectEmployeeService.getAllEmployees());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_assignee";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/assign-workers")
    public String saveAssignments(@PathVariable int sid, @RequestParam(name="usernames") List<String> usernames) {
        for (String username : usernames) {
            taskService.assignTaskToEmployee(sid, username);
        }

        return "redirect:/subtasks/" + sid;
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/unassign-worker/{username}")
    public String unassignWorker(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @PathVariable String username, HttpSession session) {
        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may unassign workers.");
        }

        taskService.unassignTaskFromEmployee(sid, username);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/edit")
    public String showEditSubtasks(@PathVariable int sid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("subtask", taskService.getTaskByID(sid));
        return "edit_subtask";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/edit")
    public String updateSubtask(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @ModelAttribute Task task) {
        taskService.updateTask(task, task.getID());
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/add-artifact")
    public String showAddArtifactPage(HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        return "add_artefact";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/add-artifact")
    public String saveArtifact(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @RequestParam("byUsername") String byUsername, @RequestParam("pathToArtifact") String pathToArtifact) {
        taskService.addArtifact(tid, byUsername, pathToArtifact);
        final String pathToSaveAt = "artifacts/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;

        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}")
    public String showTaskPage(@PathVariable int tid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("task", taskService.getTaskByID(tid));
        model.addAttribute("subtasks", taskService.getAllSubtasksForParentTask(tid));
        model.addAttribute("assignees", taskService.getAllAssigneesForTask(tid));
        model.addAttribute("timeSpent", taskService.getAllTimeContributionsForTask(tid));
        model.addAttribute("artifacts", taskService.getAllArtifactsForTask(tid));
        model.addAttribute("dependencies", taskService.getAllDependenciesForTask(tid));
        model.addAttribute("subtaskAssignees", taskService.getAllSubtasksForParentTask(tid).stream().map(task -> taskService.getAllAssigneesForTask(task.getID())).toList());
        model.addAttribute("subtaskTimeSpents", taskService.getAllSubtasksForParentTask(tid).stream().map(task -> taskService.getAllTimeContributionsForTask(task.getID())).toList());
        return "task_window";
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}")
    public String showSubtaskPage(@PathVariable int sid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("subtask", taskService.getTaskByID(sid));
        model.addAttribute("artifacts", taskService.getAllArtifactsForTask(sid));
        model.addAttribute("dependencies", taskService.getAllDependenciesForTask(sid));
        model.addAttribute("assignees", taskService.getAllAssigneesForTask(sid));
        return "subtask_window";
    }

    @GetMapping("/tasks/{tid}/edit")
    public String showEditTaskPage(@PathVariable int pid, @PathVariable int tid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may edit tasks.");
        }

        model.addAttribute("task", taskService.getTaskByID(tid));
        return "edit_task";
    }

    @PostMapping("/tasks/{tid}/edit")
    public String updateTask(@PathVariable int pid, @PathVariable int tid, @ModelAttribute Task task) {
        taskService.updateTask(task, task.getID());
        return "redirect:/projects/" + pid + "/tasks/" + tid;
    }

    @GetMapping("/tasks/{tid}/delete")
    public String showDeleteTask(@PathVariable int tid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may delete tasks.");
        }

        model.addAttribute("task", taskService.getTaskByID(tid));
        return "delete_task_window";
    }

    @PostMapping("/tasks/{tid}/delete")
    public String deleteTask(@PathVariable int pid, @PathVariable int tid) {
        taskService.deleteTaskByID(tid);
        return "redirect:/projects/" + pid + "tasks/" + tid + "/delete";
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/delete")
    public String showDeleteSubtask(@PathVariable int sid, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may delete subtasks.");
        }

        model.addAttribute("subtask", taskService.getTaskByID(sid));
        return "delete_task_window";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}delete")
    public String deleteSubtask(@PathVariable int tid, @PathVariable int sid) {
        taskService.deleteTaskByID(sid);
        return "redirect:/projects/tasks/" + tid;
    }
}
