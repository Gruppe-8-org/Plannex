package com.plannex.RowMapper;

import com.plannex.Model.Project;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProjectRowMapper implements RowMapper<Project> {
    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Project(
                rs.getInt("ProjectID"),
                rs.getString("ProjectTitle"),
                rs.getString("ProjectDescription").replace("\\n", "\n"), // Shouldn't have added linebreaks manually
                rs.getDate("ProjectStart").toLocalDate(),
                rs.getDate("ProjectEnd").toLocalDate()
        );
    }
}
