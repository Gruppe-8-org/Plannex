package com.plannex.RowMapper;

import com.plannex.Model.ProjectEmployee;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProjectEmployeeRowMapper implements RowMapper<ProjectEmployee> {
    @Override
    public ProjectEmployee mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProjectEmployee(
                rs.getString("EmployeeUsername"),
                rs.getString("EmployeeName"),
                rs.getString("EmployeeEmail"),
                rs.getString("EmployeePassword"),
                rs.getTime("EmployeeWorkingHoursFrom").toLocalTime(),
                rs.getTime("EmployeeWorkingHoursTo").toLocalTime()
        );
    }
}
