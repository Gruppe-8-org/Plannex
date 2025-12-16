package com.plannex.Controller;

import com.plannex.Exception.InsufficientPermissionsException;
import com.plannex.Model.EmployeeSkill;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Service.AuthAndPermissionsService;
import com.plannex.Model.Skill;
import com.plannex.Model.SkillDTO;
import com.plannex.Service.ProjectEmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class ProjectEmployeeController {
    private final ProjectEmployeeService projectEmployeeService;
    private final AuthAndPermissionsService authAndPermissionsService;

    public ProjectEmployeeController(ProjectEmployeeService projectEmployeeService, AuthAndPermissionsService authAndPermissionsService) {
        this.projectEmployeeService = projectEmployeeService;
        this.authAndPermissionsService = authAndPermissionsService;
    }

    @GetMapping("/add-employee")
    public String addProjectEmployee(Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may add employees.");
        }

        ProjectEmployee projectEmployee = new ProjectEmployee();
        model.addAttribute("user", projectEmployee);
        return "create_user";
    }

    @PostMapping("/add-employee")
    public String saveProjectEmployee(@ModelAttribute ProjectEmployee projectEmployee, @RequestParam("permissions") String permissions) {
        projectEmployeeService.addEmployee(projectEmployee, permissions);
        return "redirect:/employees";
    }

    @GetMapping("/{username}")
    public String showProfile(@PathVariable String username, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("employee", projectEmployeeService.getEmployeeByUsername(username));
        model.addAttribute("isOwnerOrManager", authAndPermissionsService.isManager(session) || authAndPermissionsService.isOwnerOfAccount(username, session));
        return "project_worker_page";
    }

    @GetMapping()
    public String showAllUsers(Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("allUsers", projectEmployeeService.getAllEmployees());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "teams_users_depts";
    }

    @GetMapping("/{username}/edit")
    public String editEmployee(@PathVariable String username, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!(authAndPermissionsService.isManager(session) || authAndPermissionsService.isOwnerOfAccount(username, session))) {
            throw new InsufficientPermissionsException("Only managers or profile owners may edit employee information.");
        }

        ProjectEmployee pe = projectEmployeeService.getEmployeeByUsername(username);
        model.addAttribute("user", pe);
        model.addAttribute("oldUsername", pe.getEmployeeUsername()); // FK Integrity fails without this
        return "edit_user";
    }

    @PostMapping("/{username}/edit")
    public String saveEditedEmployee(@ModelAttribute ProjectEmployee updatedEmployee, @RequestParam("oldUsername") String oldUsername, HttpSession session) {
        projectEmployeeService.updateEmployee(updatedEmployee, oldUsername);
        session.setAttribute("username", updatedEmployee.getEmployeeUsername()); // Was a very confusing bug before this
        return "redirect:/employees/" + updatedEmployee.getEmployeeUsername();
    }

    @GetMapping("/{username}/delete")
    public String showEmployeeDeletionPage(@PathVariable String username, Model model, HttpSession session) {
        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!(authAndPermissionsService.isOwnerOfAccount(username, session) || authAndPermissionsService.isManager(session))) {
            throw new InsufficientPermissionsException("Only managers or profile owners may delete employee information.");
        }

        model.addAttribute("user", projectEmployeeService.getEmployeeByUsername(username));
        return "delete_user";
    }

    @PostMapping("/{username}/delete")
    public String deleteEmployee(@PathVariable String username) {
        projectEmployeeService.deleteEmployeeByUsername(username);
        return "redirect:/employees";
    }

    @GetMapping("/{username}/assign-skills")
    public String showAddSkills(HttpSession session, Model model, @PathVariable String username) {

        if (!authAndPermissionsService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!authAndPermissionsService.isManager(session)) {
            throw new InsufficientPermissionsException("Only managers may assign workers skills.");
        }

        List<EmployeeSkill> empSkills = projectEmployeeService.getSkillsForEmployee(username);
        SkillDTO skillDTO = new SkillDTO(empSkills);
        model.addAttribute("skillDTO", skillDTO);
        model.addAttribute("allLevels", List.of("Intermediate", "Expert"));
        model.addAttribute("allUsers", projectEmployeeService.getAllEmployees());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());

        return "add_skills";
    }

    @PostMapping(value="/{username}/assign-skills", params={"addRow"})
    public String addRow(@PathVariable String username, @ModelAttribute SkillDTO skillDTO, Model model) {
        skillDTO.getSkillRows().add(new EmployeeSkill());
        skillDTO.getSkillRows().getLast().setEmployeeUsername(username);

        model.addAttribute("skillDTO", skillDTO);
        model.addAttribute("allLevels", List.of("Intermediate", "Expert"));
        model.addAttribute("sessionUser", username);
        return "add_skills";
    }

    @PostMapping(value="/{username}/assign-skills", params={"removeRow"})
    public String deleteRow(@PathVariable String username, @ModelAttribute SkillDTO skillDTO, @RequestParam String removeRow, Model model) {
        skillDTO.getSkillRows().remove(Integer.parseInt(removeRow));
        model.addAttribute("skillDTO", skillDTO);
        model.addAttribute("allLevels", List.of("Intermediate", "Expert"));
        model.addAttribute("sessionUser", username);
        return "add_skills";
    }

    @PostMapping(value="/{username}/assign-skills", params={"save"})
    public String saveAssignments(@ModelAttribute SkillDTO skillDTO, @PathVariable String username) {
        List<EmployeeSkill> skillsDesired = skillDTO.getSkillRows();
        List<EmployeeSkill> skillsCurrent = projectEmployeeService.getSkillsForEmployee(username);
        List<EmployeeSkill> skillsToAdd = skillsDesired.stream().filter(skill -> !skillsCurrent.contains(skill)).toList();
        List<EmployeeSkill> skillsToRemove = skillsCurrent.stream().filter(skill -> !skillsDesired.contains(skill)).toList();

        for (EmployeeSkill toAdd : skillsToAdd) {
            projectEmployeeService.addSkill(toAdd.getSkillTitle());
            projectEmployeeService.assignSkillToEmployee(toAdd.getSkillTitle(), username, toAdd.getSkillLevel());
        }

        for (EmployeeSkill toRemove : skillsToRemove) {
            projectEmployeeService.unassignSkillFromEmployee(toRemove.getSkillTitle(), username, toRemove.getSkillLevel());
        }

        return "redirect:/employees";
    }
}
