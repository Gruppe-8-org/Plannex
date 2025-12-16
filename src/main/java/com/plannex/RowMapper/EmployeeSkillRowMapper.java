package com.plannex.RowMapper;

import com.plannex.Model.EmployeeSkill;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EmployeeSkillRowMapper implements RowMapper<EmployeeSkill> {

    @Override
    public EmployeeSkill mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new EmployeeSkill(
                rs.getString("EmployeeUsername"),
                rs.getString("SkillTitle"),
                rs.getString("SkillLevel")
        );
    }
}
