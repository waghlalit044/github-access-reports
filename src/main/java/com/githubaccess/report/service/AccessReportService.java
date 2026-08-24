package com.githubaccess.report.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

import com.githubaccess.report.client.GitHubClient;
import com.githubaccess.report.client.GitHubCollaboratorDto;
import com.githubaccess.report.client.GitHubRepositoryDto;
import com.githubaccess.report.dto.AccessReportResponse;
import com.githubaccess.report.dto.RepositoryAccessDto;
import com.githubaccess.report.dto.UserAccessDto;

import jakarta.annotation.PreDestroy;

@Service
public class AccessReportService {

	private static final int MAX_PARALLEL_REQUESTS = 10;

    private final GitHubClient gitHubClient;

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(MAX_PARALLEL_REQUESTS);

    public AccessReportService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public AccessReportResponse generateReport(String organization) {

        List<GitHubRepositoryDto> repositories =
                gitHubClient.getRepositories(organization);

        Map<String, List<RepositoryAccessDto>> accessByUser =
                new LinkedHashMap<>();

        List<Future<List<UserRepositoryAccess>>> futures =
                new ArrayList<>();

        /*
         * Fetch collaborators for repositories in parallel.
         *
         * Maximum 10 repository requests are executed at the same time.
         */
        for (GitHubRepositoryDto repository : repositories) {

            Future<List<UserRepositoryAccess>> future =
                    executorService.submit(() ->
                            getRepositoryAccess(
                                    organization,
                                    repository.name()
                            )
                    );

            futures.add(future);
        }

        /*
         * Collect results and build the final user -> repositories map.
         */
        for (Future<List<UserRepositoryAccess>> future : futures) {

            try {

                List<UserRepositoryAccess> repositoryAccessList =
                        future.get();

                for (UserRepositoryAccess access :
                        repositoryAccessList) {

                    RepositoryAccessDto repositoryAccess =
                            new RepositoryAccessDto(
                                    access.repository(),
                                    access.role()
                            );

                    accessByUser
                            .computeIfAbsent(
                                    access.username(),
                                    key -> new ArrayList<>()
                            )
                            .add(repositoryAccess);
                }

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Report generation was interrupted.",
                        exception
                );

            } catch (ExecutionException exception) {

                Throwable cause = exception.getCause();

                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new IllegalStateException(
                        "Failed to retrieve repository access.",
                        cause
                );
            }
        }

        List<UserAccessDto> users = accessByUser.entrySet()
                .stream()
                .map(entry -> new UserAccessDto(
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(UserAccessDto::username))
                .toList();

        return new AccessReportResponse(
                organization,
                users
        );
    }

    private List<UserRepositoryAccess> getRepositoryAccess(
            String organization,
            String repository) {

        List<GitHubCollaboratorDto> collaborators =
                gitHubClient.getCollaborators(
                        organization,
                        repository
                );

        List<UserRepositoryAccess> result =
                new ArrayList<>();

        for (GitHubCollaboratorDto collaborator : collaborators) {

            result.add(
                    new UserRepositoryAccess(
                            collaborator.login(),
                            repository,
                            collaborator.roleName()
                    )
            );
        }

        return result;
    }

    private record UserRepositoryAccess(
            String username,
            String repository,
            String role) {
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }
}