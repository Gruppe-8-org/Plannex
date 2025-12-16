package com.plannex.Controller;

import com.plannex.Repository.ProjectEmployeeRepository;
import com.plannex.Service.ProjectEmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// This entire class is stolen from the sessions example
// from week 41.
// https://github.com/EK-DATA-2SEM-PROGSYSTEK/DATA-GBG-F25A-B/tree/master/Uge%2041/4.gang
@Controller
public class SessionController {
    private final ProjectEmployeeService projectEmployeeService;


    public SessionController(ProjectEmployeeService projectEmployeeService) {
        this.projectEmployeeService = projectEmployeeService;
    }

    @GetMapping("login")
    public String showLogin() {
        return "login_page";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String pw,
                        HttpSession session,
                        Model model) {

        if (projectEmployeeService.login(username, pw)) {
            session.setAttribute("username", username);

            model.addAttribute("employee", projectEmployeeService.getEmployeeByUsername(username));
            model.addAttribute("username", username);
            if (projectEmployeeService.getEmployeePermissions(username).equals("Manager")) {
                return "project_leader_page";
            }

            return "project_worker_page";
        }

        model.addAttribute("wrongCredentials", true);
        return "login_page";
    }

    @GetMapping("logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "login_page";
    }
}
