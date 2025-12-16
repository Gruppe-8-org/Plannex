package com.plannex.Model;

import java.util.Objects;

public class Skill {
    private String skillTitle;

    public Skill(String skillTitle) {
        this.skillTitle = skillTitle;
    }

    public Skill() {

    }

    public String getSkillTitle() {
        return skillTitle;
    }

    public void setSkillTitle(String skillTitle) {
        this.skillTitle = skillTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(skillTitle, skill.skillTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(skillTitle);
    }
}

