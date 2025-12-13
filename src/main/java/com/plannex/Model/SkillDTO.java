package com.plannex.Model;

import java.util.List;

public class SkillDTO {
    private List<String> levelRows;
    private List<Skill> skillRows;


    public SkillDTO(List<String> levelRows, List<Skill> skillRows) {
        this.levelRows = levelRows;
        this.skillRows = skillRows;

    }

    public List<Skill> getSkillRows() {
        return skillRows;
    }

    public void setSkillRows(List<Skill> skillRows) {
        this.skillRows = skillRows;
    }

    public List<String> getLevelRows() {
        return levelRows;
    }

    public void setLevelRows(List<String> levelRows) {
        this.levelRows = levelRows;
    }
}
