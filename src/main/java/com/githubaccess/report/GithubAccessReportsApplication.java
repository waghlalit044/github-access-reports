package com.githubaccess.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.githubaccess.report.config.GitHubProperties;

@SpringBootApplication
@EnableConfigurationProperties(GitHubProperties.class)

public class GithubAccessReportsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubAccessReportsApplication.class, args);
	}

}
