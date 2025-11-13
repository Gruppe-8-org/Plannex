# Plannex
Plannex (plan-nexus) is a project management service written as a web app. It allows managers to monitor progress and resource use, and to manage employees. Workers may contribute to tasks and share artefacts.

## Building and running the project
**Prerequisites**
1. A Java SDK 21 implementation
2. Maven 3.99 or greater
3. MySQL Server 8.0.43 or greater - actively running as a service

**Building and running - No IDE**
1. Clone the source code by writing `git clone https://github.com/Gruppe-8-org/Plannex/` in a terminal in a working directory of your choosing
2. Build the project with `mvn [clean] install`. You can skip compiling and running tests by appending `-Dmaven.test.skip=true` to the previous command (add a space between the two segments)
4. Run the executable with `java plannex`
5. Navigate to `localhost:8080` and start planning

**Building and running with IntelliJ IDEA**
1. Clone the source code as in step 1 above, but in `~/IdeaProjects`.
2. Open IDEA
3. Hit run (the green play button)
4. Navigate to `localhost:8080` and start planning

Should you wish to not run a local instance, navigate to \[azure-link\] and start planning! This requires internet access.

See [Contributing](contributing.md) for policies on contribution.
