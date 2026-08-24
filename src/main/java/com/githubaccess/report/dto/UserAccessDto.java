package com.githubaccess.report.dto;

import java.util.List;

public record UserAccessDto( String username,
        List<RepositoryAccessDto> repositories) {

}
