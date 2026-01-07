package uz.fido.pfexchange.dto.mib;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for debt cancellation operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtCancellationResponseDto {

    @JsonProperty("result")
    private Integer result;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("external_id")
    private Long externalId;

    @JsonProperty("is_sent")
    private String isSent;

    @JsonProperty("is_cancelled")
    private String isCancelled;
}
