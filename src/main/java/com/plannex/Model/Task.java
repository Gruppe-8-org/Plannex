package com.plannex.Model;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private int ID;
    private int parentProjectID;
    private int parentTaskID;
    private String taskTitle;
    private String taskDescription;
    private LocalDate taskStart;
    private LocalDate taskEnd;
    private float taskDurationHours;

    public Task(int ID, int parentProjectID, int parentTaskID, String title, String description, LocalDate taskStart, LocalDate taskEnd, float durationHours) {
        this.ID = ID;
        this.parentProjectID = parentProjectID;
        this.parentTaskID = parentTaskID;
        this.taskTitle = title;
        this.taskDescription = description;
        this.taskStart = taskStart;
        this.taskEnd = taskEnd;
        this.taskDurationHours = durationHours;
    }

    public Task() {}

    public void setTaskDurationHours(float taskDurationHours) {
        this.taskDurationHours = taskDurationHours;
    }

    public void setTaskEnd(LocalDate taskEnd) {
        this.taskEnd = taskEnd;
    }

    public void setTaskStart(LocalDate taskStart) {
        this.taskStart = taskStart;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setParentTaskID(int parentTaskID) {
        this.parentTaskID = parentTaskID;
    }

    public void setParentProjectID(int parentProjectID) {
        this.parentProjectID = parentProjectID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getParentProjectID() {
        return parentProjectID;
    }


    public int getParentTaskID() {
        return parentTaskID;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public LocalDate getTaskStart() {
        return taskStart;
    }

    public LocalDate getTaskEnd() {
        return taskEnd;
    }

    public float getTaskDurationHours() {
        return taskDurationHours;
    }

    public int getID() {
        return this.ID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return ID == task.ID && parentProjectID == task.parentProjectID && parentTaskID == task.parentTaskID && Float.compare(taskDurationHours, task.taskDurationHours) == 0 && Objects.equals(taskTitle, task.taskTitle) && Objects.equals(taskDescription, task.taskDescription) && Objects.equals(taskStart, task.taskStart) && Objects.equals(taskEnd, task.taskEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, parentProjectID, parentTaskID, taskTitle, taskDescription, taskStart, taskEnd, taskDurationHours);
    }
}
