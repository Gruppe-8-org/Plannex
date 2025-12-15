package com.plannex.Controller;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Exception.InsufficientPermissionsException;
import com.plannex.Model.EmployeeSkill;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Skill;
import com.plannex.Model.SkillDTO;
import com.plannex.Service.ProjectEmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employees")
public class ProjectEmployeeController {
    private final ProjectEmployeeService projectEmployeeService;
    private SkillDTO skillDTO;

    public ProjectEmployeeController(ProjectEmployeeService projectEmployeeService) {
        this.projectEmployeeService = projectEmployeeService;
    }

    private ProjectEmployee getEmployeeOrThrow(String username) {
        ProjectEmployee employee = projectEmployeeService.getEmployeeByUsername(username);

        if (employee == null) {
            throw new EntityDoesNotExistException("No employee with username " + username + " exists.");
        }

        return employee;
    }

    private boolean isOwnerOrManager(String username, HttpSession session) {
        String usernameLoggedIn = session.getAttribute("username").toString();
        String permissions = projectEmployeeService.getPermissions(usernameLoggedIn);
        return permissions.equals("Manager") || usernameLoggedIn.equals(username);
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("username") != null;
    }

    @GetMapping("/add-employee")
    public String addProjectEmployee(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();

        if (!projectEmployeeService.getPermissions(username).equals("Manager")) {
            throw new InsufficientPermissionsException("Only managers may add projects.");
        }

        ProjectEmployee projectEmployee = new ProjectEmployee();
        model.addAttribute("user", projectEmployee);
        return "create_user";
    }

    @PostMapping("/add-employee")
    public String saveProjectEmployee(@ModelAttribute ProjectEmployee projectEmployee, @ModelAttribute List<Skill> allSkills, @ModelAttribute String permissions) {
        projectEmployeeService.addEmployee(projectEmployee, permissions);
        return "redirect:/employees";
    }

    @GetMapping("/{username}")
    public String showProfile(@PathVariable String username, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("employee", getEmployeeOrThrow(username));
        model.addAttribute("isOwnerOrManager", isOwnerOrManager(username, session));
        return "project_worker_page";
    }

    @GetMapping()
    public String showAllUsers(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("allUsers", projectEmployeeService.getAllEmployees());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());
        return "teams_users_depts";
    }

    @GetMapping("/{username}/edit")
    public String editEmployee(@PathVariable String username, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isOwnerOrManager(username, session)) {
            throw new InsufficientPermissionsException("Only managers or profile owners may edit employee information.");
        }

        model.addAttribute("user", getEmployeeOrThrow(username));
        model.addAttribute("oldUsername", getEmployeeOrThrow(username).getEmployeeUsername()); // FK Integrity fails without this
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
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isOwnerOrManager(username, session)) {
            throw new InsufficientPermissionsException("Only managers or profile owners may delete employee information.");
        }

        model.addAttribute("user", getEmployeeOrThrow(username));
        return "delete_user"; // Add delete plz
    }

    @PostMapping("/{username}/delete")
    public String deleteEmployee(@PathVariable String username) {
        projectEmployeeService.deleteEmployeeByUsername(username);
        return "redirect:/employees";
    }

    @GetMapping("/{username}/assign-skills")
    public String showAddSkills(HttpSession session, Model model, @PathVariable String username) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!isOwnerOrManager(username, session)) {
            throw new InsufficientPermissionsException("Only managers may assign workers skills.");
        }

        List<EmployeeSkill> empSkills = projectEmployeeService.getSkillsForEmployee(username);
        skillDTO = new SkillDTO(empSkills);
        model.addAttribute("skillDTO", skillDTO);
        model.addAttribute("allLevels", List.of("Intermediate", "Expert"));
        model.addAttribute("allUsers", projectEmployeeService.getAllEmployees());
        model.addAttribute("sessionUser", session.getAttribute("username").toString());

        return "add_skills";
    }

    @PostMapping(value="/{username}/assign-skills", params={"addRow"})
    public String addRow(@ModelAttribute SkillDTO skillDTO, Model model) {
        skillDTO.getSkillRows().add(new EmployeeSkill());
        model.addAttribute("skillDTO", skillDTO);
        return "add_skills";
    }

    @PostMapping(value="/{username}/assign-skills", params={"removeRow"})
    public String removeRow(final SkillDTO skillDTO, @ModelAttribute List<EmployeeSkill> employeeSkills) {
        //skillDTO.getSkillRows().remove();
        return "add_skills";
    }

    @PostMapping(value="/{username}/assign-skills", params={"save"})
    public String saveAssignments(@ModelAttribute SkillDTO skillDTO, @PathVariable String username) {

        for (EmployeeSkill empSkill: skillDTO.getSkillRows()){
            projectEmployeeService.assignSkillToEmployee(empSkill.getSkillId(), username, empSkill.getSkillLevel());
        }

        return "redirect:/employees";
    }

    @PostMapping("/{username}/unassign-skills")
    public String unassignWorker(@PathVariable String username, HttpSession session, @ModelAttribute EmployeeSkill assignedSkill) {
        if (!isOwnerOrManager(username, session)) {
            throw new InsufficientPermissionsException("Only managers may unassign skills.");
        }

        projectEmployeeService.unassignSkillFromEmployee(assignedSkill.getSkillId(), username, assignedSkill.getSkillLevel());
        return "redirect:/employees";
    }

}
