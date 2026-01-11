package uz.fido.pfexchange.dto.mip.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipReportRequestDto {

    @NotNull
    private Long wsId;

    @NotBlank
    @Size(min = 14, max = 14)
    @Pattern(regexp = "\\d{14}", message = "{pinfl.pattern}")
    private String pinfl;
}
