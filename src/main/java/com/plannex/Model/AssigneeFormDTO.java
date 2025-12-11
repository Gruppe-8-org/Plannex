package com.plannex.Model;

import java.util.HashSet;
import java.util.List;

// Perhaps premature optimization with HashSet
public class AssigneeFormDTO {
    private HashSet<String> usernames;
    private HashSet<String> previousAssignees;

    public AssigneeFormDTO(HashSet<String> usernames, HashSet<String> previousAssignees) {
        this.usernames = usernames;
        this.previousAssignees = previousAssignees;
    }

    public AssigneeFormDTO() {
        this.usernames = new HashSet<>();
        this.previousAssignees = new HashSet<>();
    }

    public void addUsernamesFromList(List<ProjectEmployee> employees) {
        for (ProjectEmployee employee : employees) {
            usernames.add(employee.getEmployeeUsername());
        }
    }

    public void addPreviousAssigneesFromList(List<ProjectEmployee> employees) {
        for (ProjectEmployee employee : employees) {
            previousAssignees.add(employee.getEmployeeUsername());
        }
    }

    public HashSet<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(HashSet<String> usernames) {
        this.usernames = usernames;
    }

    public HashSet<String> getPreviousAssignees() {
        return previousAssignees;
    }

    public void setPreviousAssignees(HashSet<String> previousAssignees) {
        this.previousAssignees = previousAssignees;
    }
}
