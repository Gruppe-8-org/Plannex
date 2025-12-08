package com.plannex.RowMapper;

import com.plannex.Model.ProjectEmployee;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

@Component
public class ProjectEmployeeRowMapper implements RowMapper<ProjectEmployee> {

    @Override
    public ProjectEmployee mapRow(ResultSet rs, int rowNum) throws SQLException {

        Time from = rs.getTime("EmployeeWorkingHoursFrom");
        Time to = rs.getTime("EmployeeWorkingHoursTo");

        return new ProjectEmployee(
                rs.getString("EmployeeUsername"),
                rs.getString("EmployeeName"),
                rs.getString("EmployeeEmail"),
                rs.getString("EmployeePassword"),

                from != null ? from.toLocalTime() : null,
                to != null ? to.toLocalTime() : null
        );
    }
}
