# github-access-reports
Spring Boot REST API that generates GitHub organization access reports by aggregating users, repositories, and collaborator permissions, with secure token authentication, pagination, scalability, and clean layered architecture.
## 1. Problem Statement

Organizations can have many repositories and users, making it difficult to review repository access manually.

This application provides a centralized report by:

1. Authenticating with GitHub.
2. Retrieving repositories belonging to a GitHub organization.
3. Retrieving users/collaborators with access to each repository.
4. Aggregating repository access by GitHub user.
5. Exposing the final report through a JSON REST API.

The design is intended to support organizations with 100+ repositories and 1000+ users with repository access.

---

## 2. Architecture

The application follows a layered architecture:

```text
Client
  |
  v
AccessReportController
  |
  v
AccessReportService
  |
  v
GitHubClient
  |
  v
GitHub REST API
```

### Main components

| Component | Responsibility |
|---|---|
| `AccessReportController` | Exposes the REST API endpoint |
| `AccessReportService` | Contains report-generation and aggregation logic |
| `GitHubClient` | Communicates with the GitHub REST API |
| `GitHubProperties` | Stores GitHub API configuration |
| `GitHubRepositoryDto` | Represents required repository data |
| `GitHubCollaboratorDto` | Represents collaborator/access data |
| `AccessReportResponse` | Represents the final report |
| `UserAccessDto` | Represents a user's repository access |
| `RepositoryAccessDto` | Represents repository/access details |

This separation keeps HTTP handling, business logic, external API communication, configuration and data structures independent.

---

## 3. Technology Stack

- Java
- Spring Boot
- Spring Web / REST
- Spring `RestClient`
- Maven
- JUnit
- GitHub REST API

---

## 4. Prerequisites

Before running the project, install:

- Java
- Maven
- Git
- A GitHub account with sufficient permissions for the organization/repositories you want to report on

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

## 5. GitHub Authentication

The application uses a GitHub Personal Access Token.

The token should **not** be hard-coded in Java source code or committed to Git.

The application expects the following environment variable:

```text
GITHUB_TOKEN
```

The application configuration uses the environment variable through:

```yaml
github:
  api-url: https://api.github.com
  token: ${GITHUB_TOKEN}
```

### Windows Command Prompt

For the current terminal session:

```cmd
set GITHUB_TOKEN=YOUR_GITHUB_TOKEN
```

For a permanent user environment variable:

```cmd
setx GITHUB_TOKEN "YOUR_GITHUB_TOKEN"
```

After using `setx`, open a new terminal before starting the application.

### Windows PowerShell

```powershell
$env:GITHUB_TOKEN="YOUR_GITHUB_TOKEN"
```

Do not commit the actual token to GitHub.

---

## 6. Clone the Project

Replace the URL below with the actual public repository URL:

```bash
git clone https://github.com/YOUR_USERNAME/github-access-reports.git
```

Move into the project:

```bash
cd github-access-reports
```

---

## 7. Build the Project

Run:

```bash
mvn clean install
```

To skip tests while building:

```bash
mvn clean install -DskipTests
```

---

## 8. Run the Application

Run using Maven:

```bash
mvn spring-boot:run
```

Or, after building the project, run the generated JAR:

```bash
java -jar target/github-access-reports-<version>.jar
```

The application uses port `8080` and the context path:

```text
/github-access-report
```

---

## 9. API Endpoint

### Generate Access Report

**Method:**

```text
GET
```

**Endpoint:**

```text
http://localhost:8080/github-access-report/api/github/access-report/{organization}
```

Replace `{organization}` with the GitHub organization name.

### Example

```text
http://localhost:8080/github-access-report/api/github/access-report/spring-projects
```

Using curl:

```bash
curl http://localhost:8080/github-access-report/api/github/access-report/spring-projects
```

---

## 10. Request Flow

When the endpoint is called:

```text
GET /github-access-report/api/github/access-report/{organization}
                    |
                    v
        AccessReportController
                    |
                    v
        AccessReportService
                    |
                    v
              GitHubClient
                    |
                    v
              GitHub API
```

The process is:

1. The controller receives the organization name.
2. `AccessReportService` starts report generation.
3. `GitHubClient` retrieves organization repositories.
4. Repository pagination is handled by `GitHubClient`.
5. Collaborator/access information is retrieved for each repository.
6. `AccessReportService` aggregates access by GitHub login.
7. `AccessReportResponse` is returned.
8. Spring serializes the response as JSON.

---

## 11. Aggregation Strategy

GitHub provides access information at repository level.

For example:

```text
Repository A
  - user1
  - user2

Repository B
  - user1
  - user3
```

The application transforms this into a user-centric report:

```text
user1
  - Repository A
  - Repository B

user2
  - Repository A

user3
  - Repository B
```

This makes the report useful for access reviews because an administrator can see the repository access footprint of each user.

---

## 12. Scalability Strategy

The assignment requires support for:

- 100+ repositories
- 1000+ users with repository access

The application addresses this through several design decisions.

### Pagination

GitHub API results can span multiple pages.

The `GitHubClient` uses pagination with a page size of 100 rather than assuming that all repositories are returned in a single response.

### Efficient aggregation

Users are aggregated by GitHub login using a map-oriented strategy.

This avoids repeatedly scanning the entire user collection for every collaborator.

If:

- `R` = number of repositories
- `C` = total collaborator records

the aggregation work is approximately:

```text
O(R + C)
```

excluding external network/API latency.

### Avoiding unnecessary API calls

The application:

- Retrieves repository data through the dedicated client.
- Handles pagination centrally.
- Processes returned data once.
- Avoids duplicating GitHub API calls unnecessarily.

### Future production optimization

If performance measurements show that collaborator calls become the main bottleneck, controlled parallelism can be introduced.

However, concurrency must be bounded and GitHub rate limits must be respected.

---

## 13. Error Handling

Important GitHub errors include:

### 401 – Bad Credentials

This usually indicates that the authentication credentials were not accepted.

During development, an important issue was an environment-variable configuration problem where the application was reading:

```text
${GITHUB_TOKEN}
```

as a literal value instead of resolving it to the actual environment variable.

The solution is to verify that `GITHUB_TOKEN` is correctly configured before changing the application logic.

### 403 – Resource Not Accessible

A 403 can occur when the authenticated GitHub account/token does not have sufficient permission for the requested operation.

The token and GitHub account permissions should therefore be checked.

### Other failures

A production implementation should also handle:

- Organization not found
- Repository not found
- GitHub API errors
- Rate-limit responses
- Network failures
- Unexpected application errors

Errors should be translated into meaningful API responses without exposing internal stack traces or credentials.

---

## 14. Security

The project follows these security practices:

- GitHub token is supplied through an environment variable.
- Token is not hard-coded in Java.
- Token should not be committed to Git.
- Token should not be printed in logs.
- GitHub communication uses HTTPS.
- The GitHub token should have only the permissions required for the application.

Before making the repository public, check the entire project for accidentally committed secrets.

---

## 15. Testing Strategy

The main business logic is in `AccessReportService`, making it a strong unit-testing target.

Recommended tests include:

- Successful report generation
- Multiple repositories for the same user
- Multiple users in the same repository
- Different access roles
- Empty organization
- GitHub API failure
- Pagination
- Authentication failure
- Permission failure
- Large repository/user data sets
- Controller response behavior

`GitHubClient` can be mocked when testing `AccessReportService`, so unit tests do not need a live GitHub connection.

---

## 16. Design Decisions

### Layered architecture

The application separates:

```text
Controller
Service
Client
Configuration
DTOs
```

This makes the application easier to understand, test and maintain.

### GitHubClient abstraction

All GitHub REST communication is kept inside `GitHubClient`.

This prevents external API details from spreading into business logic.

### Service-level aggregation

`AccessReportService` owns the transformation from repository-level collaborator data to user-level access data.

### Externalized credentials

The GitHub token is supplied by the environment instead of being stored in source code.

### Pagination

Pagination is handled by the client so the service does not need to know GitHub's pagination mechanics.

---

## 17. Assumptions

The project assumes:

1. The supplied GitHub token is valid.
2. The authenticated GitHub account has sufficient access to the organization and repositories being queried.
3. GitHub is the source of truth for the access information being reported.
4. Repository and collaborator APIs may return paginated results.
5. The report aggregates users by GitHub login.
6. The service is a reporting system; it does not modify GitHub permissions.
7. GitHub API rate limits must be respected.

---

## 18. Project Structure

The project follows a structure similar to:

```text
src/
 ├── main/
 │   ├── java/
 │   │   └── com/githubaccess/report/
 │   │       ├── GithubAccessReportsApplication.java
 │   │       ├── controller/
 │   │       │   └── AccessReportController.java
 │   │       ├── service/
 │   │       │   └── AccessReportService.java
 │   │       ├── client/
 │   │       │   ├── GitHubClient.java
 │   │       │   └── dto/
 │   │       │       ├── GitHubRepositoryDto.java
 │   │       │       └── GitHubCollaboratorDto.java
 │   │       ├── dto/
 │   │       │   ├── AccessReportResponse.java
 │   │       │   ├── UserAccessDto.java
 │   │       │   └── RepositoryAccessDto.java
 │   │       └── config/
 │   │           └── GitHubProperties.java
 │   └── resources/
 │       └── application.yml
 └── test/
     └── java/
```

Adjust the exact package/file names if the final repository structure differs.

---

## 19. Production Improvements

For a production deployment, the following improvements could be added:

- Centralized exception handling using `@RestControllerAdvice`
- Structured logging
- GitHub rate-limit monitoring
- Metrics and health checks
- Request validation
- Controlled parallel GitHub requests
- Caching where appropriate
- Retry with backoff for suitable transient failures
- More integration tests
- CI/CD pipeline
- Secret management through the deployment platform
- API documentation using OpenAPI/Swagger

These should be introduced based on actual performance and operational requirements rather than adding unnecessary complexity.

---

## 20. GitHub Repository

Add the final public repository URL here:

```text
https://github.com/YOUR_USERNAME/github-access-reports
```

---

## 21. Assignment Requirement Coverage

This project addresses the assignment requirements:

- [x] Secure GitHub authentication
- [x] Retrieve organization repositories
- [x] Determine repository user access
- [x] Aggregate users to repositories
- [x] Return a JSON access report
- [x] Design for 100+ repositories
- [x] Design for 1000+ users
- [x] Efficient API usage
- [x] Clean code organization
- [x] Error handling
- [x] Readable and maintainable code
- [x] README documentation
- [x] Authentication configuration documentation
- [x] API usage documentation
- [x] Assumptions and design decisions

---

## 22. Author   

Lalit wagh

**GitHub Access Reports**

Java Intern Assignment Project
