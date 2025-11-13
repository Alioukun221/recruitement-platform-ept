package com.ept.sn.cri.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private int status;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    private List<String> errors;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}