package com.githubaccess.report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.githubaccess.report.dto.AccessReportResponse;
import com.githubaccess.report.service.AccessReportService;

@RestController
@RequestMapping("/api/github/access-report")
public class AccessReportController {

    private final AccessReportService accessReportService;

    public AccessReportController(AccessReportService accessReportService) {
        this.accessReportService = accessReportService;
    }

    @GetMapping("/{organization}")
    public AccessReportResponse getAccessReport(
            @PathVariable String organization) {

        return accessReportService.generateReport(organization);
    }
}