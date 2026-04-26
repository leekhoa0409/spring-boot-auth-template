package com.auth.template.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ApiResponse <T> {

    @Schema(description = "Custom status code", example = "1000")
    private int code = 1000;

    @Schema(description = "Response message if has error", example = "Invalid input data")
    private String message;

    @Schema(description = "Result from wrapped class", example = "{}")
    private T result;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
