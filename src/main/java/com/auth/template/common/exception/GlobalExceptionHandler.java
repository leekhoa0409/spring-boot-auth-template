package com.auth.template.common.exception;

import com.auth.template.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(AppException.class)
        public ResponseEntity<ErrorResponse> handleAppException(AppException exception, HttpServletRequest request) {
                ErrorCode errorCode = exception.getErrorCode();
                ErrorResponse errorResponse = new ErrorResponse();

                errorResponse.setCode(errorCode.getCode());
                errorResponse.setMessage(errorCode.getMessage());
                errorResponse.setPath(request.getRequestURI());
                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception, HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse();

                errorResponse.setMessage(ErrorCode.UNCATEGORIZED.getMessage());
                errorResponse.setCode(ErrorCode.UNCATEGORIZED.getCode());
                errorResponse.setPath(request.getRequestURI());
                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletRequest request) {
                String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
                ErrorCode errorCode = ErrorCode.valueOf(enumKey);

                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setCode(errorCode.getCode());
                errorResponse.setMessage(errorCode.getMessage());
                errorResponse.setPath(request.getRequestURI());
                return ResponseEntity.badRequest().body(errorResponse);
        }
}
