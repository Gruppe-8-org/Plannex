package com.plannex.Model;

import java.time.LocalTime;
import java.util.Objects;

public class ProjectEmployee {
    private String employeeUsername;
    private String employeeName;
    private String employeeEmail;
    private String employeePassword;
    private LocalTime workingHoursFrom;
    private LocalTime workingHoursTo;

    public ProjectEmployee(String username, String name, String email, String password, LocalTime worksFrom, LocalTime worksTo) {
        this.employeeUsername = username;
        this.employeeName = name;
        this.employeeEmail = email;
        this.employeePassword = password;
        this.workingHoursFrom = worksFrom;
        this.workingHoursTo = worksTo;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeePassword() {
        return employeePassword;
    }

    public void setEmployeePassword(String employeePassword) {
        this.employeePassword = employeePassword;
    }

    public LocalTime getWorkingHoursFrom() {
        return workingHoursFrom;
    }

    public void setWorkingHoursFrom(LocalTime workingHoursFrom) {
        this.workingHoursFrom = workingHoursFrom;
    }

    public LocalTime getWorkingHoursTo() {
        return workingHoursTo;
    }

    public void setWorkingHoursTo(LocalTime workingHoursTo) {
        this.workingHoursTo = workingHoursTo;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public void setEmployeeUsername(String employeeUsername) {
        this.employeeUsername = employeeUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEmployee that = (ProjectEmployee) o;
        return Objects.equals(employeeUsername, that.employeeUsername) && Objects.equals(employeeName, that.employeeName) && Objects.equals(employeeEmail, that.employeeEmail) && Objects.equals(employeePassword, that.employeePassword) && Objects.equals(workingHoursFrom, that.workingHoursFrom) && Objects.equals(workingHoursTo, that.workingHoursTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeUsername, employeeName, employeeEmail, employeePassword, workingHoursFrom, workingHoursTo);
    }
}
