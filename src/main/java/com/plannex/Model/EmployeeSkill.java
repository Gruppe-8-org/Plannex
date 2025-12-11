package com.plannex.Model;

public class EmployeeSkill {
        private String employeeUsername;
        private String skillTitle;
        private String skillLevel;
    private int skillId;

    public EmployeeSkill(String employeeUsername, String skillTitle, String skillLevel, int skillId) {
        this.employeeUsername = employeeUsername;
        this.skillTitle = skillTitle;
        this.skillLevel = skillLevel;
        this.skillId = skillId;
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

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }
}
