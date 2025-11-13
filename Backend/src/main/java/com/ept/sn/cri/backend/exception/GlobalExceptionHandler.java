package com.ept.sn.cri.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<Object> handleInvalidAction(InvalidActionException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(AlreadyAppliedException.class)
    public ResponseEntity<Object> handleAlreadyApplied(AlreadyAppliedException ex, WebRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(EvaluationNotFoundException.class)
    public ResponseEntity<Object> handleEvaluationNotFound(EvaluationNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ApplicationNotBelongToOfferException.class)
    public ResponseEntity<Object> handleApplicationNotBelongToOffer(ApplicationNotBelongToOfferException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Object> handleFileStorageException(FileStorageException ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Object> handleUnauthorizedAction(UnauthorizedActionException ex, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailExists(EmailAlreadyExistsException ex, WebRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue", request);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, status);
    }
}
