package com.githubaccess.report.dto;

import java.util.List;

public record AccessReportResponse(String organization,
        List<UserAccessDto> users) {

}
