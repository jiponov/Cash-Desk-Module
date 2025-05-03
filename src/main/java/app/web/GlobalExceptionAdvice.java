package app.web;

import app.shared.exception.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    // пропуснато поле или невалидна стойност. грешки от Bean Validation. 400 BAD_REQUEST. Валидирането на @Valid тела (например в @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {

        StringBuilder sb = new StringBuilder("Validation failed: ");

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = error.getField() + ": " + error.getDefaultMessage();
            sb.append(message).append("; ");
            log.warn("Validation error - {}", message);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(sb.toString());
    }


    // невалидна сума или несъответстващи купюри. невалидна бизнес логика. 400 BAD_REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArguments(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid input: " + ex.getMessage());
    }


    // недостатъчен баланс или липса на банкноти. custom логика (изключение). 400 BAD_REQUEST.
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handleDomainErrors(DomainException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Business rule error: " + ex.getMessage());
    }


    // грешки при четене или писане. файлова грешка (server problem - ако .txt файл е заключен, липсва и т.н.). 500 INTERNAL_SERVER_ERROR
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOErrors(IOException ex) {
        log.error("File I/O error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("A file error occurred. Please contact support.");
    }


    // Exception.class: всякакви други изключения. 500 INTERNAL_SERVER_ERROR
    // нещо непредвидено (NullPointer, Runtime и т.н.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
    }

}