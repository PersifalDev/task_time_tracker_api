package ru.haritonenko.task_time_tracker_api.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.EmployeeNotFoundException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeArgumentException;
import ru.haritonenko.task_time_tracker_api.employee.domain.exception.IllegalEmployeeStateException;
import ru.haritonenko.task_time_tracker_api.handler.error.ErrorMessageResponse;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.IllegalTaskArgumentException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.IllegalTaskStateException;
import ru.haritonenko.task_time_tracker_api.tasks.domain.exception.TaskNotFoundException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.IllegalTimeRecordArgumentException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.IllegalTimeRecordStateException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.TaskOccupiedByAnotherEmployeeException;
import ru.haritonenko.task_time_tracker_api.time_records.domain.exception.TimeRecordNotFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String detailedMessage = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage()),
                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(error -> error.getDefaultMessage())
                )
                .collect(Collectors.joining(", "));

        if (detailedMessage.isBlank()) {
            detailedMessage = "Request validation failed";
        }

        log.warn("Request body validation failed: {}", detailedMessage, ex);

        var errorDto = getErrorMessageResponse(
                "Validation error",
                detailedMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessageResponse> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {
        String detailedMessage = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint validation failed: {}", detailedMessage, ex);

        var errorDto = getErrorMessageResponse(
                "Validation error",
                detailedMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalTaskStateException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalTaskStateException(
            IllegalTaskStateException ex
    ) {
        log.warn("Invalid task state: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Task state error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleTaskNotFoundException(
            TaskNotFoundException ex
    ) {
        log.warn("Task not found: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Task not found",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalTaskArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalTaskArgumentException(
            IllegalTaskArgumentException ex
    ) {
        log.warn("Invalid task argument: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Task argument error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(TimeRecordNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleTimeRecordNotFoundException(
            TimeRecordNotFoundException ex
    ) {
        log.warn("Time record not found: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Time record not found",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalTimeRecordArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalTimeRecordArgumentException(
            IllegalTimeRecordArgumentException ex
    ) {
        log.warn("Invalid time record argument: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Time record argument error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalTimeRecordStateException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalTimeRecordStateException(
            IllegalTimeRecordStateException ex
    ) {
        log.warn("Invalid time record state: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Time record state error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(TaskOccupiedByAnotherEmployeeException.class)
    public ResponseEntity<ErrorMessageResponse> handleTaskOccupiedByAnotherEmployeeException(
            TaskOccupiedByAnotherEmployeeException ex
    ) {
        log.warn("Task is occupied by another employee: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Task occupation error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEmployeeNotFoundException(
            EmployeeNotFoundException ex
    ) {
        log.warn("Employee not found: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Employee not found",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalEmployeeArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalEmployeeArgumentException(
            IllegalEmployeeArgumentException ex
    ) {
        log.warn("Invalid employee argument: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Employee argument error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalEmployeeStateException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalEmployeeStateException(
            IllegalEmployeeStateException ex
    ) {
        log.warn("Invalid employee state: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Employee state error",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessageResponse> handleAccessDeniedException(
            AccessDeniedException ex
    ) {
        log.warn("Access denied: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Forbidden",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorDto);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessageResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        log.warn("Request body parsing failed: {}", ex.getMessage(), ex);

        var errorDto = getErrorMessageResponse(
                "Request body parsing error",
                "Invalid request body format"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    private ErrorMessageResponse getErrorMessageResponse(
            String message,
            String detailedMessage
    ) {
        return new ErrorMessageResponse(
                message,
                detailedMessage,
                LocalDateTime.now().toString()
        );
    }
}