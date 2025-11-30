package com.plannex.UMLAutomation;

import com.plannex.Repository.ProjectEmployeeRepository;
import com.plannex.Repository.ProjectRepository;
import com.plannex.Repository.TaskRepository;

public class Main {
    private static final MethodDumper methodDumper = new MethodDumper();

    public static void main(String[] args) {
        System.out.println("ProjectRepository:");
        methodDumper.dumpMethodsFromClass(ProjectRepository.class);
        System.out.println("ProjectEmployeeRepository:");
        methodDumper.dumpMethodsFromClass(ProjectEmployeeRepository.class);
        System.out.println("TaskRepository:");
        methodDumper.dumpMethodsFromClass(TaskRepository.class);
    }
}
