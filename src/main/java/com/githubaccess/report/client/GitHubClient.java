package com.githubaccess.report.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.githubaccess.report.config.GitHubProperties;

@Component
public class GitHubClient {

	
	 private static final int PAGE_SIZE = 100;

	    private final RestClient restClient;

	    public GitHubClient(
	            RestClient.Builder restClientBuilder,
	            GitHubProperties githubProperties) {
	    	
	    	 // TEMPORARY DEBUG CODE
	    	 /*String tokenFromSpring = githubProperties.token();
	    	    String tokenFromEnvironment = System.getenv("GITHUB_TOKEN");

	    	    System.out.println("===== TOKEN DEBUG =====");

	    	    System.out.println("Spring property token = "
	    	            + tokenFromSpring);

	    	    System.out.println("Environment variable exists = "
	    	            + (tokenFromEnvironment != null
	    	            && !tokenFromEnvironment.isBlank()));

	    	    System.out.println("Environment token prefix = "
	    	            + (tokenFromEnvironment != null
	    	            && tokenFromEnvironment.length() >= 11
	    	            ? tokenFromEnvironment.substring(0, 11)
	    	            : "MISSING"));

	    	    System.out.println("=======================");*/

	        this.restClient = restClientBuilder
	                .baseUrl(githubProperties.apiUrl())
	                .defaultHeader(
	                        "Authorization",
	                        "Bearer " + githubProperties.token()
	                )
	                .defaultHeader(
	                        "Accept",
	                        "application/vnd.github+json"
	                )
	                .defaultHeader(
	                        "X-GitHub-Api-Version",
	                        "2026-03-10"
	                )
	                .build();
	    }

	    public List<GitHubRepositoryDto> getRepositories(String organization) {

	        List<GitHubRepositoryDto> repositories = new ArrayList<>();

	        int page = 1;

	        while (true) {

	            List<GitHubRepositoryDto> pageResult = restClient.get()
	                    .uri(
	                            "/orgs/{organization}/repos?per_page={perPage}&page={page}",
	                            organization,
	                            PAGE_SIZE,
	                            page
	                    )
	                    .retrieve()
	                    .body(new ParameterizedTypeReference<List<GitHubRepositoryDto>>() {
	                    });
	            
	            //debugging code
	            
	           /* System.out.println("ORGANIZATION = " + organization);

	            System.out.println("REPOSITORIES FOUND = " +
	                    (pageResult == null ? 0 : pageResult.size()));
	            
	            */

	            if (pageResult == null || pageResult.isEmpty()) {
	                break;
	            }

	            repositories.addAll(pageResult);

	            if (pageResult.size() < PAGE_SIZE) {
	                break;
	            }

	            page++;
	        }

	        return repositories;
	    }

	    public List<GitHubCollaboratorDto> getCollaborators(
	            String organization,
	            String repository) {

	        List<GitHubCollaboratorDto> collaborators = new ArrayList<>();

	        int page = 1;

	        while (true) {

	            List<GitHubCollaboratorDto> pageResult = restClient.get()
	                    .uri(
	                            "/repos/{owner}/{repo}/collaborators?affiliation={affiliation}&per_page={perPage}&page={page}",
	                            organization,
	                            repository,
	                            "all",
	                            PAGE_SIZE,
	                            page
	                    )
	                    .retrieve()
	                    .body(new ParameterizedTypeReference<List<GitHubCollaboratorDto>>() {
	                    });

	            if (pageResult == null || pageResult.isEmpty()) {
	                break;
	            }

	            collaborators.addAll(pageResult);

	            if (pageResult.size() < PAGE_SIZE) {
	                break;
	            }

	            page++;
	        }

	        return collaborators;
	    }
}