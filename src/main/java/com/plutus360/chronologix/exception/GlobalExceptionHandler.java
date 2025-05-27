package com.plutus360.chronologix.exception;


import java.util.ArrayList;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.plutus360.chronologix.dtos.ErrorResponse;

import lombok.extern.slf4j.Slf4j;
import java.sql.SQLException;

@Slf4j
@RestControllerAdvice 
public class GlobalExceptionHandler {


    @ExceptionHandler(UnableToProccessIteamException.class)
    public ResponseEntity<ErrorResponse> handleUnableToProccessIteamException(UnableToProccessIteamException ex) {
        log.error("UnableToProccessIteamException: {}", ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail();
        errorDetail.setField("general"); // Default field
        errorDetail.setMessage(ex.getMessage());

        // Create the error response
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Processing failed");
        errorResponse.setErrors(List.of(errorDetail));


        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleHibernateConstraintViolationException(
            ConstraintViolationException ex) {
        log.error("Hibernate ConstraintViolationException: {}", ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail();
        
        // Get the underlying SQLException which contains the detailed error message
        SQLException sqlException = ex.getSQLException();
        String detailMessage = null;
        String fieldName = "general";
        
        if (sqlException != null) {
            String sqlMessage = sqlException.getMessage();
            log.debug("SQL Exception message: {}", sqlMessage);
            
            // Extract the detail message directly from SQL exception
            // Pattern: "Detail: Key (field_name)=(value) already exists."
            if (sqlMessage != null && sqlMessage.contains("Detail: Key (")) {
                int detailStart = sqlMessage.indexOf("Detail: ");
                if (detailStart != -1) {
                    detailMessage = sqlMessage.substring(detailStart + 8); // "Detail: ".length() = 8
                    // Extract field name from "Key (field_name)=(value)"
                    int keyStart = detailMessage.indexOf("Key (");
                    int keyEnd = detailMessage.indexOf(")=");
                    if (keyStart != -1 && keyEnd != -1) {
                        fieldName = detailMessage.substring(keyStart + 5, keyEnd); // "Key (".length() = 5
                    }
                }
            }
        }
        
        // Set the field and message
        errorDetail.setField(fieldName);
        if (detailMessage != null && !detailMessage.trim().isEmpty()) {
            errorDetail.setMessage(detailMessage.trim());
        } else {
            // Fallback message
            errorDetail.setMessage("A record with this " + fieldName + " already exists.");
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Data validation failed");
        errorResponse.setErrors(List.of(errorDetail));

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage());

        // Check if the root cause is a Hibernate ConstraintViolationException
        if (ex.getCause() instanceof ConstraintViolationException constraintViolationException) {
            return handleHibernateConstraintViolationException(constraintViolationException);
        }

        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail();
        errorDetail.setField("general");
        
        String message = ex.getMessage();
        if (message != null && message.contains("duplicate key")) {
            if (message.contains("email")) {
                errorDetail.setField("email");
                errorDetail.setMessage("Email address already exists. Please use a different email.");
            } else if (message.contains("username")) {
                errorDetail.setField("username");
                errorDetail.setMessage("Username already exists. Please choose a different username.");
            } else {
                errorDetail.setMessage("A record with this information already exists.");
            }
        } else {
            errorDetail.setMessage("Data integrity constraint violation.");
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Data validation failed");
        errorResponse.setErrors(List.of(errorDetail));

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleJakartaConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {
        log.error("Jakarta ConstraintViolationException: {}", ex.getMessage());

        List<ErrorResponse.ErrorDetail> errorDetails = new ArrayList<>();
        
        ex.getConstraintViolations().forEach(violation -> {
            ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail();
            errorDetail.setField(violation.getPropertyPath().toString());
            errorDetail.setMessage(violation.getMessage());
            errorDetails.add(errorDetail);
        });

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Validation failed");
        errorResponse.setErrors(errorDetails);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
        log.error("SQLException: {}", ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail();
        errorDetail.setField("general");
        
        // Handle common SQL error codes
        String sqlState = ex.getSQLState();
        if ("23505".equals(sqlState)) { // PostgreSQL unique constraint violation
            errorDetail.setMessage("A record with this information already exists.");
        } else if ("23000".equals(sqlState)) { // Generic integrity constraint violation
            errorDetail.setMessage("Data constraint violation. Please check your input data.");
        } else {
            errorDetail.setMessage("Database operation failed. Please try again.");
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Database error");
        errorResponse.setErrors(List.of(errorDetail));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception: ", ex);

        ErrorResponse.ErrorDetail errorDetail = new ErrorResponse.ErrorDetail();
        errorDetail.setField("general");
        errorDetail.setMessage("An unexpected error occurred. Please try again later.");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(false);
        errorResponse.setMessage("Internal server error");
        errorResponse.setErrors(List.of(errorDetail));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
