package com.plannex.Model;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private int parentProjectID;
    private int parentTaskID;
    private String taskTitle;
    private String taskDescription;
    private LocalDate taskStart;
    private LocalDate taskEnd;
    private float taskDurationHours;

    public Task(int parentProjectID, int parentTaskID, String title, String description, LocalDate taskStart, LocalDate taskEnd, float durationHours) {
        this.parentProjectID = parentProjectID;
        this.parentTaskID = parentTaskID;
        this.taskTitle = title;
        this.taskDescription = description;
        this.taskStart = taskStart;
        this.taskEnd = taskEnd;
        this.taskDurationHours = durationHours;
    }

    public Task() {}

    public int getParentProjectID() {
        return parentProjectID;
    }


    public int getParentTaskID() {
        return parentTaskID;
    }

    public void setParentTaskID(int parentTaskID) {
        this.parentTaskID = parentTaskID;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public LocalDate getTaskStart() {
        return taskStart;
    }

    public void setTaskStart(LocalDate taskStart) {
        this.taskStart = taskStart;
    }

    public LocalDate getTaskEnd() {
        return taskEnd;
    }

    public void setTaskEnd(LocalDate taskEnd) {
        this.taskEnd = taskEnd;
    }

    public float getTaskDurationHours() {
        return taskDurationHours;
    }

    public void setTaskDurationHours(float taskDurationHours) {
        this.taskDurationHours = taskDurationHours;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return parentProjectID == task.parentProjectID && parentTaskID == task.parentTaskID && Float.compare(taskDurationHours, task.taskDurationHours) == 0 && Objects.equals(taskTitle, task.taskTitle) && Objects.equals(taskDescription, task.taskDescription) && Objects.equals(taskStart, task.taskStart) && Objects.equals(taskEnd, task.taskEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentProjectID, parentTaskID, taskTitle, taskDescription, taskStart, taskEnd, taskDurationHours);
    }
}
