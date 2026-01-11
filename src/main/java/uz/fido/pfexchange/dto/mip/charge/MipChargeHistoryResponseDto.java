package uz.fido.pfexchange.dto.mip.charge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for charged history information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipChargeHistoryResponseDto {

    @JsonProperty("result")
    private Integer result;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("ws_id")
    private Long wsId;

    @JsonProperty("fio")
    private String fio;

    @JsonProperty("retention")
    private List<ChargedRetentionInfo> retention;

    /**
     * Charged retention details with period information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargedRetentionInfo {

        @JsonProperty("retention_type")
        private String retentionType;

        @JsonProperty("period")
        private String period;

        @JsonProperty("summ")
        private String summ;

        @JsonProperty("percent")
        private String percent;

        @JsonProperty("debt_number")
        private String debtNumber;
    }
}
