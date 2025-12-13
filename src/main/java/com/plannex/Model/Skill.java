package com.plannex.Model;

public class Skill {
    private int skillId;
    private String skillTitle;

    public Skill(String skillTitle, int skillId) {
        this.skillTitle = skillTitle;
        this.skillId = skillId;
    }

    public Skill() {

    }

    public String getSkillTitle() {
        return skillTitle;
    }

    public void setSkillTitle(String skillTitle) {
        this.skillTitle = skillTitle;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }
}

