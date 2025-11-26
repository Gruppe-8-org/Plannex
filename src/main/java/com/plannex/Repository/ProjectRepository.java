package com.plannex.Repository;

import com.plannex.Exception.EntityDoesNotExistException;
import com.plannex.Model.Project;
import com.plannex.RowMapper.ProjectRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ProjectRepository {
    protected final JdbcTemplate jdbcTemplate;
    protected final ProjectRowMapper projectRowMapper;

    public ProjectRepository(JdbcTemplate jdbcTemplate, ProjectRowMapper projectRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectRowMapper = projectRowMapper;
    }

    public int addProject(Project project) {
            return jdbcTemplate.update("INSERT INTO Projects (ProjectTitle, ProjectDescription, ProjectStart, ProjectEnd) VALUES (?, ?, ?, ?);",
                    project.getProjectTitle(), project.getProjectDescription(), project.getProjectStart(), project.getProjectEnd());
    }

    public Project getProjectByID(int projectID) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Projects WHERE ProjectID = ?;", projectRowMapper, projectID);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public List<Project> getAllProjects() {
        try {
            return jdbcTemplate.query("SELECT * FROM Projects;", projectRowMapper);
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public int updateProject(Project modifiedProject, int targetProjectID) {
        if (getProjectByID(targetProjectID) == null) {
            throw new EntityDoesNotExistException("No project with projectID " + targetProjectID + " exists.");
        }

        return jdbcTemplate.update(
                "UPDATE Projects " +
                        "SET ProjectTitle = ?, ProjectDescription = ?, ProjectStart = ?, ProjectEnd = ?" +
                "WHERE ProjectID = ?;",
                modifiedProject.getProjectTitle(), modifiedProject.getProjectDescription(), modifiedProject.getProjectStart(),
                modifiedProject.getProjectEnd(), targetProjectID
        );
    }

    @Transactional
    public int deleteProjectByID(int projectID) {
        if (getProjectByID(projectID) == null) {
            throw new EntityDoesNotExistException("No project with projectID " + projectID + " exists.");
        }

        return jdbcTemplate.update("DELETE FROM Projects WHERE ProjectID = ?;", projectID);
    }
}
