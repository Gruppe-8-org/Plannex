package com.plannex.Controller;

import com.plannex.Exception.InsufficientPermissionsException;
import com.plannex.Model.Project;
import com.plannex.Model.Task;
import com.plannex.Service.AuthAndPermissionsService;
import com.plannex.Service.ProjectService;
import com.plannex.Service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final AuthAndPermissionsService authAndPermissionsService;
    private final TaskService taskService;

    @Autowired
    public ProjectController(ProjectService projectService, AuthAndPermissionsService authAndPermissionsService, TaskService taskService) {
        this.projectService = projectService;
        this.authAndPermissionsService = authAndPermissionsService;
        this.taskService = taskService;
    }

    @GetMapping()
    public String displayProjectsPage(Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Project> allProjects = projectService.getAllProjects();
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("employeesInvolved", allProjects.stream().map(project -> projectService.getAllInvolved(project.getID())).toList());
        model.addAttribute("startDates", allProjects.stream().map(project -> project.getProjectStart().toString()).toList());
        model.addAttribute("timeSpents", allProjects.stream().map(project -> projectService.getTotalTimeSpent(project.getID())).toList());
        model.addAttribute("endDates", allProjects.stream().map(project -> project.getProjectEnd().toString()).toList());
        model.addAttribute("isManager", authAndPermissionsService.isManager(session));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "projects_window";
    }

    @GetMapping("/{pid}")
    public String getProject(@PathVariable int pid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Task> allTasks = projectService.getAllTasksForProject(pid);
        model.addAttribute("project", projectService.getProjectByID(pid));
        model.addAttribute("allTasks", allTasks);
        model.addAttribute("timeSpent", projectService.getTotalTimeSpent(pid));
        model.addAttribute("taskAssignees", allTasks.stream().map(task -> taskService.getAllAssigneesForTask(task.getID())).toList());
        model.addAttribute("taskTimeContributions", allTasks.stream().map(task -> taskService.getAllTimeContributionsForTask(task.getID()).stream().mapToDouble(f -> f).sum()).toList());
        model.addAttribute("isManager", authAndPermissionsService.isManager(session));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "project_window";
    }

    @GetMapping("/add-project")
    public String addProject(Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may add projects.");
        }

        model.addAttribute("project", new Project());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "add_project_window";
    }

    @PostMapping("/add-project")
    public String saveProject(@ModelAttribute Project project) {
        projectService.addProject(project);
        return "redirect:/projects";
    }

    @GetMapping("/{pid}/edit")
    public String editProject(@PathVariable int pid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may edit projects.");
        }

        model.addAttribute("project", projectService.getProjectByID(pid));
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "edit_project_window";
    }

    @PostMapping("/{pid}/edit")
    public String updateProject(@ModelAttribute Project editedProject, @PathVariable int pid) {
        projectService.updateProject(editedProject, pid);
        return "redirect:/projects/" + pid;
    }

    @GetMapping("/{pid}/delete")
    public String deleteProject(@PathVariable int pid, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may delete projects.");
        }

        Project p = projectService.getProjectByID(pid);
        model.addAttribute("ID", pid);
        model.addAttribute("title", p.getProjectTitle());
        model.addAttribute("description", p.getProjectDescription());
        model.addAttribute("start", p.getProjectStart());
        model.addAttribute("end", p.getProjectEnd());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        model.addAttribute("mainEntityType", "project");
        model.addAttribute("whereToSubmit", "/projects/" + pid + "/delete");
        model.addAttribute("whereToGoOnCancel", "/projects/" + pid);
        return "delete_main_entity";
    }

    @PostMapping("/{pid}/delete")
    public String actuallyDeleteProject(@PathVariable int pid) {
        projectService.deleteProjectByID(pid);
        return "redirect:/projects";
    }
}
