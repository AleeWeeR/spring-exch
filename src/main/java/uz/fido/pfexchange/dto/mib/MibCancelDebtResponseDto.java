package uz.fido.pfexchange.dto.mib;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from MIB pension cancel-inventory API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibCancelDebtResponseDto {

    @JsonProperty("result_code")
    private Integer resultCode;

    @JsonProperty("result_message")
    private String resultMessage;
}
