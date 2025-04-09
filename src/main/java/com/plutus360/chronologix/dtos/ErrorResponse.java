package com.plutus360.chronologix.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Error response structure")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    

    @Schema(description = "Indicates whether the request was successful", example = "false")
    private boolean status;

    @Schema(description = "A general message describing the error", example = "Processing failed")
    private String message;

    @Schema(description = "List of errors, each containing a field and a message")
    private List<ErrorDetail> errors;


    @Schema(description = "Details of a single error")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorDetail {

        @Schema(description = "The field associated with the error", example = "general")
        private String field;

        @Schema(description = "The error message", example = "Unable to process the item due to invalid data")
        private String message;

    }
}
