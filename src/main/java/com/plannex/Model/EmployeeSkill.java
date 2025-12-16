package com.plannex.Model;

import java.util.Objects;

public class EmployeeSkill {
        private String employeeUsername;
        private String skillTitle;
        private String skillLevel;

    public EmployeeSkill() {
    }

    public EmployeeSkill(String employeeUsername, String skillTitle, String skillLevel) {
        this.employeeUsername = employeeUsername;
        this.skillTitle = skillTitle;
        this.skillLevel = skillLevel;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public void setEmployeeUsername(String employeeUsername) {
        this.employeeUsername = employeeUsername;
    }

    public String getSkillTitle() {
        return skillTitle;
    }

    public void setSkillTitle(String skillTitle) {
        this.skillTitle = skillTitle;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeSkill that = (EmployeeSkill) o;
        return Objects.equals(skillTitle, that.skillTitle) && Objects.equals(skillLevel, that.skillLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillTitle, skillLevel);
    }
}
