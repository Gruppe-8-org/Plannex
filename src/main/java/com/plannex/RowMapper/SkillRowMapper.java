package com.plannex.RowMapper;

import com.plannex.Model.Skill;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SkillRowMapper implements RowMapper<Skill> {

    @Override
    public Skill mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Skill(
                rs.getString("SkillTitle"),
                rs.getInt("SkillID")

        );
    }
}
