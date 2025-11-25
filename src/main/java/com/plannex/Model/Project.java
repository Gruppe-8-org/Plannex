package com.plannex.Model;

import java.time.LocalDate;
import java.util.Objects;

public class Project {
    private String projectTitle;
    private String projectDescription;
    private LocalDate projectStart;
    private LocalDate projectEnd;

    public Project(String title, String description, LocalDate start, LocalDate end) {
        this.projectTitle = title;
        this.projectDescription = description;
        this.projectStart = start;
        this.projectEnd = end;
    }


    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public LocalDate getProjectStart() {
        return projectStart;
    }

    public void setProjectStart(LocalDate projectStart) {
        this.projectStart = projectStart;
    }

    public LocalDate getProjectEnd() {
        return projectEnd;
    }

    public void setProjectEnd(LocalDate projectEnd) {
        this.projectEnd = projectEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(projectTitle, project.projectTitle) && Objects.equals(projectDescription, project.projectDescription) && Objects.equals(projectStart, project.projectStart) && Objects.equals(projectEnd, project.projectEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectTitle, projectDescription, projectStart, projectEnd);
    }
}
