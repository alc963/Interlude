## Requirements

### Machine

The spring boot project was setup with [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21). Make sure it's **JDK 21** to avoid issues down the line.

[Docker Desktop](https://www.docker.com/products/docker-desktop/) for postgres in docker.

WSL is required for Docker on Windows devices. In the terminal `wsl.exe --install Ubuntu` (may take a while to download).

### VScode

[Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) for various Java tools.

[Spring Boot Extension Pack](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack) for various Spring Boot tools.

## Initial Spring boot dependencies

### Core essentials

* **Spring Web**: Gives you Spring MVC + embedded Tomcat, needed for building REST APIs / web endpoints

* **Spring Data JPA**: ORM layer — lets you define entities/repositories instead of writing raw SQL

* **PostgreSQL Driver**: The JDBC driver so Spring can actually talk to your Postgres DB

### Others

* **Validation**: @NotNull, @Size, etc. on request DTOs — saves you from writing manual null/format checks

* **Spring Boot DevTools**: Auto-restarts the app on code changes, faster local dev loop for the team

* **Lombok**: Cuts boilerplate (getters/setters/constructors) via annotations — genuinely helpful in a multi-person codebase so entity/DTO classes aren't 80 lines of boilerplate

* **spring-dotdev**: Auto-loads .env files for