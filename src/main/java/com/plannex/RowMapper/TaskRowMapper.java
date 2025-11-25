package com.plannex.RowMapper;

import com.plannex.Model.Task;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class TaskRowMapper implements RowMapper<Task> {
    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Task(
                rs.getInt("ProjectID"),
                rs.getInt("ParentTaskID"),
                rs.getString("TaskTitle"),
                rs.getString("TaskDescription").replace("\\n", "\n"),
                rs.getDate("TaskStart").toLocalDate(),
                rs.getDate("TaskEnd").toLocalDate(),
                rs.getFloat("TaskDurationHours")
        );
    }
}
