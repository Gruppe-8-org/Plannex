package com.plannex.Model;

import java.time.LocalDate;
import java.util.Objects;

public class Project {
    private int ID;
    private String projectTitle;
    private String projectDescription;
    private LocalDate projectStart;
    private LocalDate projectEnd;

    public Project(int id, String title, String description, LocalDate start, LocalDate end) {
        ID = id;
        this.projectTitle = title;
        this.projectDescription = description;
        this.projectStart = start;
        this.projectEnd = end;
    }

    public Project() {

    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public void setProjectStart(LocalDate projectStart) {
        this.projectStart = projectStart;
    }

    public void setProjectEnd(LocalDate projectEnd) {
        this.projectEnd = projectEnd;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public LocalDate getProjectStart() {
        return projectStart;
    }

    public LocalDate getProjectEnd() {
        return projectEnd;
    }

    public int getID() {
        return this.ID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return ID == project.ID && Objects.equals(projectTitle, project.projectTitle) && Objects.equals(projectDescription, project.projectDescription) && Objects.equals(projectStart, project.projectStart) && Objects.equals(projectEnd, project.projectEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, projectTitle, projectDescription, projectStart, projectEnd);
    }
}
