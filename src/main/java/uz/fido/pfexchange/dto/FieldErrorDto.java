package uz.fido.pfexchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FieldErrorDto {
    @Schema(description = "Field name")
    private String field;

    @Schema(description = "Error messages")
    private List<String> messages;
}
