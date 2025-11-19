package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Check status response DTO
 * Response for /check-status endpoint
 *
 * result: 1=success (200), 0=error
 * status:
 *   1 = faol (active)
 *   2 = nofaol (close_desc=11, abroad)
 *   3 = nofaol, Pensiya jamg'armasiga muroiaat qiling (other reasons)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Check status javobi")
public class PersonAbroadCheckStatusResponseDto {

    @Schema(
            description = "Natija: 1=muvaffaqiyatli, 0=xatolik",
            example = "1",
            required = true
    )
    @JsonProperty("result")
    private Integer result;

    @Schema(
            description = "Xabar matni",
            example = ""
    )
    @JsonProperty("msg")
    private String msg;

    @Schema(
            description = "Veb-servis identifikatori",
            example = "77",
            required = true
    )
    @JsonProperty("ws_id")
    private Long wsId;

    @Schema(
            description = "Holat: 1=faol, 2=nofaol (chet elda), 3=nofaol (boshqa sabablar)",
            example = "1",
            required = true
    )
    @JsonProperty("status")
    private Integer status;
}
