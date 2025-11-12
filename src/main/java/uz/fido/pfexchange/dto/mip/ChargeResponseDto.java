package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for charge information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponseDto {

    @JsonProperty("result")
    private Integer result;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("ws_id")
    private Long wsId;

    @JsonProperty("fio")
    private String fio;

    @JsonProperty("retention")
    private List<RetentionInfo> retention;

    /**
     * Retention/Charge details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionInfo {

        @JsonProperty("retention_type")
        private String retentionType;

        @JsonProperty("total_debt")
        private String totalDebt;

        @JsonProperty("balance_debt")
        private String balanceDebt;

        @JsonProperty("debt_number")
        private String debtNumber;

        @JsonProperty("fio_recipient")
        private String fioRecipient;
    }
}