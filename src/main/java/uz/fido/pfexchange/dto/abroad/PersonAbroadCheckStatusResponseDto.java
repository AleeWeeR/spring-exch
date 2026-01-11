package uz.fido.pfexchange.dto.abroad;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Check status response DTO
 * Response for /check-status endpoint
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

    @JsonProperty("result")
    private Integer result;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("ws_id")
    private Long wsId;

    @JsonProperty("status")
    private Integer status;
}
