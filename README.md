# Plannex
Plannex (plan-nexus) is a project management service written as a web app. It allows managers to monitor progress and resource use, and to manage employees. Workers may contribute to tasks and share artefacts.

## Building and running the project
**Prerequisites**
1. A Java SDK 21 implementation
2. Maven 3.99 or greater
3. Spring Boot 4 (JDBC 4, Spring- (JDBC)Session 4)
4. Thymeleaf 3.1.3
5. MySQL 8.0.44 (MySQL-connector 9.5.0)

**Building and running - No IDE**
1. Clone the source code by writing `git clone https://github.com/Gruppe-8-org/Plannex/` in a terminal in a working directory of your choosing
2. Build the project with `mvn [clean] install`. You can skip compiling and running tests by appending `-Dmaven.test.skip=true` to the previous command (add a space between the two segments)
4. Run the executable with `java plannex`
5. Navigate to `localhost:8080/login` and start planning

**Building and running with IntelliJ IDEA 2025.2.4**
1. Clone the source code as in step 1 above, but in `~/IdeaProjects`.
2. Open IDEA
3. Hit run (the green play button)
4. Navigate to `localhost:8080/login` and start planning

Should you wish to not run a local instance, navigate to (http://plannex-a4cxfubygbhpcedv.norwayeast-01.azurewebsites.net) and start planning! This requires internet access.

See [Contributing](contributing.md) for policies on contribution.
