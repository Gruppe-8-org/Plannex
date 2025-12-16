package com.plannex.Model;

import java.util.ArrayList;
import java.util.List;

public class SkillDTO {
    private List<EmployeeSkill> skillRows;

    public SkillDTO() {
        skillRows = new ArrayList<>();
    }

    public SkillDTO(List<EmployeeSkill> skillRows) {
        this.skillRows = skillRows;
    }

    public List<EmployeeSkill> getSkillRows() {
        return skillRows;
    }

    public void setSkillRows(List<EmployeeSkill> skillRows) {
        this.skillRows = skillRows;
    }
}
