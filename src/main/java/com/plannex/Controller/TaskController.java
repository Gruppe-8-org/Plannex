package com.plannex.Controller;

import com.plannex.Exception.InsufficientPermissionsException;
import com.plannex.Model.AssigneeFormDTO;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.Service.AuthAndPermissionsService;
import com.plannex.Service.ProjectEmployeeService;
import com.plannex.Service.ProjectService;
import com.plannex.Service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.naming.OperationNotSupportedException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{pid}")
public class TaskController {
    private final TaskService taskService;
    private final AuthAndPermissionsService authAndPermissionsService;
    private final ProjectService projectService;
    private final ProjectEmployeeService projectEmployeeService;

    public TaskController(TaskService taskService, AuthAndPermissionsService authAndPermissionsService, ProjectService projectService, ProjectEmployeeService projectEmployeeService) {
        this.taskService = taskService;
        this.authAndPermissionsService = authAndPermissionsService;
        this.projectEmployeeService = projectEmployeeService;
        this.projectService = projectService;
    }

    @GetMapping("/add-task")
    public String showAddTask(@PathVariable int pid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may add tasks.");
        }

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
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may add subtasks.");
        }

        Task sub = new Task();
        sub.setParentProjectID(pid);
        sub.setParentTaskID(tid);

        model.addAttribute("parentProjectID", pid);
        model.addAttribute("parentTaskID", tid);
        model.addAttribute("subtask", sub);
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_subtask";
    }

    @PostMapping("/tasks/{tid}/add-subtask")
    public String saveSubtask(@PathVariable int pid, @PathVariable int tid, @ModelAttribute Task task) throws OperationNotSupportedException {
        task.setParentProjectID(pid);
        task.setParentTaskID(tid);
        taskService.addSubtask(task);
        return "redirect:/projects/" + pid + "/tasks/" + tid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/add-dependency")
    public String showAddDependency(
            @PathVariable int pid,
            @PathVariable int tid,
            @PathVariable int sid,
            Model model,
            HttpSession session) {

        if (!authAndPermissionsService.isLoggedIn(session)) return "redirect:/login";

        model.addAttribute("allTasks", taskService.getAllSubtasksForParentTask(tid));
        model.addAttribute("sid", sid);
        model.addAttribute("tid", tid);
        model.addAttribute("pid", pid);
        model.addAttribute("sessionUser", session.getAttribute("username").toString());

        return "add_dependencies";
    }


    @PostMapping("/tasks/{tid}/subtasks/{sid}/add-dependency")
    public String saveDependency(
            @PathVariable int pid,
            @PathVariable int tid,
            @PathVariable int sid,
            @RequestParam(name="blockedByTaskIDs") List<Integer> blockedByTaskIDs)
            throws OperationNotSupportedException {

        for (Integer blockedID : blockedByTaskIDs) {
            taskService.addFollowsDependency(sid, blockedID);
        }

        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }


    @GetMapping("/tasks/{tid}/add-dependency")
    public String showAddDependencyTask(@PathVariable int pid, @PathVariable String tid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("allTasks", projectService.getAllTasksForProject(pid));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_dependencies_task";
    }

    @PostMapping("/tasks/{tid}/add-dependency")
    public String saveDependencyTask(@PathVariable int tid, @PathVariable String pid, @RequestParam(name="blockedByTaskIDs") List<Integer> blockedByTaskIDs) {
        for (Integer blockedByTaskID : blockedByTaskIDs) {
            taskService.addFollowsDependency(tid, blockedByTaskID);
        }

        return "redirect:/projects/" + pid + "/tasks/" + tid;
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/dependencies/delete-{blockedBy}")
    public String deleteDependency(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @PathVariable int blockedBy) {
        taskService.deleteFollowsDependency(sid, blockedBy);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/assign-workers")
    public String showAddAssignment(HttpSession session, Model model,
                                    @PathVariable int pid,
                                    @PathVariable int tid,
                                    @PathVariable int sid) {

        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may assign workers tasks.");
        }

        AssigneeFormDTO formData = new AssigneeFormDTO();
        formData.addUsernamesFromList(taskService.getAllAssigneesForSubtask(sid));
        model.addAttribute("allWorkers", projectEmployeeService.getAllWorkers());
        model.addAttribute("pid", pid);
        model.addAttribute("tid", tid);
        model.addAttribute("sid", sid);
        model.addAttribute("assigneeDTO", formData);
        model.addAttribute("sessionUser", session.getAttribute("username").toString());

        return "add_assignee";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/assign-workers")
    public String saveAssignments(@PathVariable int pid,
                                  @PathVariable int tid,
                                  @PathVariable int sid,
                                  @ModelAttribute AssigneeFormDTO formData) {
        formData.setPreviousAssignees(
                (HashSet<String>) taskService.getAllAssigneesForSubtask(sid).stream().map(ProjectEmployee::getEmployeeUsername).collect(Collectors.toSet())
        );
        Set<String> usernamesToAdd = formData.getUsernames().stream().filter(u -> !formData.getPreviousAssignees().contains(u)).collect(Collectors.toSet());
        Set<String> usernamesToRemove = formData.getPreviousAssignees().stream().filter(pu -> !formData.getUsernames().contains(pu)).collect(Collectors.toSet());
        for (String username : usernamesToAdd) {
            taskService.assignTaskToEmployee(sid, username);
        }

        for (String username : usernamesToRemove) {
            taskService.unassignTaskFromEmployee(sid, username);
        }

        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }


    @PostMapping("/tasks/{tid}/subtasks/{sid}/unassign-worker/{username}")
    public String unassignWorker(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @PathVariable String username, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may unassign workers.");
        }

        taskService.unassignTaskFromEmployee(sid, username);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/edit")
    public String showEditSubtasks(@PathVariable String pid, @PathVariable String tid, @PathVariable int sid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may edit subtasks.");
        }

        model.addAttribute("isManager", authAndPermissionsService.isManager(session));
        model.addAttribute("subtask", taskService.getTaskByID(sid));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "edit_subtask";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/edit")
    public String updateSubtask(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @ModelAttribute Task task) {
        taskService.updateTask(task, task.getID());
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/add-artifact")
    public String showAddArtifactPage(
            @PathVariable int pid,
            @PathVariable int tid,
            @PathVariable int sid,
            HttpSession session,
            Model model) {

        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("pid", pid);
        model.addAttribute("tid", tid);
        model.addAttribute("sid", sid);
        model.addAttribute("sessionUser", session.getAttribute("username"));
        return "add_artefact";
    }


    @PostMapping("/tasks/{tid}/subtasks/{sid}/add-artifact")
    public String saveArtifact(
            @PathVariable int pid,
            @PathVariable int tid,
            @PathVariable int sid,
            @RequestParam("byUsername") String byUsername,
            @RequestParam("pathToArtifact") String pathToArtifact) {
      
        taskService.addArtifact(sid, byUsername, pathToArtifact);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }


    @GetMapping("/tasks/{tid}")
    public String showTaskPage(@PathVariable String pid, @PathVariable int tid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("task", taskService.getTaskByID(tid));
        model.addAttribute("subtasks", taskService.getAllSubtasksForParentTask(tid));
        model.addAttribute("assignees", taskService.getAllAssigneesForTask(tid));
        model.addAttribute("timeSpent", taskService.getAllTimeContributionsForTask(tid).stream().mapToDouble(f -> f).sum());
        model.addAttribute("artifacts", taskService.getAllArtifactsForTask(tid));
        model.addAttribute("dependencies", taskService.getAllDependenciesForTask(tid));
        model.addAttribute("subtaskAssignees", taskService.getAllSubtasksForParentTask(tid).stream().map(task -> taskService.getAllAssigneesForSubtask(task.getID())).toList());
        model.addAttribute("subtaskTimeSpents", taskService.getAllSubtasksForParentTask(tid).stream().map(task -> taskService.getAllTimeContributionsForSubtask(task.getID()).stream().mapToDouble(f -> f).sum()).toList());
        model.addAttribute("isManager", authAndPermissionsService.isManager(session));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "task_window";
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}")
    public String showSubtaskPage(@PathVariable String pid, @PathVariable String tid, @PathVariable int sid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("subtask", taskService.getTaskByID(sid));
        model.addAttribute("artifacts", taskService.getAllArtifactsForTask(sid));
        model.addAttribute("dependencies", taskService.getAllDependenciesForTask(sid));
        model.addAttribute("assignees", taskService.getAllAssigneesForSubtask(sid));
        model.addAttribute("timeSpents", taskService.getAllTimeContributionsForSubtask(sid).stream().mapToDouble(f -> f).sum());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        model.addAttribute("isManager", authAndPermissionsService.isManager(session));
        return "subtask_window";
    }

    @GetMapping("/tasks/{tid}/edit")
    public String showEditTaskPage(@PathVariable int pid, @PathVariable int tid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may edit tasks.");
        }

        model.addAttribute("isManager", authAndPermissionsService.isManager(session));
        model.addAttribute("task", taskService.getTaskByID(tid));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "edit_task";
    }

    @PostMapping("/tasks/{tid}/edit")
    public String updateTask(@PathVariable int pid, @PathVariable int tid, @ModelAttribute Task task) {
        taskService.updateTask(task, task.getID());
        return "redirect:/projects/" + pid + "/tasks/" + tid;
    }

    @GetMapping("/tasks/{tid}/delete")
    public String showDeleteTask(@PathVariable int pid, @PathVariable int tid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may delete tasks.");
        }

        Task t = taskService.getTaskByID(tid);
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        model.addAttribute("title", t.getTaskTitle());
        model.addAttribute("description", t.getTaskDescription());
        model.addAttribute("start", t.getTaskStart());
        model.addAttribute("end", t.getTaskEnd());
        model.addAttribute("mainEntityType", "task");
        model.addAttribute("whereToSubmit", "/projects/" + pid + "/tasks/" + tid + "/delete");
        model.addAttribute("whereToGoOnCancel", "/projects/" + pid + "/tasks/" + tid);
        return "delete_main_entity";
    }

    @PostMapping("/tasks/{tid}/delete")
    public String deleteTask(@PathVariable int pid, @PathVariable int tid) {
        taskService.deleteTaskByID(tid);
        return "redirect:/projects/" + pid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/delete")
    public String showDeleteSubtask(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may delete subtasks.");
        }

        Task s = taskService.getTaskByID(sid);
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        model.addAttribute("title", s.getTaskTitle());
        model.addAttribute("description", s.getTaskDescription());
        model.addAttribute("start", s.getTaskStart());
        model.addAttribute("end", s.getTaskEnd());
        model.addAttribute("mainEntityType", "subtask");
        model.addAttribute("whereToSubmit", "/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid + "/delete");
        model.addAttribute("whereToGoOnCancel", "/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid);
        return "delete_main_entity";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/delete")
    public String deleteSubtask(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid) {
        taskService.deleteTaskByID(sid);
        return "redirect:/projects/" + pid + "/tasks/" + tid;
    }

    @GetMapping("/tasks/{tid}/subtasks/{sid}/contribute-time")
    public String showTimeContributionForm(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, HttpSession session, Model model) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("subtaskTitle", taskService.getTaskByID(sid).getTaskTitle());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_time_contribution";
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/contribute-time")
    public String saveTimeContribution(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, HttpSession session, @RequestParam("timeSpent") float timeSpent) {
        taskService.contributeTime(session.getAttribute("username").toString(), sid, timeSpent);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/delete-time-contribution")
    public String deleteTimeContribution(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, HttpSession session, @RequestParam("byEmployee") String byUser, @RequestParam("when") LocalDateTime when) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session) && !byUser.equals(session.getAttribute("username").toString())) {
            throw new InsufficientPermissionsException("Managers may delete all time contributions, workers may only delete their own.");
        }

        taskService.deleteTimeContribution(byUser, sid, when);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }

    @PostMapping("/tasks/{tid}/subtasks/{sid}/delete-artifact")
    public String deleteArtifact(@PathVariable int pid, @PathVariable int tid, @PathVariable int sid, @RequestParam("author") String author, @RequestParam("pathToArtifact") String pathToArtifact, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        String requestingUser = session.getAttribute("username").toString();

        if (!authAndPermissionsService.isManager(session) && !requestingUser.equals(author)) {
            throw new InsufficientPermissionsException("Managers may delete all artifacts, workers may only delete their own.");
        }

        taskService.deleteArtifact(sid, session.getAttribute("username").toString(), pathToArtifact);
        return "redirect:/projects/" + pid + "/tasks/" + tid + "/subtasks/" + sid;
    }
}
