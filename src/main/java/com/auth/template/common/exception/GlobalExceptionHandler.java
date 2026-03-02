package com.auth.template.common.exception;

import com.auth.template.common.dto.ApiResponse;
import com.auth.template.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(AppException.class)
        public ResponseEntity<ErrorResponse> handleAppException(AppException exception, HttpServletRequest request) {
                ErrorCode errorCode = exception.getErrorCode();
                ErrorResponse errorResponse = new ErrorResponse();

                errorResponse.setCode(errorCode.getCode());
                errorResponse.setMessage(errorCode.getMessage());
                errorResponse.setPath(request.getRequestURI());
                return ResponseEntity.status(errorCode.getHttpStatusCode()).body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception, HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse();

                errorResponse.setMessage(ErrorCode.UNCATEGORIZED.getMessage());
                errorResponse.setCode(ErrorCode.UNCATEGORIZED.getCode());
                errorResponse.setPath(request.getRequestURI());
                return ResponseEntity.internalServerError().body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
                                                                       HttpServletRequest request) {
                Map<String, List<String>> errors = new HashMap<>();

                exception.getBindingResult().getFieldErrors().forEach(error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();

                        errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
                });
                ApiResponse<Map<String, List<String>>> apiResponse = ApiResponse.<Map<String, List<String>>>builder()
                        .code(ErrorCode.VALIDATION_ERROR.getCode())
                        .message(ErrorCode.VALIDATION_ERROR.getMessage())
                        .result(errors)
                        .build();

                return ResponseEntity.badRequest().body(apiResponse);
        }
}
