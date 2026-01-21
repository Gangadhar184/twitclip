package com.example.twitclip.exception;

import com.example.twitclip.dto.ClipResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ClipResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> serverError(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(new ClipResponse("Server failed to clip video"));
    }
}
