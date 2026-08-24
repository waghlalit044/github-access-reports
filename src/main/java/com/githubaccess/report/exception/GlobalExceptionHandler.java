package com.githubaccess.report.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleGitHubError(
            HttpClientErrorException exception) {

        HttpStatus status = HttpStatus.valueOf(
                exception.getStatusCode().value()
        );

        String message;

        if (status == HttpStatus.UNAUTHORIZED) {
            message = "GitHub authentication failed. Check your GITHUB_TOKEN.";
        } else if (status == HttpStatus.FORBIDDEN) {
            message = "GitHub access denied. The token does not have permission to access this resource.";
        } else if (status == HttpStatus.NOT_FOUND) {
            message = "GitHub organization or repository was not found.";
        } else {
            message = "GitHub API request failed.";
        }

        Map<String, Object> response = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(
            Exception exception) {

        Map<String, Object> response = Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "An unexpected error occurred."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
