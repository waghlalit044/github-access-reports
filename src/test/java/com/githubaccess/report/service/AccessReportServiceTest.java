package com.githubaccess.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubaccess.report.client.GitHubClient;
import com.githubaccess.report.client.GitHubCollaboratorDto;
import com.githubaccess.report.client.GitHubRepositoryDto;
import com.githubaccess.report.dto.AccessReportResponse;
import com.githubaccess.report.dto.UserAccessDto;

@ExtendWith(MockitoExtension.class)
public class AccessReportServiceTest {
   
	 @Mock
	    private GitHubClient gitHubClient;

	    private AccessReportService accessReportService;

	    @BeforeEach
	    void setUp() {
	        accessReportService = new AccessReportService(gitHubClient);
	    }

	    @Test
	    void shouldGenerateAccessReport() {

	        String organization = "access-report-demo";

	        when(gitHubClient.getRepositories(organization))
	                .thenReturn(List.of(
	                        new GitHubRepositoryDto(
	                                "access-demo",
	                                "access-report-demo/access-demo"
	                        )
	                ));

	        when(gitHubClient.getCollaborators(
	                organization,
	                "access-demo"
	        )).thenReturn(List.of(
	                new GitHubCollaboratorDto(
	                        "waghlalit044",
	                        "admin"
	                )
	        ));

	        AccessReportResponse response =
	                accessReportService.generateReport(organization);

	        assertNotNull(response);

	        assertEquals(
	                organization,
	                response.organization()
	        );

	        assertEquals(
	                1,
	                response.users().size()
	        );

	        UserAccessDto user =
	                response.users().get(0);

	        assertEquals(
	                "waghlalit044",
	                user.username()
	        );

	        assertEquals(
	                1,
	                user.repositories().size()
	        );

	        assertEquals(
	                "access-demo",
	                user.repositories().get(0).repository()
	        );

	        assertEquals(
	                "admin",
	                user.repositories().get(0).permission()
	        );
	    }
	
}
