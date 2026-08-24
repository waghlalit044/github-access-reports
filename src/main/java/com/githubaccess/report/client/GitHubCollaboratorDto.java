package com.githubaccess.report.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubCollaboratorDto(String login,
		
		 @JsonProperty("role_name")
        String roleName) {

}
