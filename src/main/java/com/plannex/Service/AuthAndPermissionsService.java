package com.plannex.Service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthAndPermissionsService {
    private final ProjectEmployeeService projectEmployeeService;

    public AuthAndPermissionsService(ProjectEmployeeService projectEmployeeService) {
        this.projectEmployeeService = projectEmployeeService;
    }

    public boolean isOwnerOfAccount(String username, HttpSession session) {
        return session.getAttribute("username").toString().equals(username);
    }

    public boolean isManager(HttpSession session) {
        String username = session.getAttribute("username").toString();
        return projectEmployeeService.getPermissions(username).equals("Manager");
    }

    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("username") != null;
    }
}
