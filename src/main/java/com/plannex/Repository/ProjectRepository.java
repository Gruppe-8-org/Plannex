package com.plannex.Repository;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Task;
import com.plannex.RowMapper.ProjectEmployeeRowMapper;
import com.plannex.RowMapper.ProjectRowMapper;
import com.plannex.RowMapper.TaskRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectRepository {
    protected final JdbcTemplate jdbcTemplate;
    protected final ProjectRowMapper projectRowMapper;
    protected final TaskRowMapper taskRowMapper;

    public ProjectRepository(JdbcTemplate jdbcTemplate, ProjectRowMapper projectRowMapper, TaskRowMapper taskRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectRowMapper = projectRowMapper;
        this.taskRowMapper = taskRowMapper;
    }

    public int addProject(Project project) {
            return jdbcTemplate.update("INSERT INTO Projects (ProjectTitle, ProjectDescription, ProjectStart, ProjectEnd) VALUES (?, ?, ?, ?);",
                    project.getProjectTitle(), project.getProjectDescription(), project.getProjectStart(), project.getProjectEnd());
    }

    public Project getProjectByIDOrThrow(int projectID) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Projects WHERE ProjectID = ?;", projectRowMapper, projectID);
        } catch (EmptyResultDataAccessException erdae) {
            throw new EntityDoesNotExistException("No project with ID " + projectID + " exists.");
        }
    }

    public List<Project> getAllProjects() {
        return jdbcTemplate.query("SELECT * FROM Projects;", projectRowMapper);
    }

    public List<Task> getAllTasksForProject(int projectID) {
        getProjectByIDOrThrow(projectID);
        return jdbcTemplate.query("SELECT * FROM Tasks WHERE ProjectID = ? AND ParentTaskID IS NULL;", taskRowMapper, projectID);
    }

    public Integer getAllInvolved(int projectID) {
        getProjectByIDOrThrow(projectID);
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT ta.EmployeeUsername) FROM TaskAssignees AS ta
                JOIN Tasks AS t ON ta.TaskID = t.TaskID
                WHERE t.ProjectID = ?""", Integer.class, projectID);
    }

    public int updateProject(Project modifiedProject, int targetProjectID) {
        getProjectByIDOrThrow(targetProjectID);
        return jdbcTemplate.update(
                "UPDATE Projects " +
                        "SET ProjectTitle = ?, ProjectDescription = ?, ProjectStart = ?, ProjectEnd = ?" +
                "WHERE ProjectID = ?;",
                modifiedProject.getProjectTitle(), modifiedProject.getProjectDescription(), modifiedProject.getProjectStart(),
                modifiedProject.getProjectEnd(), targetProjectID
        );
    }

    public int deleteProjectByID(int projectID) {
        getProjectByIDOrThrow(projectID);
        return jdbcTemplate.update("DELETE FROM Projects WHERE ProjectID = ?;", projectID);
    }

    public float getTotalTimeSpent(int projectID) {
        getProjectByIDOrThrow(projectID);
        Float result = jdbcTemplate.queryForObject("""
                
                    SELECT SUM(HoursSpent) FROM TimeSpent AS tc
                JOIN Tasks AS t ON tc.OnTaskID = t.TaskID
                WHERE t.ProjectID = ?""", Float.class, projectID);

        return result != null ? result : 0;
    }
}
