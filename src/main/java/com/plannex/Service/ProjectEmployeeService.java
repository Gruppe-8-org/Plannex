package com.plannex.Service;

import com.plannex.Model.EmployeeSkill;
import com.plannex.Model.ProjectEmployee;
import com.plannex.Model.Skill;
import com.plannex.Repository.ProjectEmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository projectEmployeeRepository;

    public ProjectEmployeeService(ProjectEmployeeRepository projectEmployeeRepository) {
        this.projectEmployeeRepository = projectEmployeeRepository;
    }

    public String getPermissions(String username) {
        return projectEmployeeRepository.getEmployeePermissions(username);
    }

    public int addEmployee(ProjectEmployee employee, String permissions) {
        return projectEmployeeRepository.addEmployee(employee, permissions);
    }

    public ProjectEmployee getEmployeeByUsername(String username) {
        return projectEmployeeRepository.getEmployeeByUsername(username);
    }

    public List<ProjectEmployee> getAllEmployees() {
        return projectEmployeeRepository.getAllEmployees();
    }

    public List<ProjectEmployee> getAllWorkers() {
        return projectEmployeeRepository.getAllWorkers();
    }

    public String getEmployeePermissions(String username) {
        return projectEmployeeRepository.getEmployeePermissions(username);
    }

    public int updateEmployee(ProjectEmployee updatedEmployee, String targetUsername) {
        return projectEmployeeRepository.updateEmployee(updatedEmployee, targetUsername);
    }

    public int deleteEmployeeByUsername(String username) {
        return projectEmployeeRepository.deleteEmployeeByUsername(username);
    }

    public boolean login(String username, String pw) {
        return projectEmployeeRepository.login(username, pw);
    }

    // Nedenunder er det der skal lave controller ting til

    public List<Skill> getAllSkills() {
        return projectEmployeeRepository.getAllSkills();
    }

    public Skill getSkillFromAllSkills (List<Skill> allSkills, String chosenSkill) {
        return projectEmployeeRepository.getSkillFromAllSkills(allSkills, chosenSkill);
    }

    public List<EmployeeSkill> getSkillsForEmployee(String username) {
        return projectEmployeeRepository.getSkillsForEmployee(username);
    }

    public int assignSkillToEmployee(String skillTitle, String employeeUsername, String skillLevel) {
        return projectEmployeeRepository.assignSkillToEmployee(skillTitle, employeeUsername, skillLevel);
    }

    public int unassignSkillFromEmployee(String skillTitle, String employeeUsername, String skillLevel) {
        return projectEmployeeRepository.unassignSkillFromEmployee(skillTitle, employeeUsername, skillLevel);
    }

    public Skill getSkillByTitle(String skillTitle) {
        return projectEmployeeRepository.getSkillByTitle(skillTitle);
    }

    public void addSkill(String skillTitle) {
        projectEmployeeRepository.addSkillUnlessItAlreadyExists(skillTitle);
    }

    public void removeSkill(String skillTitle) {
        projectEmployeeRepository.removeSkillIfExists(skillTitle);
    }

    private void validateSkillLevel(String level) {
        if (!level.equals("Expert") && !level.equals("Intermediate")) {
            throw new IllegalArgumentException("Skill level must be Expert or Intermediate");
        }
    }

    public float getBaseWage(String username) {
        //Har sat til de får 300 kroner i timen i stedet for månedlig løn. Det passer til vores TimeSpent schema.
        return 300.0f;
    }

    //Den her bruger 2 forskellige metoder fra repository, så den har sit eget navn i stedet for gentagende navn.
    public float calculateHourlyWage(String username) {
        float base = getBaseWage(username);

        int expertSkills = projectEmployeeRepository.countExpertSkills(username);
        int intermediateSkills = projectEmployeeRepository.countIntermediateSkills(username);

        return (float)(
                base *
                        Math.pow(1.10, expertSkills) *
                        Math.pow(1.05, intermediateSkills)
        );
    }

}
